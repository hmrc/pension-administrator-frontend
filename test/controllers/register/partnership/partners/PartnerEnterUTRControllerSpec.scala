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

package controllers.register.partnership.partners

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerWithCommonBehaviour
import forms.EnterUTRFormProvider
import models.{Index, Mode, NormalMode}
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.enterUTR

class PartnerEnterUTRControllerSpec extends ControllerWithCommonBehaviour {

  import PartnerEnterUTRControllerSpec._
  
  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()
  private val utrForm = formProvider(partnerName)

  private def controller(dataRetrievalAction: DataRetrievalAction) = new PartnerEnterUTRController(
    new FakeNavigator(onwardRoute), frontendAppConfig, FakeUserAnswersCacheConnector, FakeAuthAction, FakeAllowAccessProvider(),
    dataRetrievalAction, new DataRequiredActionImpl, formProvider,
    stubMessagesControllerComponents(), view
  )

  val view: enterUTR = app.injector.instanceOf[enterUTR]

  private def enterUTRView(form: Form[_] = utrForm): String = view(form, viewModel(NormalMode, index))(fakeRequest, messages).toString

  "PartnerEnterUTRController" must {

    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode, index),
      onSubmitAction = data => controller(data).onSubmit(NormalMode, index),
      validData = getPartner,
      viewAsString = enterUTRView,
      form = utrForm,
      request = postRequest
    )
  }

}

object PartnerEnterUTRControllerSpec {
  private val formProvider = new EnterUTRFormProvider()
  private val index = 0
  private val partnerName = "test first name test last name"
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "1111111111"))

  private def viewModel(mode: Mode, index: Index)(implicit messages: Messages) =
    CommonFormWithHintViewModel(
      postCall = routes.PartnerEnterUTRController.onSubmit(mode, index),
      title = Message("enterUTR.heading", Message("thePartner").resolve),
      heading = Message("enterUTR.heading", partnerName),
      mode = mode,
      entityName = partnerName
    )
}





