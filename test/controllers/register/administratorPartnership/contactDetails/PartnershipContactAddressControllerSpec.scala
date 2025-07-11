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
import controllers.actions._
import forms.UKAddressFormProvider
import models._
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, FakeNavigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress


class PartnershipContactAddressControllerSpec
  extends ControllerSpecBase
    with ScalaFutures
    with OptionValues {

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad

  private def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  private val view: manualAddress = app.injector.instanceOf[manualAddress]

  private val messagePrefix = "enter.address"
  private val formProvider = new UKAddressFormProvider(new FakeCountryOptions(environment, frontendAppConfig))
  private val form: Form[Address] = formProvider("error.country.invalid")
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
      countryOptions,
      controllerComponents,
      view
    )

  private def viewAsString(form: Form[?] = form): String =
    view(form, viewModel, NormalMode, isUkHintText)(fakeRequest, messages).toString

  "PartnershipContactAddress Controller" must {

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

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
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
