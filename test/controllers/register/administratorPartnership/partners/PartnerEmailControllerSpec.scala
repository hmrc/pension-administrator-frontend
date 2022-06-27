/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.register.administratorPartnership.partners

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import controllers.behaviours.ControllerWithCommonBehaviour
import forms.EmailFormProvider
import models.{Index, Mode, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.email

class PartnerEmailControllerSpec extends ControllerWithCommonBehaviour {
 import PartnerEmailControllerSpec._

  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private def controller(dataRetrievalAction: DataRetrievalAction) = new PartnerEmailController(
    new FakeNavigator(onwardRoute), frontendAppConfig, FakeUserAnswersCacheConnector, FakeAuthAction, FakeAllowAccessProvider(config = frontendAppConfig),
    dataRetrievalAction, new DataRequiredActionImpl, formProvider,
    controllerComponents, view
  )

  val view: email = app.injector.instanceOf[email]

  private def emailView(form: Form[_]): String = view(form, viewModel(NormalMode, index), Some(psaName))(fakeRequest, messages).toString

  "PartnerEmail Controller" must {

    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode, index),
      onSubmitAction = data => controller(data).onSubmit(NormalMode, index),
      validData = getPartnershipPartner,
      viewAsString = emailView,
      form = emailForm,
      request = postRequest
    )
  }

}

object PartnerEmailControllerSpec {
  private val formProvider = new EmailFormProvider()
  private val emailForm = formProvider()
  private val index = 0
  private val partnerName = "test first name test last name"
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "test@test.com"))
  private val psaName = "Test Partnership Name"

  private def viewModel(mode: Mode, index: Index) =
    CommonFormWithHintViewModel(
      postCall = routes.PartnerEmailController.onSubmit(mode, index),
      title = Message("email.title", Message("thePartner")),
      heading = Message("email.title", partnerName),
      mode = mode,
      entityName = psaName,
      returnLink = Some(controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad().url)
    )
}


