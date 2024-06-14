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

package controllers.register.administratorPartnership.partnershipDetails

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerWithCommonBehaviour
import forms.EnterPAYEFormProvider
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.enterPAYE

class PartnershipEnterPAYEControllerSpec extends ControllerWithCommonBehaviour {

  import PartnershipEnterPAYEControllerSpec._

  val view: enterPAYE = app.injector.instanceOf[enterPAYE]

  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad
  private val payeForm = formProvider(partnershipName)

  private def controller(dataRetrievalAction: DataRetrievalAction) = new PartnershipEnterPAYEController(
    frontendAppConfig, FakeUserAnswersCacheConnector, new FakeNavigator(onwardRoute), FakeAuthAction, FakeAllowAccessProvider(config = frontendAppConfig),
    dataRetrievalAction, new DataRequiredActionImpl, formProvider,
    controllerComponents, view)

  private def enterPAYEView(form: Form[_]): String = view(form, viewModel(NormalMode))(fakeRequest, messages).toString

  "PartnershipEnterPAYEController" must {

    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode),
      onSubmitAction = data => controller(data).onSubmit(NormalMode),
      validData = getPartnership,
      viewAsString = enterPAYEView,
      form = payeForm,
      request = postRequest
    )
  }
}

object PartnershipEnterPAYEControllerSpec {
  private val formProvider = new EnterPAYEFormProvider()
  private val partnershipName = "Test Partnership Name"
  private val payeNumber = "123AB456"
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", payeNumber))

  private def viewModel(mode: Mode) =
    CommonFormWithHintViewModel(
      postCall = routes.PartnershipEnterPAYEController.onSubmit(mode),
      title = Message("enterPAYE.heading", Message("thePartnership")),
      heading = Message("enterPAYE.heading", partnershipName),
      mode = mode,
      hint = Some(Message("enterPAYE.hint")),
      entityName = partnershipName,
      returnLink = Some(controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad().url)
    )
}