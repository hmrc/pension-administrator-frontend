/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.register.partnership

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.{BusinessDetailsFormModel, BusinessDetailsFormProvider}
import identifiers.register.partnership.PartnershipDetailsId
import models.BusinessDetails
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, _}
import utils.FakeNavigator
import viewmodels.OrganisationNameViewModel
import views.html.organisationName

class PartnershipRegisteredNameControllerSpec extends ControllerSpecBase {

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val businessDetailsFormModel = BusinessDetailsFormModel(
    companyNameMaxLength = 105,
    companyNameRequiredMsg = "partnershipName.error.required",
    companyNameLengthMsg = "partnershipName.error.length",
    companyNameInvalidMsg = "partnershipName.error.invalid",
    utrMaxLength = 10,
    utrRequiredMsg = None,
    utrLengthMsg = "businessDetails.error.utr.length",
    utrInvalidMsg = "businessDetails.error.utr.invalid"
  )

  val formProvider = new BusinessDetailsFormProvider(isUK = false)
  val form = formProvider(businessDetailsFormModel)
  val testCompanyName = "test company name"
  val testBusinessDetails = BusinessDetails(testCompanyName, None)

  def viewmodel = OrganisationNameViewModel(
    postCall = controllers.register.partnership.routes.PartnershipRegisteredNameController.onSubmit(),
    title = "partnershipName.title",
    heading = "partnershipName.heading"
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new controllers.register.partnership.PartnershipRegisteredNameController(
      frontendAppConfig,
      messagesApi,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeUserAnswersCacheConnector
    )

  def viewAsString(form: Form[_] = form): String = organisationName(frontendAppConfig, form, viewmodel)(fakeRequest, messages).toString

  "CompanyRegisteredName Controller" when {

    "on a GET" must {

      "return OK and the correct view" in {
        val result = controller().onPageLoad()(fakeRequest)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "populate the view correctly when the question has previously been answered" in {
        val validData = Json.obj(PartnershipDetailsId.toString -> testBusinessDetails)
        val getRelevantData = new FakeDataRetrievalAction(Some(validData))

        val result = controller(getRelevantData).onPageLoad()(fakeRequest)

        contentAsString(result) mustBe viewAsString(form.fill(testBusinessDetails))
      }

      "redirect to Session Expired for a GET if no existing data is found" in {
        val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "on a POST" must {

      "redirect to the next page when valid data is submitted" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("companyName", testCompanyName))

        val result = controller().onSubmit()(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "return a Bad Request and errors when invalid data is submitted" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("companyName", "[invalid value]"))
        val boundForm = form.bind(Map("companyName" -> "[invalid value]"))

        val result = controller().onSubmit()(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }

      "redirect to Session Expired for a POST if no existing data is found" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("companyName", testCompanyName))
        val result = controller(dontGetAnyData).onSubmit()(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }
}