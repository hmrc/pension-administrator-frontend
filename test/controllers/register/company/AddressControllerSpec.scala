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

import connectors.RegistrationConnector
import connectors.cache.{FakeUserAnswersCacheConnector, FeatureToggleConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.AddressFormProvider
import identifiers.register.company.ConfirmCompanyAddressId
import models.{NormalMode, TolerantAddress}
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import utils.{AddressHelper, FakeCountryOptions, FakeNavigator}
import views.html.address.manualAddress

class AddressControllerSpec extends ControllerSpecBase {
  val view: manualAddress = inject[manualAddress]
  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  val formProvider = new AddressFormProvider(countryOptions)
  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad
  val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]
  val addressHelper: AddressHelper = inject[AddressHelper]

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData,
                 featureToggleConnector: FeatureToggleConnector = FakeFeatureToggleConnector.disabled) =
    new AddressController(
      FakeAuthAction,
      FakeUserAnswersCacheConnector,
      controllerComponents,
      countryOptions,
      featureToggleConnector,
      formProvider,
      dataRetrievalAction,
      new FakeNavigator(desiredRoute = onwardRoute),
      new FakeNavigator(desiredRoute = onwardRoute),
      mockRegistrationConnector,
      new DataRequiredActionImpl,
      view,
      addressHelper
    )


  val testAddress = TolerantAddress(
    Some("Some Building"),
    Some("1 Some Street"),
    Some("Some Village"),
    Some("Some Town"),
    Some("ZZ1 1ZZ"),
    Some("GB")
  )

  private val companyDetails = Json.obj(ConfirmCompanyAddressId.toString -> testAddress)

  "GET /update-missing-address-fields" must {
    "return SEE_OTHER" in {
      val dataRetrieval = new FakeDataRetrievalAction(Some(companyDetails))
      val result = controller(dataRetrieval).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
    }
  }
  "POST /update-missing-address-fields" must {
    "return SEE_OTHER when valid data is submitted" in {
      val dataRetrieval = new FakeDataRetrievalAction(Some(companyDetails))
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "A Big House"),
        ("addressLine2", "1 Some Place"),
        ("postCode", "ZZ1 1ZZ"),
        ("country", "GB")
      )

      val result = controller(dataRetrieval).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
    }
    "return BAD_REQUEST when invalid data is submitted" in {
      val dataRetrieval = new FakeDataRetrievalAction(Some(companyDetails))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalidValue"))

      val result = controller(dataRetrieval).onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
    }
  }
}
