/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers.register

import akka.stream.Materializer
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import forms.register.VatFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{NormalMode, PSAUser, UserType, Vat}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.libs.json._
import play.api.mvc.{AnyContent, Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.VatViewModel
import views.html.register.vat

import scala.concurrent.Future

object VatControllerSpec {

  object FakeIdentifier extends TypedIdentifier[Vat]

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val cacheConnector: UserAnswersCacheConnector,
                                  override val navigator: Navigator,
                                  formProvider: VatFormProvider
                                ) extends VatController {

    def onPageLoad(viewmodel: VatViewModel, answers: UserAnswers): Future[Result] = {
      get(FakeIdentifier, formProvider(), viewmodel)(DataRequest(FakeRequest(), "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))
    }

    def onSubmit(viewmodel: VatViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(FakeIdentifier, NormalMode, formProvider(), viewmodel)(DataRequest(fakeRequest, "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))
    }
  }

}

class VatControllerSpec extends WordSpec with MustMatchers with OptionValues with ScalaFutures with MockitoSugar {

  import VatControllerSpec._

  val viewmodel = VatViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    hint = "legend",
    subHeading = Some("sub-heading")
  )

  "get" must {

    "return a successful result when there is no existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[VatFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual vat(appConfig, formProvider(), viewmodel)(request, messages).toString
      }
    }

    "return a successful result when there is an existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[VatFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val answers = UserAnswers().set(FakeIdentifier)(Vat.Yes("123456789")).get
          val result = controller.onPageLoad(viewmodel, answers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual vat(
            appConfig,
            formProvider().fill(Vat.Yes("123456789")),
            viewmodel
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

          implicit val materializer: Materializer = app.materializer

          when(
            cacheConnector.save[Vat, FakeIdentifier.type](any(), eqTo(FakeIdentifier), any())(any(), any(), any())
          ).thenReturn(Future.successful(Json.obj()))

          val request = FakeRequest().withFormUrlEncodedBody(
            ("vat.hasVat", "true"), ("vat.vat", "123456789")
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }

    "return a bad request when the submitted data is invalid" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[VatFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual vat(
            appConfig,
            formProvider().bind(Map.empty[String, String]),
            viewmodel
          )(request, messages).toString
      }
    }
  }
}
