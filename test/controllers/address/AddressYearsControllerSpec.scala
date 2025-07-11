/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.address

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions.FakeAllowAccessProvider
import forms.address.AddressYearsFormProvider
import identifiers.TypedIdentifier
import identifiers.register.DirectorsOrPartnersChangedId
import identifiers.register.partnership.partners.PartnerAddressYearsId
import models._
import models.requests.DataRequest
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.mockito.MockitoSugar
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import scala.concurrent.{ExecutionContext, Future}

object AddressYearsControllerSpec {

  object FakeIdentifier extends TypedIdentifier[AddressYears]

  class TestController @Inject()(appConfig: FrontendAppConfig,
                                 override val cacheConnector: UserAnswersCacheConnector,
                                 override val navigator: Navigator,
                                 formProvider: AddressYearsFormProvider,
                                 val controllerComponents: MessagesControllerComponents,
                                 val view: addressYears
                                )(implicit val executionContext: ExecutionContext) extends AddressYearsController with I18nSupport {

    override val allowAccess = FakeAllowAccessProvider(config = appConfig)

    def request(answers: UserAnswers, fakeRequest: Request[AnyContent] = FakeRequest()): DataRequest[AnyContent] =
      DataRequest(fakeRequest, "cacheId", PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), answers)

    def onPageLoad(viewmodel: AddressYearsViewModel, answers: UserAnswers): Future[Result] = {
      implicit val req: DataRequest[AnyContent] = request(answers)
      get(FakeIdentifier, formProvider("error"), viewmodel, NormalMode)
    }

    def onSubmit(viewmodel: AddressYearsViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent],
                 mode: Mode = NormalMode, id: TypedIdentifier[AddressYears] = FakeIdentifier): Future[Result] = {
      implicit val req: DataRequest[AnyContent] = request(answers, fakeRequest)
      post(id, mode, formProvider("error"), viewmodel)
    }
  }

}

class AddressYearsControllerSpec extends SpecBase with Matchers with OptionValues with ScalaFutures with MockitoSugar {

  import AddressYearsControllerSpec._

  val view: addressYears = app.injector.instanceOf[addressYears]

  val viewmodel = AddressYearsViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    legend = "legend"
  )

  "get" must {

    "return a successful result when there is no existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[AddressYearsFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(formProvider("error")(messages), viewmodel, NormalMode)(request, messages).toString
      }
    }

    "return a successful result when there is an existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[AddressYearsFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val answers = UserAnswers().set(FakeIdentifier)(AddressYears.OverAYear).asOpt.value
          val result = controller.onPageLoad(viewmodel, answers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            formProvider("error")(messages).fill(AddressYears.OverAYear),
            viewmodel,
            NormalMode
          )(request, messages).toString
      }
    }
  }

  "post" must {

    "return a redirect when the submitted data is valid" in {

      import play.api.inject._

      val cacheConnector = mock[UserAnswersCacheConnector]

      running(_.overrides(
        bind[UserAnswersCacheConnector].toInstance(cacheConnector),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          when(cacheConnector.save[AddressYears, FakeIdentifier.type](
            eqTo(FakeIdentifier), any())(any(), any(), any())
          ) thenReturn Future.successful(Json.obj())

          val request = FakeRequest().withFormUrlEncodedBody(
            "value" -> AddressYears.OverAYear.toString
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }

    "save the change flag in update mode" in {

      import play.api.inject._

      val cacheConnector = FakeUserAnswersCacheConnector

      running(_.overrides(
        bind[UserAnswersCacheConnector].toInstance(cacheConnector),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val request = FakeRequest().withFormUrlEncodedBody(
            "value" -> AddressYears.OverAYear.toString
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request, UpdateMode, PartnerAddressYearsId(0))

          status(result) mustEqual SEE_OTHER
          FakeUserAnswersCacheConnector.verify(DirectorsOrPartnersChangedId, true)
      }
    }

    "return a bad request when the submitted data is invalid" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[AddressYearsFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(
            formProvider("error")(messages).bind(Map.empty[String, String]),
            viewmodel,
            NormalMode
          )(request, messages).toString
      }
    }
  }

}

