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

package controllers.register.company

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.EnterVATFormProvider
import identifiers.register.{BusinessNameId, EnterVATId}
import models.NormalMode
import play.api.data.Form
import play.api.libs.json.{JsString, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.enterVAT

class CompanyEnterVATControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = companydetails.routes.CheckYourAnswersController.onPageLoad()

  val view: enterVAT = app.injector.instanceOf[enterVAT]

  val formProvider = new EnterVATFormProvider()
  val form: Form[String] = formProvider(companyName)

  def controller(dataRetrievalAction: DataRetrievalAction = getCompany) =
    new CompanyEnterVATController(
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

  private def viewModel: CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = routes.CompanyEnterVATController.onSubmit(NormalMode),
      title = Message("enterVAT.title", Message("theCompany")),
      heading = Message("enterVAT.heading", companyName),
      mode = NormalMode,
      entityName = companyName,
      returnLink =  Some(routes.CompanyRegistrationTaskListController.onPageLoad().url)
    )

  def viewAsString(form: Form[?] = form): String = view(
    form,
    viewModel
  )(fakeRequest, messages).toString

  val testAnswer = "123456789"

  "CompanyVATNumber Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(
        BusinessNameId.toString -> "Test Company Name",
        EnterVATId.toString -> JsString(testAnswer)
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

    "redirect to the task list page" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))
      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(companydetails.routes.CheckYourAnswersController.onPageLoad().url)
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
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }
}
