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

package controllers.register.administratorPartnership.contactDetails

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions.*
import forms.{UKAddressFormProvider, UKOnlyAddressFormProvider}
import models.*
import models.admin.ukResidencyToggle
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.Helpers.*
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, FakeNavigator, FeatureFlagMockHelper}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.{manualAddress, manualAddressUKOnly}


class PartnershipContactAddressControllerSpec
  extends ControllerSpecBase
    with ScalaFutures
    with OptionValues
    with FeatureFlagMockHelper
    with BeforeAndAfterEach {

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  private val view: manualAddress = app.injector.instanceOf[manualAddress]
  private val viewUKOnly: manualAddressUKOnly = app.injector.instanceOf[manualAddressUKOnly]

  private val messagePrefix = "enter.address"
  private val formProvider = new UKAddressFormProvider(new FakeCountryOptions(environment, frontendAppConfig))
  private val formProviderUKOnly = new UKOnlyAddressFormProvider()
  private val form: Form[Address] = formProvider()
  private val formUK: Form[AddressUKOnly] = formProviderUKOnly()
  private val isUkHintText = true

  private def viewModel = ManualAddressViewModel(
    postCall = routes.PartnershipContactAddressController.onSubmit(NormalMode),
    countryOptions = countryOptions.options,
    title = Message(s"$messagePrefix.heading").withArgs("the partnership"),
    heading = Message(s"$messagePrefix.heading").withArgs("Test Partnership Name"),
    partnershipName = Some("Test Partnership Name"),
    returnLink = Some(controllers.register.administratorPartnership.routes.PartnershipRegistrationTaskListController.onPageLoad().url)
  )

  private def controller(dataRetrievalAction: DataRetrievalAction = getPartnership) =
    new PartnershipContactAddressController(
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAllowAccessProvider(config = frontendAppConfig),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      formProviderUKOnly,
      mockFeatureFlagService,
      countryOptions,
      controllerComponents,
      view,
      viewUKOnly
    )

  private def viewAsString(form: Form[?] = form): String =
    view(form, viewModel, NormalMode, isUkHintText)(fakeRequest, messages).toString

  private def ukOnlyViewAsString(form: Form[?] = formUK) =
    viewUKOnly(
      form,
      viewModel,
      NormalMode,
      isUkHintText
    )(fakeRequest, messages).toString()

  override def beforeEach(): Unit = {
    super.beforeEach()
    featureFlagMock(ukResidencyToggle)
  }

  "PartnershipContactAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" when {
      "ukResidency toggle is disabled" in {
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
      "ukResidency toggle is enabled" in {
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
    }

    "return a Bad Request and errors when invalid data is submitted" when {
      "ukResidency toggle is disabled" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val result = controller().onSubmit(NormalMode)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }
      "ukResidency toggle is enabled" in {
        featureFlagMock(ukResidencyToggle, isEnabled = true)
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
        val boundForm = formUK.bind(Map("value" -> "invalid value"))

        val result = controller().onSubmit(NormalMode)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe ukOnlyViewAsString(boundForm)
      }
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
