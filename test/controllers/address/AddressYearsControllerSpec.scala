/*
 * Copyright 2018 HM Revenue & Customs
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

import akka.stream.Materializer
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.DataCacheConnector
import forms.address.AddressYearsFormProvider
import identifiers.TypedIdentifier
import models.{AddressYears, NormalMode}
import models.requests.DataRequest
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContent, Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => eqTo, _}

import scala.concurrent.Future

object AddressYearsControllerSpec {

  object FakeIdentifier extends TypedIdentifier[AddressYears]

  class TestController @Inject() (
                                   override val appConfig: FrontendAppConfig,
                                   override val messagesApi: MessagesApi,
                                   override val cacheConnector: DataCacheConnector,
                                   override val navigator: Navigator,
                                   formProvider: AddressYearsFormProvider
                                 ) extends AddressYearsController {

    def onPageLoad(viewmodel: AddressYearsViewModel, answers: UserAnswers): Future[Result] = {
      get(FakeIdentifier, formProvider("error"), viewmodel)(DataRequest(FakeRequest(), "cacheId", answers))
    }

    def onSubmit(viewmodel: AddressYearsViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(FakeIdentifier, NormalMode, formProvider("error"), viewmodel)(DataRequest(fakeRequest, "cacheId", answers))
    }
  }
}

class AddressYearsControllerSpec extends WordSpec with MustMatchers with OptionValues with ScalaFutures with MockitoSugar {

  import AddressYearsControllerSpec._

  val viewmodel = AddressYearsViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    legend = "legend"
  )

  "get" must {

    "return a successful result when there is no existing answer" in {

      running(_.overrides()) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[AddressYearsFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual addressYears(appConfig, formProvider("error")(messages), viewmodel)(request, messages).toString
      }
    }

    "return a successful result when there is an existing answer" in {

      running(_.overrides()) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[AddressYearsFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val answers = UserAnswers().set(FakeIdentifier)(AddressYears.OverAYear).asOpt.value
          val result = controller.onPageLoad(viewmodel, answers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual addressYears(
            appConfig,
            formProvider("error")(messages).fill(AddressYears.OverAYear),
            viewmodel
          )(request, messages).toString
      }
    }
  }

  "post" must {

    "return a redirect when the submitted data is valid" in {

      import play.api.inject._

      val cacheConnector = mock[DataCacheConnector]

      running(_.overrides(
        bind[DataCacheConnector].toInstance(cacheConnector),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          when(cacheConnector.save[AddressYears, FakeIdentifier.type](
            eqTo(FakeIdentifier), any())(any(), any(), any(), any(), any())
          ) thenReturn Future.successful(UserAnswers())

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[AddressYearsFormProvider]
          val request = FakeRequest().withFormUrlEncodedBody(
            "value" -> AddressYears.OverAYear.toString
          )
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }

    "return a bad request when the submitted data is invalid" in {

      running(_.overrides()) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[AddressYearsFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual addressYears(
            appConfig,
            formProvider("error")(messages).bind(Map.empty[String, String]),
            viewmodel
          )(request, messages).toString
      }
    }
  }
}
