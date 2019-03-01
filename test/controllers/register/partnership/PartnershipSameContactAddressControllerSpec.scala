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

package controllers.register.partnership

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import forms.address.SameContactAddressFormProvider
import models.{AddressYears, NormalMode, TolerantAddress}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import utils.{FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel
import views.html.address.sameContactAddress

class PartnershipSameContactAddressControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new SameContactAddressFormProvider()

  private val testAddress = TolerantAddress(
    Some("address line 1"),
    Some("address line 2"),
    Some("test town"),
    Some("test county"),
    Some("test post code"), Some("GB")
  )

  private val partnershipDetails = models.BusinessDetails("Test Partnership Name", Some("1234567890"))

  private val requiredData = UserAnswers()
    .partnershipDetails(partnershipDetails)
    .partnershipRegisteredAddress(testAddress)
    .dataRetrievalAction

  def viewmodel = SameContactAddressViewModel(
    postCall = controllers.register.partnership.routes.PartnershipSameContactAddressController.onSubmit(NormalMode),
    title = Message("partnership.sameContactAddress.title"),
    heading = Message("partnership.sameContactAddress.heading").withArgs(partnershipDetails.companyName),
    hint = None,
    secondaryHeader = None,
    address = testAddress,
    psaName = "Test name",
    mode = NormalMode
  )

  val countryOptions = new CountryOptions(environment, frontendAppConfig)

  def controller(dataRetrievalAction: DataRetrievalAction) =
    new PartnershipSameContactAddressController(
      new FakeNavigator(desiredRoute = onwardRoute),
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      countryOptions
    )

  def viewAsString(form: Form[_] = formProvider()): String = sameContactAddress(
    frontendAppConfig,
    form,
    viewmodel,
    countryOptions
  )(fakeRequest, messages).toString

  "PartnershipSameContactAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(requiredData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = UserAnswers()
        .partnershipDetails(partnershipDetails)
        .partnershipRegisteredAddress(testAddress)
        .partnershipSameContactAddress(areSame = false)
        .dataRetrievalAction

      val result = controller(validData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(formProvider().fill(false))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller(requiredData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = formProvider().bind(Map("value" -> "invalid value"))

      val result = controller(requiredData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
