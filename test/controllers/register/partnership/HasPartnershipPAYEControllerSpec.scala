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

package controllers.register.partnership

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerWithCommonBehaviour
import forms.HasReferenceNumberFormProvider
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import utils.FakeRequest
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.hasReferenceNumber

class HasPartnershipPAYEControllerSpec extends ControllerWithCommonBehaviour {

  import HasPartnershipPAYEControllerSpec._

  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad
  private val hasReferenceNumberForm = formProvider("error.required", partnershipName)

  val view: hasReferenceNumber = app.injector.instanceOf[hasReferenceNumber]

  private def controller(dataRetrievalAction: DataRetrievalAction) = new HasPartnershipPAYEController(
    frontendAppConfig, FakeUserAnswersCacheConnector, new FakeNavigator(onwardRoute), FakeAuthAction, FakeAllowAccessProvider(config = frontendAppConfig),
    dataRetrievalAction, new DataRequiredActionImpl, formProvider,
    controllerComponents, view)

  private def hasReferenceNumberView(form: Form[_]): String =
    view(form, viewModel(NormalMode))(fakeRequest, messages).toString

  "HasPartnershipPAYEController" must {

    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode),
      onSubmitAction = data => controller(data).onSubmit(NormalMode),
      validData = getPartnership,
      viewAsString = hasReferenceNumberView,
      form = hasReferenceNumberForm,
      request = postRequest
    )
  }

}

object HasPartnershipPAYEControllerSpec {
  private val partnershipName = "Test Partnership Name"
  private val formProvider = new HasReferenceNumberFormProvider()
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "true"))

  private def viewModel(mode: Mode) =
    CommonFormWithHintViewModel(
      postCall = routes.HasPartnershipPAYEController.onSubmit(mode),
      title = Message("hasPAYE.heading", Message("thePartnership")),
      heading = Message("hasPAYE.heading", partnershipName),
      hint = Some(Message("hasPAYE.hint")),
      mode = mode,
      entityName = partnershipName
    )
}
