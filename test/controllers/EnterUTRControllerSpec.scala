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

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import forms.EnterUTRFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{NormalMode, PSAUser, ReferenceValue, UserType}
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.mvc.{AnyContent, Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.CommonFormWithHintViewModel
import views.html.enterUTR

import scala.concurrent.Future

class EnterUTRControllerSpec extends SpecBase {

  import EnterUTRControllerSpec._

  val viewModel = CommonFormWithHintViewModel(
    postCall = Call("GET", "www.example.com"),
    title = "title",
    heading = "heading",
    mode = NormalMode,
    entityName = "company name"
  )

  "get" must {

    "return a successful result when there is no existing answer" in {
      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[EnterUTRFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewModel, UserAnswers())

          status(result) mustEqual OK
          contentAsString(result) mustEqual enterUTR(frontendAppConfig, formProvider(entityName), viewModel)(FakeRequest(), messages).toString
      }
    }

    "return a successful result when there is an existing answer" in {
      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[EnterUTRFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onPageLoad(viewModel, UserAnswers().set(FakeIdentifier)(testUTR).get)

          status(result) mustEqual OK
          contentAsString(result) mustEqual enterUTR(
            frontendAppConfig,
            formProvider(entityName).fill(testUTR),
            viewModel
          )(FakeRequest(), messages).toString
      }
    }
  }

  "post" must {

    "return a redirect when the submitted data is valid" in {
      running(_.overrides(
        bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val request = FakeRequest().withFormUrlEncodedBody(
            ("value", utr)
          )
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewModel, UserAnswers(), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "www.example.com"
      }
    }

    "return a bad request when the submitted data is invalid" in {
      running(_.overrides(
        bind[Navigator].toInstance(FakeNavigator)
      )) {
        app =>
          val formProvider = app.injector.instanceOf[EnterUTRFormProvider]
          val controller = app.injector.instanceOf[TestController]
          val result = controller.onSubmit(viewModel, UserAnswers(), FakeRequest())

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual enterUTR(
            frontendAppConfig,
            formProvider(entityName).bind(Map.empty[String, String]),
            viewModel
          )(FakeRequest(), messages).toString
      }
    }
  }
}

object EnterUTRControllerSpec {
  private val entityName = "entity name"
  private val utr = "1234567890"
  private val testUTR = ReferenceValue(utr)

  object FakeIdentifier extends TypedIdentifier[ReferenceValue]

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val cacheConnector: UserAnswersCacheConnector,
                                  override val navigator: Navigator,
                                  formProvider: EnterUTRFormProvider
                                ) extends EnterUTRController {

    def onPageLoad(viewmodel: CommonFormWithHintViewModel, answers: UserAnswers): Future[Result] = {
      get(FakeIdentifier, formProvider(entityName), viewmodel)(DataRequest(FakeRequest(), "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))
    }

    def onSubmit(viewmodel: CommonFormWithHintViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(FakeIdentifier, NormalMode, formProvider(entityName), viewmodel)(DataRequest(fakeRequest, "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))
    }
  }

}







