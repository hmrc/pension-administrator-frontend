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

package controllers.register.partnership

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerWithCommonBehaviour
import forms.EmailFormProvider
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.email

class PartnershipEmailControllerSpec extends ControllerWithCommonBehaviour {
  import PartnershipEmailControllerSpec._

  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private def controller(dataRetrievalAction: DataRetrievalAction) = new PartnershipEmailController(
    new FakeNavigator(onwardRoute), frontendAppConfig, FakeUserAnswersCacheConnector, FakeAuthAction, FakeAllowAccessProvider(),
    dataRetrievalAction, new DataRequiredActionImpl, formProvider,
    stubMessagesControllerComponents(), view)

  private def emailView(form: Form[_] = emailForm): String = view(form, viewModel(NormalMode))(fakeRequest, messages).toString

  "PartnershipEmail Controller" must {

    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode),
      onSubmitAction = data => controller(data).onSubmit(NormalMode),
      validData = getPartnership,
      viewAsString = emailView,
      form = emailForm,
      request = postRequest
    )
  }
}

object PartnershipEmailControllerSpec extends ControllerSpecBase {
  private val formProvider = new EmailFormProvider()
  private val emailForm = formProvider()
  private val partnershipName = "Test Partnership Name"
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "test@test.com"))

  val view: email = app.injector.instanceOf[email]

  private def viewModel(mode: Mode)(implicit messages: Messages) =
    CommonFormWithHintViewModel(
      postCall = routes.PartnershipEmailController.onSubmit(mode),
      title = Message("email.title", Message("thePartnership").resolve),
      heading = Message("email.title", partnershipName),
      mode = mode,
      entityName = partnershipName
    )
}

