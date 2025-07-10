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

package controllers.register.administratorPartnership.contactDetails

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerWithCommonBehaviour
import forms.EmailFormProvider
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.email

class PartnershipEmailControllerSpec extends ControllerWithCommonBehaviour {
  private val formProvider = new EmailFormProvider()
  private val emailForm = formProvider()
  private val partnershipName = "Test Partnership Name"
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "test@test.com"))

  val view: email = app.injector.instanceOf[email]


  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private def controller(dataRetrievalAction: DataRetrievalAction) = new PartnershipEmailController(
    new FakeNavigator(onwardRoute), FakeUserAnswersCacheConnector, FakeAuthAction, FakeAllowAccessProvider(config = frontendAppConfig),
    dataRetrievalAction, new DataRequiredActionImpl, formProvider,
    controllerComponents, view)

  private def emailView(form: Form[?]): String = view(form, viewModel(NormalMode), None)(fakeRequest, messages).toString

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

  private def viewModel(mode: Mode) =
    CommonFormWithHintViewModel(
      postCall = routes.PartnershipEmailController.onSubmit(mode),
      title = Message("email.title", Message("thePartnership")),
      heading = Message("email.title", partnershipName),
      mode = mode,
      entityName = partnershipName,
      returnLink = Some(controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad().url)
    )
}

