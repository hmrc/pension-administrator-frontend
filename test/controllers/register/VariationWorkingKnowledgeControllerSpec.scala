/*
 * Copyright 2020 HM Revenue & Customs
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

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.VariationWorkingKnowledgeFormProvider
import identifiers.register.adviser.IsNewAdviserId
import identifiers.register.individual.IndividualDetailsId
import identifiers.register.{DeclarationChangedId, PAInDeclarationJourneyId, VariationWorkingKnowledgeId}
import models.{CheckUpdateMode, TolerantIndividual, UpdateMode, UserType}
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{FakeNavigator, UserAnswers}
import views.html.register.variationWorkingKnowledge

class VariationWorkingKnowledgeControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new VariationWorkingKnowledgeFormProvider()
  val form: Form[Boolean] = formProvider()

  private val individual = UserAnswers(Json.obj())
    .set(IndividualDetailsId)(TolerantIndividual(Some("Mark"), None, Some("Wright"))).asOpt.value

  private val dataRetrievalAction = new FakeDataRetrievalAction(Some(individual.json))

  val existingData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(individual.set(VariationWorkingKnowledgeId)(true).asOpt.value.json))

  val view: variationWorkingKnowledge = app.injector.instanceOf[variationWorkingKnowledge]

  def controller(dataRetrievalAction: DataRetrievalAction = dataRetrievalAction) =
    new VariationWorkingKnowledgeController(
      frontendAppConfig,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction(UserType.Individual),
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      stubMessagesControllerComponents(),
      view
    )

  def viewAsString(form: Form[_] = form): String = view(form, None, UpdateMode)(fakeRequest, messages).toString

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

    "redirect to the next page when valid data is submitted for check update mode and set PAInDeclarationJourneyId" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      FakeUserAnswersCacheConnector.reset()

      val result = controller().onSubmit(CheckUpdateMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersCacheConnector.verify(PAInDeclarationJourneyId, true)
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
      FakeUserAnswersCacheConnector.verify(IsNewAdviserId, true)
    }
  }
}
