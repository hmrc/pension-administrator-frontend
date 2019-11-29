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

package controllers.register.adviser

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerWithCommonBehaviour
import forms.EmailFormProvider
import models.{Index, Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.email

class AdviserEmailControllerSpec extends ControllerWithCommonBehaviour {
  import AdviserEmailControllerSpec._

  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()
  val view: email = app.injector.instanceOf[email]
  private def controller(dataRetrievalAction: DataRetrievalAction) = new AdviserEmailController(
    new FakeNavigator(onwardRoute), frontendAppConfig, FakeUserAnswersCacheConnector, FakeAuthAction, FakeAllowAccessProvider(),
    dataRetrievalAction, new DataRequiredActionImpl, formProvider, stubMessagesControllerComponents(), view)

  private def emailView(form: Form[_] = emailForm): String = view(form, viewModel(NormalMode, index))(fakeRequest, messages).toString

  "AdviserEmail Controller" must {

    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode),
      onSubmitAction = data => controller(data).onSubmit(NormalMode),
      validData = getAdviser,
      viewAsString = emailView,
      form = emailForm,
      request = postRequest
    )
  }

}


object AdviserEmailControllerSpec {
  private val formProvider = new EmailFormProvider()
  private val emailForm = formProvider()
  private val index = 0
  private val adviserName = "Test Adviser Name"
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "test@test.com"))

  private def viewModel(mode: Mode, index: Index)(implicit messages: Messages) =
    CommonFormWithHintViewModel(
      postCall = routes.AdviserEmailController.onSubmit(mode),
      title = Message("email.title", Message("theAdviser").resolve),
      heading = Message("email.title", adviserName),
      mode = mode,
      entityName = adviserName
    )
}




