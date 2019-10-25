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
import forms.ReasonFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{NormalMode, PSAUser, UserType}
import play.api.i18n.MessagesApi
import play.api.inject._
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.CommonFormWithHintViewModel
import views.html.reason

import scala.concurrent.{ExecutionContext, Future}

class ReasonControllerSpec extends ControllerSpecBase {

  import ReasonControllerSpec._

  "ReasonController" must {
    "get" must {

      "return a successful result when there is no existing answer" in {
        running(_.overrides(
          bind[Navigator].toInstance(FakeNavigator)
        )) {
          app =>
            val controller = app.injector.instanceOf[TestController]
            val result = controller.onPageLoad(viewModel, UserAnswers())

            status(result) mustEqual OK
            contentAsString(result) mustEqual reason(frontendAppConfig, form, viewModel)(fakeRequest, messages).toString
        }
      }

      "return a successful result when there is an existing answer" in {
        running(_.overrides(
          bind[Navigator].toInstance(FakeNavigator)
        )) {
          app =>
            val controller = app.injector.instanceOf[TestController]
            val answers = UserAnswers().set(FakeIdentifier)(testReason).get
            val result = controller.onPageLoad(viewModel, answers)

            status(result) mustEqual OK
            contentAsString(result) mustEqual reason(frontendAppConfig, form.fill(testReason), viewModel)(fakeRequest, messages).toString
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
            val request = FakeRequest().withFormUrlEncodedBody(("value", testReason))
            val controller = app.injector.instanceOf[TestController]
            val result = controller.onSubmit(viewModel, UserAnswers(), request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual "www.example.com"
            FakeUserAnswersCacheConnector.verify(FakeIdentifier, testReason)
        }
      }

      "return a bad request when the submitted data is invalid" in {
        running(_.overrides(
          bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
          bind[Navigator].toInstance(FakeNavigator)
        )) {
          app =>
            val controller = app.injector.instanceOf[TestController]
            val request = FakeRequest().withFormUrlEncodedBody(("value", "{invalid}"))
            val result = controller.onSubmit(viewModel, UserAnswers(), request)

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual reason(frontendAppConfig, form.bind(Map("value" -> "{invalid}")), viewModel)(request, messages).toString
        }
      }
    }
  }
}

object ReasonControllerSpec extends ControllerSpecBase {

  object FakeIdentifier extends TypedIdentifier[String]

  private val entityName = "entity name"
  private val formProvider = new ReasonFormProvider()
  private val form = formProvider(entityName)
  private val testReason = "test reason"

  private def viewModel: CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = routes.IndexController.onPageLoad(),
      title = "title",
      heading = "heading",
      mode = NormalMode,
      entityName = entityName
    )

  class TestController @Inject()(
                                  override val appConfig: FrontendAppConfig,
                                  override val messagesApi: MessagesApi,
                                  override val dataCacheConnector: UserAnswersCacheConnector,
                                  override val navigator: Navigator,
                                  formProvider: ReasonFormProvider
                                )(implicit val ec: ExecutionContext) extends ReasonController {

    def onPageLoad(viewModel: CommonFormWithHintViewModel, answers: UserAnswers): Future[Result] = {
      get(FakeIdentifier, viewModel, form)(DataRequest(FakeRequest(), "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))
    }

    def onSubmit(viewModel: CommonFormWithHintViewModel, answers: UserAnswers, fakeRequest: Request[AnyContent]): Future[Result] = {
      post(FakeIdentifier, NormalMode, viewModel, form)(DataRequest(fakeRequest, "cacheId",
        PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers))
    }
  }

}


