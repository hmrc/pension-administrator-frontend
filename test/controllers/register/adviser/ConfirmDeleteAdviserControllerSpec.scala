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

package controllers.register.adviser

import connectors.FakeUserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, _}
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.ConfirmDeleteAdviserFormProvider
import identifiers.register.DeclarationChangedId
import models.{NormalMode, UpdateMode}
import play.api.data.Form
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{ConfirmDeleteViewModel, Message}
import views.html.confirmDelete

class ConfirmDeleteAdviserControllerSpec extends ControllerWithQuestionPageBehaviours {

  private val adviserName = "test adviser"

  val formProvider = new ConfirmDeleteAdviserFormProvider(messagesApi)
  val form = formProvider(adviserName)
  val validData = UserAnswers().adviserName(adviserName).dataRetrievalAction

  private def viewModel(name: String) = ConfirmDeleteViewModel(
    routes.ConfirmDeleteAdviserController.onSubmit(),
    controllers.routes.PsaDetailsController.onPageLoad(),
    Message("confirmDelete.adviser.title"),
    "confirmDelete.adviser.heading",
    Some(name),
    None
  )

  def controller(dataRetrievalAction: DataRetrievalAction = validData) =
    new ConfirmDeleteAdviserController(frontendAppConfig, messagesApi, FakeAuthAction, new FakeAllowAccessProvider(),
      dataRetrievalAction, new DataRequiredActionImpl, FakeUserAnswersCacheConnector, formProvider, new FakeNavigator(desiredRoute = onwardRoute))

  def viewAsString(form: Form[_] = form): String = confirmDelete(frontendAppConfig, form, viewModel(adviserName))(fakeRequest, messages).toString

  "ConfirmDeleteAdviserController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page when valid data is submitted" in {
      FakeUserAnswersCacheConnector.reset()
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersCacheConnector.verifyNot(DeclarationChangedId)
    }

    "set the change flag when in Update mode and user answers yes, he wants to delete the adviser" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(UpdateMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersCacheConnector.verify(DeclarationChangedId, true)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid data"))
      val boundForm = form.bind(Map("value" -> "invalid data"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid data"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
