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

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.register.EnterPAYEController
import forms.EnterPAYEFormProvider
import identifiers.TypedIdentifier
import models._
import models.requests.DataRequest
import play.api.i18n.MessagesApi
import play.api.inject._
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.enterPAYE

import scala.concurrent.{ExecutionContext, Future}

class EnterPAYEControllerSpec extends ControllerSpecBase {

  import EnterPAYEControllerSpec._

  "EnterPAYE Controller" must {
    "get" must {

      "return a successful resultCompany when there is no existing answer" in {
        running(_.overrides(
          bind[Navigator].toInstance(FakeNavigator)
        )) {
          app =>
            val controller = app.injector.instanceOf[TestController]
            val result = controller.onPageLoad(viewModel, UserAnswers())

            status(result) mustEqual OK
            contentAsString(result) mustEqual enterPAYE(frontendAppConfig, form, viewModel)(fakeRequest, messages).toString
        }
      }

      "return a successful resultCompany when there is an existing answer" in {
        running(_.overrides(
          bind[Navigator].toInstance(FakeNavigator)
        )) {
          app =>
            val controller = app.injector.instanceOf[TestController]
            val answers = UserAnswers().set(FakeIdentifier)(payeNumber).get
            val result = controller.onPageLoad(viewModel, answers)

            status(result) mustEqual OK
            contentAsString(result) mustEqual enterPAYE(frontendAppConfig, form.fill(payeNumber), viewModel)(fakeRequest, messages).toString
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
            val request = FakeRequest().withFormUrlEncodedBody(("value", payeNumber))
            val controller = app.injector.instanceOf[TestController]
            val result = controller.onSubmit(viewModel, UserAnswers(), request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual "www.example.com"
            FakeUserAnswersCacheConnector.verify(FakeIdentifier, payeNumber)
        }
      }

      "return a bad request when the submitted data is invalid" in {
        running(_.overrides(
          bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
          bind[Navigator].toInstance(FakeNavigator)
        )) {
          app =>
            val controller = app.injector.instanceOf[TestController]
            val request = FakeRequest().withFormUrlEncodedBody(("value", "invalid"))
            val result = controller.onSubmit(viewModel, UserAnswers(), request)

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual enterPAYE(frontendAppConfig, form.bind(Map("value" -> "invalid")), viewModel)(request, messages).toString
        }
      }
    }
  }
}

object EnterPAYEControllerSpec extends ControllerSpecBase {

  object FakeIdentifier extends TypedIdentifier[String]

  private val companyName = "Test Company Name"
  private val formProvider = new EnterPAYEFormProvider()
  private val form = formProvider(companyName)

  private def viewModel: CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = routes.SessionExpiredController.onPageLoad(),
      title = Message("enterPAYE.heading", Message("theCompany").resolve),
      heading = Message("enterPAYE.heading", companyName),
      mode = NormalMode,
      entityName = companyName
    )

  private val payeNumber = "123AB456"

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val cacheConnector: UserAnswersCacheConnector,
                                  override val navigator: Navigator,
                                  formProvider: EnterPAYEFormProvider
                                )(implicit val ec: ExecutionContext) extends EnterPAYEController {

    def onPageLoad(viewModel: CommonFormWithHintViewModel, answers: UserAnswers): Future[Result] = {
      get(FakeIdentifier, form, viewModel)(DataRequest(FakeRequest(), "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))
    }

    def onSubmit(viewModel: CommonFormWithHintViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(FakeIdentifier, NormalMode, form, viewModel)(DataRequest(fakeRequest, "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))
    }
  }

}
