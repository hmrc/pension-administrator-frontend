/*
 * Copyright 2020 HM Revenue & Customs
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
import forms.HasReferenceNumberFormProvider
import models.{Index, Mode, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

class HasPartnerUTRControllerSpec extends ControllerWithCommonBehaviour {
  import HasPartnerUTRControllerSpec._

  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()
  private val hasReferenceNumberForm = formProvider("error.required", partnerName)

  private def controller(dataRetrievalAction: DataRetrievalAction) = new HasPartnerUTRController(
    frontendAppConfig, FakeUserAnswersCacheConnector, new FakeNavigator(onwardRoute), FakeAuthAction, FakeAllowAccessProvider(config = frontendAppConfig),
    dataRetrievalAction, new DataRequiredActionImpl, formProvider,
    stubMessagesControllerComponents(),
    view)

  val view: hasReferenceNumber = app.injector.instanceOf[hasReferenceNumber]

  private def hasReferenceNumberView(form: Form[_]): String =
    view(form, viewModel(NormalMode, index))(fakeRequest, messages).toString

  "HasPartnerUTRController" must {

    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode, index),
      onSubmitAction = data => controller(data).onSubmit(NormalMode, index),
      validData = getPartner,
      viewAsString = hasReferenceNumberView,
      form = hasReferenceNumberForm,
      request = postRequest
    )
  }

}
object HasPartnerUTRControllerSpec {
  private val partnerName = "test first name test last name"
  private val formProvider = new HasReferenceNumberFormProvider()
  private val index = 0
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "true"))

  private def viewModel(mode: Mode, index: Index) =
    CommonFormWithHintViewModel(
      postCall = routes.HasPartnerUTRController.onSubmit(mode, index),
      title = Message("hasUTR.heading", Message("thePartner")),
      heading = Message("hasUTR.heading", partnerName),
      hint = Some(Message("utr.p1")),
      mode = mode,
      entityName = partnerName
    )
}


