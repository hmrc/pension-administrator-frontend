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

package controllers.register.company

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions._
import controllers.behaviours.ControllerWithCommonBehaviour
import forms.EmailFormProvider
import models.FeatureToggle.Enabled
import models.FeatureToggleName.PsaRegistration
import models.{Mode, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.email

class CompanyEmailControllerSpec extends ControllerWithCommonBehaviour {

  private val formProvider = new EmailFormProvider()
  private val emailForm = formProvider()
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "test@test.com"))

  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()
  val view: email = app.injector.instanceOf[email]

  private def controller(dataRetrievalAction: DataRetrievalAction) = new CompanyEmailController(
    new FakeNavigator(onwardRoute),
    new FakeNavigator(onwardRoute),
    frontendAppConfig,
    FakeUserAnswersCacheConnector,
    FakeAuthAction,
    FakeAllowAccessProvider(config = frontendAppConfig),
    dataRetrievalAction,
    new DataRequiredActionImpl,
    formProvider,
    controllerComponents,
    view,
    FakeFeatureToggleConnector.returns(Enabled(PsaRegistration))
  )

  private def emailView(form: Form[_]): String = view(form, viewModel(NormalMode), Some(companyName))(fakeRequest, messages).toString

  "CompanyEmail Controller" must {

    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode),
      onSubmitAction = data => controller(data).onSubmit(NormalMode),
      validData = getCompany,
      viewAsString = emailView,
      form = emailForm,
      request = postRequest
    )
  }

  private def viewModel(mode: Mode) =
    CommonFormWithHintViewModel(
      postCall = routes.CompanyEmailController.onSubmit(mode),
      title = Message("email.title", Message("theCompany")),
      heading = Message("email.title", companyName),
      mode = mode,
      entityName = companyName,
      returnLink = Some(controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad().url)
    )
}
