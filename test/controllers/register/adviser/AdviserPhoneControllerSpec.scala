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

package controllers.register.adviser

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions._
import controllers.behaviours.ControllerWithCommonBehaviour
import forms.PhoneFormProvider
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.phone

class AdviserPhoneControllerSpec extends ControllerWithCommonBehaviour {

  import AdviserPhoneControllerSpec._
  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val view: phone = app.injector.instanceOf[phone]

  private def controller(dataRetrievalAction: DataRetrievalAction) = new AdviserPhoneController(
    new FakeNavigator(onwardRoute),
    frontendAppConfig,
    FakeUserAnswersCacheConnector,
    FakeAuthAction,
    FakeAllowAccessProvider(config = frontendAppConfig),
    dataRetrievalAction,
    new DataRequiredActionImpl,
    formProvider,
    controllerComponents,
    view
  )

  private def phoneView(form: Form[_]): String = view(form, viewModel(NormalMode), None)(fakeRequest, messages).toString

  "AdviserPhoneController" must {

    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode),
      onSubmitAction = data => controller(data).onSubmit(NormalMode),
      validData = getAdviser,
      viewAsString = phoneView,
      form = phoneForm,
      request = postRequest
    )
  }
}

object AdviserPhoneControllerSpec {
  private val formProvider = new PhoneFormProvider()
  private val phoneForm = formProvider()
  private val adviserName = "Test Adviser Name"
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "12345"))

  private def viewModel(mode: Mode) =
    CommonFormWithHintViewModel(
      postCall = routes.AdviserPhoneController.onSubmit(mode),
      title = Message("phone.title", Message("theAdviser")),
      heading = Message("phone.title", adviserName),
      mode = mode,
      entityName = adviserName,
      returnLink = Some(controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad().url)
    )
}




