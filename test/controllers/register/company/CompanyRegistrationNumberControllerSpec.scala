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

package controllers.register.company

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.company.CompanyRegistrationNumberFormProvider
import identifiers.register.company.{BusinessDetailsId, CompanyRegistrationNumberId}
import models.{BusinessDetails, Mode, NormalMode}
import play.api.data.Form
import play.api.libs.json.{JsString, _}
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.register.company.enterNumber

class CompanyRegistrationNumberControllerSpec extends ControllerSpecBase {

  private val companyName = "Test Company Name"

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new CompanyRegistrationNumberFormProvider()
  val form = formProvider()

  def controller(dataRetrievalAction: DataRetrievalAction = getCompany) =
    new CompanyRegistrationNumberController(
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

  private def viewModel: CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = routes.CompanyRegistrationNumberController.onSubmit(NormalMode),
      title = Message("companyRegistrationNumber.heading", Message("theCompany").resolve),
      heading = Message("companyRegistrationNumber.heading", companyName),
      mode = NormalMode,
      entityName = companyName
    )

  def viewAsString(form: Form[_] = form): String = enterNumber(
    frontendAppConfig,
    form,
    viewModel
  )(fakeRequest, messages).toString

  val testAnswer = "AB123456"

  "CompanyRegistrationNumber Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(
        BusinessDetailsId.toString ->
          BusinessDetails("Test Company Name", Some("Test UTR")),
        CompanyRegistrationNumberId.toString -> JsString(testAnswer)
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(testAnswer))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", ""))
      val boundForm = form.bind(Map("value" -> ""))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
