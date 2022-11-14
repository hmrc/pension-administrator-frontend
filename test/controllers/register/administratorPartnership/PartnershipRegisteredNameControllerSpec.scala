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

package controllers.register.administratorPartnership

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.BusinessNameFormProvider
import identifiers.register.BusinessNameId
import models.NormalMode
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, _}
import utils.FakeNavigator
import viewmodels.OrganisationNameViewModel
import views.html.organisationName

class PartnershipRegisteredNameControllerSpec extends ControllerSpecBase {

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  val view: organisationName = app.injector.instanceOf[organisationName]

  val formProvider = new BusinessNameFormProvider()
  val form: Form[String] = formProvider(
    requiredKey = "partnershipName.error.required",
    invalidKey = "partnershipName.error.invalid",
    lengthKey = "partnershipName.error.length")
  val testCompanyName = "test company name"

  def viewmodel = OrganisationNameViewModel(
    postCall = controllers.register.administratorPartnership.routes.PartnershipRegisteredNameController.onSubmit(),
    title = "partnershipName.title",
    heading = "partnershipName.heading"
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new PartnershipRegisteredNameController(
      frontendAppConfig,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      FakeUserAnswersCacheConnector,
      controllerComponents,
      view
    )

  def viewAsString(form: Form[_] = form): String = view(form, viewmodel)(fakeRequest, messages).toString

  "CompanyRegisteredName Controller" when {

    "on a GET" must {

      "return OK and the correct view" in {
        val result = controller().onPageLoad(NormalMode)(fakeRequest)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "populate the view correctly when the question has previously been answered" in {
        val validData = Json.obj(BusinessNameId.toString -> testCompanyName)
        val getRelevantData = new FakeDataRetrievalAction(Some(validData))

        val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

        contentAsString(result) mustBe viewAsString(form.fill(testCompanyName))
      }

      "redirect to Session Expired for a GET if no existing data is found" in {
        val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
      }
    }

    "on a POST" must {

      "redirect to the next page when valid data is submitted" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testCompanyName))

        val result = controller().onSubmit(NormalMode)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "return a Bad Request and errors when invalid data is submitted" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("companyName", ""))
        val boundForm = form.bind(Map("companyName" -> ""))

        val result = controller().onSubmit(NormalMode)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }

      "redirect to Session Expired for a POST if no existing data is found" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("companyName", testCompanyName))
        val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
      }
    }
  }
}
