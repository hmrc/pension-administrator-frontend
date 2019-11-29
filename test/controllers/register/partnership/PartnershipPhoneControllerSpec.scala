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
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerWithCommonBehaviour
import forms.PhoneFormProvider
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.phone

class PartnershipPhoneControllerSpec extends ControllerWithCommonBehaviour {

  import PartnershipPhoneControllerSpec._

  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val view: phone = app.injector.instanceOf[phone]

  private def controller(dataRetrievalAction: DataRetrievalAction) = new PartnershipPhoneController(
    new FakeNavigator(onwardRoute), frontendAppConfig, FakeUserAnswersCacheConnector, FakeAuthAction, FakeAllowAccessProvider(),
    dataRetrievalAction, new DataRequiredActionImpl, formProvider,
    stubMessagesControllerComponents(), view)

  private def phoneView(form: Form[_] = phoneForm): String = view(form, viewModel(NormalMode))(fakeRequest, messages).toString

  "PartnershipPhoneController" must {

    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode),
      onSubmitAction = data => controller(data).onSubmit(NormalMode),
      validData = getPartnership,
      viewAsString = phoneView,
      form = phoneForm,
      request = postRequest
    )
  }
}

object PartnershipPhoneControllerSpec {
  private val formProvider = new PhoneFormProvider()
  private val phoneForm = formProvider()
  private val partnershipName = "Test Partnership Name"
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "12345"))

  private def viewModel(mode: Mode)(implicit messages: Messages) =
    CommonFormWithHintViewModel(
      postCall = routes.PartnershipPhoneController.onSubmit(mode),
      title = Message("phone.title", Message("thePartnership").resolve),
      heading = Message("phone.title", partnershipName),
      mode = mode,
      entityName = partnershipName
    )
}

