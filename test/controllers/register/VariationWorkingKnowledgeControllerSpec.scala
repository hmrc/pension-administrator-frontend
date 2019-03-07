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

package controllers.register

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.VariationWorkingKnowledgeFormProvider
import identifiers.register.individual.IndividualDetailsId
import identifiers.register.{DeclarationChangedId, VariationWorkingKnowledgeId}
import models.{TolerantIndividual, UpdateMode, UserType}
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import views.html.register.variationWorkingKnowledge

class VariationWorkingKnowledgeControllerSpec extends ControllerSpecBase {

  def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new VariationWorkingKnowledgeFormProvider()
  val form = formProvider()

  private val individual = UserAnswers(Json.obj())
    .set(IndividualDetailsId)(TolerantIndividual(Some("Mark"), None, Some("Wright"))).asOpt.value

  private val dataRetrievalAction = new FakeDataRetrievalAction(Some(individual.json))

  val existingData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(individual.set(VariationWorkingKnowledgeId)(true).asOpt.value.json))

  def controller(dataRetrievalAction: DataRetrievalAction = dataRetrievalAction) =
    new VariationWorkingKnowledgeController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction(UserType.Individual),
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  def viewAsString(form: Form[_] = form) = variationWorkingKnowledge(frontendAppConfig, form, "Mark Wright", UpdateMode)(fakeRequest, messages).toString

  "VariationWorkingKnowledge Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = individual.set(VariationWorkingKnowledgeId)(true).asOpt.value
      val getRelevantData = new FakeDataRetrievalAction(Some(validData.json))

      val result = controller(getRelevantData).onPageLoad(UpdateMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(true))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(UpdateMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(UpdateMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(dontGetAnyData).onSubmit(UpdateMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page and update the change ID when data has changed" in {
      FakeUserAnswersCacheConnector.reset()
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))
      val result = controller(existingData).onSubmit(UpdateMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersCacheConnector.verify(DeclarationChangedId, true)
    }

    "redirect to the next page but not update the change ID when data has not changed" in {
      FakeUserAnswersCacheConnector.reset()
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(existingData).onSubmit(UpdateMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersCacheConnector.verifyNot(DeclarationChangedId)
    }
  }
}
