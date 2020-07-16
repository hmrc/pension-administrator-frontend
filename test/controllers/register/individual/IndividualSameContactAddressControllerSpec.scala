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

package controllers.register.individual

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.SameContactAddressFormProvider
import identifiers.register.individual.{IndividualAddressId, IndividualDetailsId, IndividualSameContactAddressId}
import models.{AddressYears, NormalMode, TolerantAddress, TolerantIndividual}
import play.api.data.Form
import play.api.libs.json.JsResult
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.countryOptions.CountryOptions
import utils.{FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel
import views.html.address.sameContactAddress

class IndividualSameContactAddressControllerSpec extends ControllerSpecBase {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val formProvider = new SameContactAddressFormProvider()
  val form = formProvider("error.required")

  val testAddress = TolerantAddress(
    Some("address line 1"),
    Some("address line 2"),
    Some("test town"),
    Some("test county"),
    Some("test post code"), Some("GB")
  )

  def viewmodel = SameContactAddressViewModel(
    postCall = routes.IndividualSameContactAddressController.onSubmit(NormalMode),
    title = Message("individual.same.contact.address.title"),
    heading = Message("individual.same.contact.address.heading"),
    hint = None,
    address = testAddress,
    psaName = "Test name",
    mode = NormalMode
  )

  val countryOptions = new CountryOptions(environment, frontendAppConfig)

  def controller(dataRetrievalAction: DataRetrievalAction = getIndividual) =
    new IndividualSameContactAddressController(
      frontendAppConfig,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      countryOptions,
      stubMessagesControllerComponents(),
      view
    )

  val view: sameContactAddress = app.injector.instanceOf[sameContactAddress]

  def viewAsString(form: Form[_] = form): String =
    view(
      form,
      viewmodel,
      countryOptions
    )(fakeRequest, messages).toString

  val validData: JsResult[UserAnswers] = UserAnswers()
    .set(IndividualAddressId)(testAddress).flatMap(_.set(IndividualDetailsId)(TolerantIndividual(Some("First"), Some("Middle"), Some("Last"))))

  val getRelevantData = new FakeDataRetrievalAction(Some(validData.get.json))

  "IndividualSameContactAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getData = new FakeDataRetrievalAction(Some(validData.flatMap(_.set(IndividualSameContactAddressId)(false)).get.json))

      val result = controller(getData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(false))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

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
