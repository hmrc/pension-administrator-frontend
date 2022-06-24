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

package controllers.register.administratorPartnership.partners

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.AddressFormProvider
import models._
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, FakeNavigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class PartnerPreviousAddressControllerSpec extends ControllerSpecBase with ScalaFutures {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val formProvider = new AddressFormProvider(new FakeCountryOptions(environment, frontendAppConfig))
  private val form = formProvider()
  private val index = Index(0)

  private def controller(dataRetrievalAction: DataRetrievalAction = getPartner) =
    new PartnerPreviousAddressController(
      frontendAppConfig,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      new FakeCountryOptions(environment, frontendAppConfig),
      controllerComponents,
      view
    )

  val view: manualAddress = app.injector.instanceOf[manualAddress]

  private val viewModel =
    ManualAddressViewModel(
      routes.PartnerPreviousAddressController.onSubmit(NormalMode, index),
      countryOptions.options,
      Message("enter.previous.address.heading", Message("thePartner")),
      Message("enter.previous.address.heading", "test first name test last name"),
      None
    )

  private def viewAsString(form: Form[_] = form) =
    view(
      form,
      viewModel,
      NormalMode
    )(fakeRequest, messages).toString

  "PartnerPreviousAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page" when {
      "valid data is submitted with country as GB" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("addressLine1", "test address line 1"), ("addressLine2", "test address line 2"),
          ("postCode", "NE1 1NE"),
          "country" -> "GB"
        )

        val result = controller().onSubmit(NormalMode, index)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
      "valid data is submitted with country as non GB" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("addressLine1", "test address line 1"), ("addressLine2", "test address line 2"),
          "country" -> "CA"
        )

        val result = controller().onSubmit(NormalMode, index)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("field1", "value 1"), ("field2", "value 2"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
