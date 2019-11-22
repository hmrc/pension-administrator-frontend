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

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions.{DataRetrievalAction, _}
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.register.adviser.ConfirmDeleteAdviserFormProvider
import identifiers.register.DeclarationChangedId
import identifiers.register.adviser.ConfirmDeleteAdviserId
import models.{NormalMode, UpdateMode}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{ConfirmDeleteViewModel, Message}
import views.html.confirmDelete

class ConfirmDeleteAdviserControllerSpec extends ControllerWithQuestionPageBehaviours {

  private val adviserName = "test adviser"
  val view: confirmDelete = app.injector.instanceOf[confirmDelete]
  val formProvider = new ConfirmDeleteAdviserFormProvider()
  val form: Form[Boolean] = formProvider(adviserName)
  private val validData = UserAnswers().adviserName(adviserName).dataRetrievalAction

  private def viewModel(name: String) = ConfirmDeleteViewModel(
    routes.ConfirmDeleteAdviserController.onSubmit(),
    controllers.routes.PsaDetailsController.onPageLoad(),
    Message("confirmDelete.adviser.title"),
    "confirmDelete.adviser.heading",
    Some(name),
    None
  )

  def controller(dataRetrievalAction: DataRetrievalAction = validData) =
    new ConfirmDeleteAdviserController(frontendAppConfig, FakeAuthAction, FakeAllowAccessProvider(),
      dataRetrievalAction, new DataRequiredActionImpl, FakeUserAnswersCacheConnector, formProvider, new FakeNavigator(desiredRoute = onwardRoute),
      stubMessagesControllerComponents(), view)

  def viewAsString(form: Form[_] = form): String = view(form, viewModel(adviserName), NormalMode)(fakeRequest, messages).toString

  "ConfirmDeleteAdviserController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to AlreadyDeleted for a GET if no existing data is found" in {
      val result = controller(getEmptyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.register.adviser.routes.AdviserAlreadyDeletedController.onPageLoad().url)
    }

    "don't remove the adviser information, don't set the change flag when in NormalMode and user answers no, to confirm delete" in {
      FakeUserAnswersCacheConnector.reset()
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersCacheConnector.verifyNot(DeclarationChangedId)
      FakeUserAnswersCacheConnector.lastUpsert must be(None)
    }

    "set the change flag, remove all the adviser information when in Update mode and user answers yes, to confirm delete" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(UpdateMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersCacheConnector.verify(DeclarationChangedId, true)
      FakeUserAnswersCacheConnector.lastUpsert.get mustEqual Json.obj(ConfirmDeleteAdviserId.toString -> true)
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

