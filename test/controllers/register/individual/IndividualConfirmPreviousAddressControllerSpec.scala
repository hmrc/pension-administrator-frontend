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

package controllers.register.individual

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.SameContactAddressFormProvider
import identifiers.register.individual.{ExistingCurrentAddressId, IndividualConfirmPreviousAddressId, IndividualDetailsId}
import models._
import play.api.data.Form
import play.api.libs.json.JsResult
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import utils.{FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel
import views.html.address.sameContactAddress

class IndividualConfirmPreviousAddressControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new SameContactAddressFormProvider()

  val testAddress = TolerantAddress(
    Some("address line 1"),
    Some("address line 2"),
    Some("test town"),
    Some("test county"),
    Some("test post code"), Some("GB")
  )

  def viewmodel = SameContactAddressViewModel(
    postCall = routes.IndividualConfirmPreviousAddressController.onSubmit(),
    title = Message("individual.confirmPreviousAddress.title"),
    heading = Message("individual.confirmPreviousAddress.heading", "John Doe"),
    secondaryHeader = None,
    hint = None,
    address = testAddress,
    psaName = "John Doe",
    mode = UpdateMode
  )

  val countryOptions = new CountryOptions(environment, frontendAppConfig)

  def controller(dataRetrievalAction: DataRetrievalAction = getIndividual) =
    new IndividualConfirmPreviousAddressController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      countryOptions
    )

  def viewAsString(form: Form[_] = formProvider()): String =
    sameContactAddress(
      frontendAppConfig,
      form,
      viewmodel,
      countryOptions
    )(fakeRequest, messages).toString

  val validData: JsResult[UserAnswers] = UserAnswers()
    .set(IndividualDetailsId)(TolerantIndividual(Some("John"), None, Some("Doe"))).flatMap(_.set(
    ExistingCurrentAddressId)(testAddress))

  val getRelevantData = new FakeDataRetrievalAction(Some(validData.get.json))

  "IndividualConfirmPreviousAddressController" must {

    "return OK and the correct view for a GET" in {
      val result = controller(getRelevantData).onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getData = new FakeDataRetrievalAction(Some(validData.flatMap(_.set(IndividualConfirmPreviousAddressId)(false)).get.json))

      val result = controller(getData).onPageLoad(UpdateMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(formProvider().fill(false))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller(getRelevantData).onSubmit(UpdateMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = formProvider().bind(Map("value" -> "invalid value"))

      val result = controller(getRelevantData).onSubmit(UpdateMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))
      val result = controller(dontGetAnyData).onSubmit(UpdateMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

}