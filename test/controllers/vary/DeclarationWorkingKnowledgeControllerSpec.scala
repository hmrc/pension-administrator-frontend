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

package controllers.vary

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.DeclarationWorkingKnowledgeFormProvider
import identifiers.register.DeclarationWorkingKnowledgeId
import identifiers.register.adviser.AdviserDetailsId
import identifiers.register.company.BusinessDetailsId
import models.register.DeclarationWorkingKnowledge
import models.register.adviser.AdviserDetails
import models.{BusinessDetails, UpdateMode}
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.vary.declarationWorkingKnowledge

class DeclarationWorkingKnowledgeControllerSpec extends ControllerSpecBase {

  val psaName = "Blah Inc"
  val personWithWorkingKnowledgeName = "Bill Bloggs"

  def getAdviserDetails: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      AdviserDetailsId.toString ->
        AdviserDetails(personWithWorkingKnowledgeName, "", ""),
      BusinessDetailsId.toString ->
        BusinessDetails(companyName = psaName, uniqueTaxReferenceNumber = None)

    )))

  def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new DeclarationWorkingKnowledgeFormProvider()
  val form = formProvider()

  val existingData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(DeclarationWorkingKnowledgeId.toString -> JsString(DeclarationWorkingKnowledge.Adviser.toString))))

  def controller(dataRetrievalAction: DataRetrievalAction = getAdviserDetails) =
    new DeclarationWorkingKnowledgeController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  def viewAsString(form: Form[_] = form) = declarationWorkingKnowledge(frontendAppConfig,
    form, UpdateMode, psaName, personWithWorkingKnowledgeName)(fakeRequest, messages).toString

  "DeclarationWorkingKnowledge Controller" must {

    "return OK and the correct view for a GET" in {


      val result = controller().onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

//    "populate the view correctly on a GET when the question has previously been answered" in {
//      val validData = Json.obj(DeclarationWorkingKnowledgeId.toString -> JsString(DeclarationWorkingKnowledge.values.head.toString))
//      val getRelevantData = new FakeDataRetrievalAction(Some(validData))
//
//      val result = controller(getRelevantData).onPageLoad(UpdateMode)(fakeRequest)
//
//      contentAsString(result) mustBe viewAsString(form.fill(DeclarationWorkingKnowledge.values.head))
//    }
//
//    "redirect to the next page when valid data is submitted" in {
//      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", DeclarationWorkingKnowledge.options.head.value))
//
//      val result = controller().onSubmit(UpdateMode)(postRequest)
//
//      status(result) mustBe SEE_OTHER
//      redirectLocation(result) mustBe Some(onwardRoute.url)
//    }
//
//    "return a Bad Request and errors when invalid data is submitted" in {
//      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
//      val boundForm = form.bind(Map("value" -> "invalid value"))
//
//      val result = controller().onSubmit(UpdateMode)(postRequest)
//
//      status(result) mustBe BAD_REQUEST
//      contentAsString(result) mustBe viewAsString(boundForm)
//    }
//
//    "redirect to Session Expired for a GET if no existing data is found" in {
//      val result = controller(dontGetAnyData).onPageLoad(UpdateMode)(fakeRequest)
//
//      status(result) mustBe SEE_OTHER
//      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
//    }
//
//    "redirect to Session Expired for a POST if no existing data is found" in {
//      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", DeclarationWorkingKnowledge.options.head.value))
//      val result = controller(dontGetAnyData).onSubmit(UpdateMode)(postRequest)
//
//      status(result) mustBe SEE_OTHER
//      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
//    }
//
//    "redirect to the next page and update the change ID when data has changed" in {
//      FakeUserAnswersCacheConnector.reset()
//      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", DeclarationWorkingKnowledge.WorkingKnowledge.toString))
//      val result = controller(existingData).onSubmit(UpdateMode)(postRequest)
//
//      status(result) mustBe SEE_OTHER
//      redirectLocation(result) mustBe Some(onwardRoute.url)
//      FakeUserAnswersCacheConnector.verify(DeclarationChangedId, true)
//    }
//
//    "redirect to the next page but not update the change ID when data has not changed" in {
//      FakeUserAnswersCacheConnector.reset()
//      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", DeclarationWorkingKnowledge.Adviser.toString))
//      val result = controller(existingData).onSubmit(UpdateMode)(postRequest)
//
//      status(result) mustBe SEE_OTHER
//      redirectLocation(result) mustBe Some(onwardRoute.url)
//      FakeUserAnswersCacheConnector.verifyNot(DeclarationChangedId)
//    }
  }
}
