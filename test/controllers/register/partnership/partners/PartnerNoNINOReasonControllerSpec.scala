/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.behaviours.NoNINOReasonControllerWithCommonBehaviour
import forms.NINOReasonFormProvider
import models.{Index, Mode, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.reason

class PartnerNoNINOReasonControllerSpec extends NoNINOReasonControllerWithCommonBehaviour {
 import PartnerNoNINOReasonControllerSpec._
  
  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad
  private val reasonForm = formProvider(partnerName)
  
  private def controller(dataRetrievalAction: DataRetrievalAction) = new PartnerNoNINOReasonController(
    new FakeNavigator(onwardRoute), FakeUserAnswersCacheConnector, FakeAuthAction, FakeAllowAccessProvider(config = frontendAppConfig),
    dataRetrievalAction, new DataRequiredActionImpl, formProvider,
    controllerComponents, view
  )

  val view: reason = app.injector.instanceOf[reason]

  private def reasonView(form: Form[?]): String =view(form, viewModel(NormalMode, index))(fakeRequest, messages).toString

  "PartnerNoNINOReasonController" must {

    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode, index),
      onSubmitAction = data => controller(data).onSubmit(NormalMode, index),
      validData = getPartner,
      viewAsString = reasonView,
      form = reasonForm,
      request = postRequest
    )
  }

}

object PartnerNoNINOReasonControllerSpec {
  private val formProvider = new NINOReasonFormProvider()
  private val index = 0
  private val partnerName = "test first name test last name"
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "test reason"))

  private def viewModel(mode: Mode, index: Index) =
    CommonFormWithHintViewModel(
      postCall = routes.PartnerNoNINOReasonController.onSubmit(mode, index),
      title = Message("whyNoNINO.heading", Message("thePartner")),
      heading = Message("whyNoNINO.heading", partnerName),
      mode = mode,
      entityName = partnerName
    )
}






