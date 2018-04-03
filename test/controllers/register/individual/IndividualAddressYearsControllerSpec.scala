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

package controllers.register.individual

import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressYearsFormProvider
import identifiers.register.individual.{IndividualAddressYearsId, IndividualDetailsId}
import models.{AddressYears, NormalMode, TolerantIndividual}
import play.api.data.Form
import play.api.libs.json.{JsString, _}
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

class IndividualAddressYearsControllerSpec extends ControllerSpecBase {

  def onwardRoute = controllers.routes.IndexController.onPageLoad()

  val formProvider = new AddressYearsFormProvider()
  val form = formProvider(Message("error.addressYears.required"))
  val questionText = "individualAddressYears.title"
  val individualDetails = TolerantIndividual(Some("TestFirstName"), None, Some("TestLastName"))
  val name = individualDetails.fullName
  val viewmodel = AddressYearsViewModel(
    postCall = routes.IndividualAddressYearsController.onSubmit(NormalMode),
    title = Message(questionText, name),
    heading = Message(questionText, name),
    legend = Message(questionText, name),
    Some(Message("whatYouWillNeed.secondary.heading"))
  )

  def controller(dataRetrievalAction: DataRetrievalAction = getIndividual) =
    new IndividualAddressYearsController(frontendAppConfig, messagesApi, new FakeNavigator(desiredRoute = onwardRoute), FakeDataCacheConnector, FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, formProvider)

  def viewAsString(form: Form[_] = form) = addressYears(frontendAppConfig, form, viewmodel)(fakeRequest, messages).toString

  val validData = UserAnswers()
    .set(IndividualDetailsId)(individualDetails)

  val getRelevantData = new FakeDataRetrievalAction(Some(validData.get.json))

  "IndividualAddressYears Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getData = new FakeDataRetrievalAction(Some(validData.flatMap(_.set(IndividualAddressYearsId)(AddressYears.values.head)).get.json))

      val result = controller(getData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(AddressYears.values.head))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", AddressYears.options.head.value))

      val result = controller(getRelevantData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(getRelevantData).onSubmit(NormalMode)(postRequest)

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
