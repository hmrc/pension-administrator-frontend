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

package controllers.register

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.StillUseAdviserFormProvider
import identifiers.register.adviser.AdviserNameId
import identifiers.register.{BusinessNameId, RegistrationInfoId, VariationStillDeclarationWorkingKnowledgeId}
import models.register.DeclarationWorkingKnowledge
import models.{RegistrationCustomerType, RegistrationInfo, RegistrationLegalStatus, UpdateMode}
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.stillUseAdviser

class StillUseAdviserControllerSpec extends ControllerSpecBase {

  private val psaName = "Blah Inc"
  private val personWithWorkingKnowledgeName = "Bill Bloggs"

  private val jsObjectAdviserAndBusinessDetails: JsObject = Json.obj(
    AdviserNameId.toString -> personWithWorkingKnowledgeName,
    BusinessNameId.toString ->psaName,
    RegistrationInfoId.toString -> RegistrationInfo(
      RegistrationLegalStatus.LimitedCompany, "", noIdentifier = false, RegistrationCustomerType.UK, None, None),

  )

  private val dataRetrievalActionWithAdviserAndBusinessDetails =
    new FakeDataRetrievalAction(Some(jsObjectAdviserAndBusinessDetails))

  private val onwardRoute = controllers.routes.IndexController.onPageLoad

  val view: stillUseAdviser = app.injector.instanceOf[stillUseAdviser]

  private val formProvider = new StillUseAdviserFormProvider()
  private val form = formProvider()


  "DeclarationWorkingKnowledge Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "save the answer yes on a valid request and redirect to Session Expired" in {
      val result = controller().onSubmit(UpdateMode)(fakeRequest.withFormUrlEncodedBody("value" -> "true"))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      FakeUserAnswersCacheConnector.verify(VariationStillDeclarationWorkingKnowledgeId, true)
    }

    "save the answer no on a valid request" in {
      val result = controller().onSubmit(UpdateMode)(fakeRequest.withFormUrlEncodedBody("value" -> "false"))
      status(result) mustBe SEE_OTHER
      FakeUserAnswersCacheConnector.verify(VariationStillDeclarationWorkingKnowledgeId, false)
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
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", DeclarationWorkingKnowledge.options.head.value))
      val result = controller(dontGetAnyData).onSubmit(UpdateMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }


  private def controller(dataRetrievalAction: DataRetrievalAction = dataRetrievalActionWithAdviserAndBusinessDetails) =
    new StillUseAdviserController(
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )

  private def viewAsString(form: Form[?] = form) = view(
    form, UpdateMode, Some(psaName), displayReturnLink = true, personWithWorkingKnowledgeName)(fakeRequest, messages).toString
}
