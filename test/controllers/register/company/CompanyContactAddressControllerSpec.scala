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
import controllers.actions.*
import forms.{UKAddressFormProvider, UKOnlyAddressFormProvider}
import models.*
import models.admin.ukResidencyToggle
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.Helpers.*
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, FakeNavigator, FeatureFlagMockHelper}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.{manualAddress, manualAddressUKOnly}

class CompanyContactAddressControllerSpec extends ControllerSpecBase with ScalaFutures with FeatureFlagMockHelper with BeforeAndAfterEach {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val view: manualAddress = app.injector.instanceOf[manualAddress]
  val viewUK: manualAddressUKOnly = app.injector.instanceOf[manualAddressUKOnly]

  val formProvider = new UKAddressFormProvider(countryOptions)
  val formProviderUKOnly = new UKOnlyAddressFormProvider
  val form: Form[Address] = formProvider()
  val formUK: Form[AddressUKOnly] = formProviderUKOnly()
  private val isUkHintText = true

  def controller(dataRetrievalAction: DataRetrievalAction = getCompany) =
    new CompanyContactAddressController(
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      formProviderUKOnly,
      countryOptions,
      controllerComponents,
      mockFeatureFlagService,
      view,
      viewUK
    )

  private lazy val viewModel = ManualAddressViewModel(
    routes.CompanyContactAddressController.onSubmit(NormalMode),
    countryOptions.options,
    Message("enter.address.heading", Message("theCompany")),
    Message("enter.address.heading", companyName),
    psaName = Some(companyName),
    returnLink = Some(controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad().url)
  )

  private def viewAsString(form: Form[?] = form) =
    view(
      form,
      viewModel,
      NormalMode,
      isUkHintText
  )(fakeRequest, messages).toString()

  private def viewNoCountryAsString(form: Form[?] = formUK) =
      viewUK(
        form,
        viewModel,
        NormalMode,
        isUkHintText
  )(fakeRequest, messages).toString()

  override def beforeEach(): Unit = {
    super.beforeEach()
    featureFlagMock(ukResidencyToggle)
  }

  "CompanyContactAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        ("postCode", "NE1 1NE"),
        "country" -> "GB"
      )

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the next page when valid data is submitted with toggle enabled" in {
      featureFlagMock(ukResidencyToggle, isEnabled = true)
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        ("postCode", "NE1 1NE")
      )

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "return a Bad Request and errors when invalid data is submitted with toggle enabled" in {
      featureFlagMock(ukResidencyToggle, isEnabled = true)
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = formUK.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewNoCountryAsString(boundForm)
    }

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody()
          val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
      }
    }
  }

}
