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

package controllers.register.company.directors

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions._
import controllers.behaviours.ControllerWithCommonBehaviour
import forms.EmailFormProvider
import models.{Index, Mode, NormalMode}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.email

class DirectorEmailControllerSpec extends ControllerWithCommonBehaviour {

  override val onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val view: email = app.injector.instanceOf[email]
  private val formProvider = new EmailFormProvider()
  private val emailForm = formProvider()
  private val index = 0
  private val directorName = "test first name test last name"
  private val postRequest = FakeRequest().withFormUrlEncodedBody(("value", "test@test.com"))

  private def controller(dataRetrievalAction: DataRetrievalAction) = new DirectorEmailController(
    new FakeNavigator(onwardRoute),
    FakeUserAnswersCacheConnector,
    FakeAuthAction,
    FakeAllowAccessProvider(config = frontendAppConfig),
    dataRetrievalAction,
    new DataRequiredActionImpl,
    formProvider,
    controllerComponents,
    view
  )

  private def emailView(form: Form[?]): String = view(form, viewModel(NormalMode, index), None)(fakeRequest, messages).toString

  "DirectorEmail Controller" must {

    behave like controllerWithCommonFunctions(
      onPageLoadAction = data => controller(data).onPageLoad(NormalMode, index),
      onSubmitAction = data => controller(data).onSubmit(NormalMode, index),
      validData = getDirector,
      viewAsString = emailView,
      form = emailForm,
      request = postRequest
    )
  }

  private def viewModel(mode: Mode, index: Index) =
    CommonFormWithHintViewModel(
      postCall = routes.DirectorEmailController.onSubmit(mode, index),
      title = Message("email.title", Message("theDirector")),
      heading = Message("email.title", directorName),
      mode = mode,
      entityName = companyName,
      returnLink = Some(controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad().url)
    )
}
