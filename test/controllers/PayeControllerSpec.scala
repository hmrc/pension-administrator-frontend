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

package controllers

import akka.stream.Materializer
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import forms.PayeFormProvider
import identifiers.TypedIdentifier
import models._
import models.requests.DataRequest
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.PayeViewModel
import views.html.paye

import scala.concurrent.Future

class PayeControllerSpec extends WordSpec with MustMatchers with MockitoSugar {

  import PayeControllerSpec._

  val viewmodel = PayeViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    hint = Some("hint")
  )

  "get" must {

    "return a successful result when there is no existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[PayeFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewmodel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual paye(appConfig, formProvider(), viewmodel)(request, messages).toString
      }
    }

    "return a successful result when there is an existing answer" in {

      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          val appConfig = app.injector.instanceOf[FrontendAppConfig]
          val formProvider = app.injector.instanceOf[PayeFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val answers = UserAnswers().set(FakeIdentifier)(Paye.Yes("paye")).get
          val result = controller.onPageLoad(viewmodel, answers)

          status(result) mustEqual OK
          contentAsString(result) mustEqual paye(
            appConfig,
            formProvider().fill(Paye.Yes("paye")),
            viewmodel
          )(request, messages).toString
      }
    }
  }

  "post" must {

    "redirect when the submitted data is valid" in {

      import play.api.inject._

      val cacheConnector = mock[UserAnswersCacheConnector]

      running(_.overrides(
        bind[UserAnswersCacheConnector].toInstance(cacheConnector),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>

          implicit val materializer: Materializer = app.materializer

          when(
            cacheConnector.save[Paye, FakeIdentifier.type](any(), eqTo(FakeIdentifier), any())(any(), any(), any())
          ).thenReturn(Future.successful(Json.obj()))

          val request = FakeRequest().withFormUrlEncodedBody(
            ("paye.hasPaye", "true"), ("paye.paye", "123/AB56789")
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
          val formProvider = app.injector.instanceOf[PayeFormProvider]
          val request = FakeRequest()
          val messages = app.injector.instanceOf[MessagesApi].preferred(request)
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewmodel, UserAnswers(), request)

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual paye(
            appConfig,
            formProvider().bind(Map.empty[String, String]),
            viewmodel
          )(request, messages).toString
      }
    }
  }
}

object PayeControllerSpec extends OptionValues {

  object FakeIdentifier extends TypedIdentifier[Paye]

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val cacheConnector: UserAnswersCacheConnector,
                                  override val navigator: Navigator,
                                  formProvider: PayeFormProvider
                                ) extends PayeController {

    def onPageLoad(viewmodel: PayeViewModel, answers: UserAnswers): Future[Result] = {
      get(FakeIdentifier, formProvider(), viewmodel)(DataRequest(FakeRequest(), "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))
    }

    def onSubmit(viewmodel: PayeViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(FakeIdentifier, NormalMode, formProvider(), viewmodel)(DataRequest(fakeRequest, "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))
    }
  }

}
