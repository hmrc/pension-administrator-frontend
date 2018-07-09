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

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent}
import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.AddressFormProvider
import models._
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress


class PartnershipContactAddressControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with OptionValues {

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val messagePrefix = "partnership.contactAddress"
  val partnershipDetails = models.BusinessDetails("Test Partnership Name", "1234567890")

  val formProvider = new AddressFormProvider(new FakeCountryOptions(environment, frontendAppConfig))
  val form: Form[Address] = formProvider("error.country.invalid")

  val viewmodel = ManualAddressViewModel(
    postCall = routes.PartnershipContactAddressController.onSubmit(NormalMode),
    countryOptions = countryOptions.options,
    title = Message(s"$messagePrefix.title"),
    heading = Message(s"$messagePrefix.heading").withArgs(partnershipDetails.companyName),
    secondaryHeader = Some("site.secondaryHeader"),
    hint = Some(Message(s"$messagePrefix.hint").withArgs(partnershipDetails.companyName))
  )

  val fakeAuditService = new StubSuccessfulAuditService()

  def controller(dataRetrievalAction: DataRetrievalAction = getPartnership) =
    new PartnershipContactAddressController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      countryOptions,
      fakeAuditService
    )

  def viewAsString(form: Form[_] = form): String = manualAddress(frontendAppConfig, form, viewmodel)(fakeRequest, messages).toString

  "PartnershipContactAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = UserAnswers()
        .partnershipDetails(partnershipDetails)
        .partnershipContactAddress(Address("value 1", "value 2", None, None, None, "GB"))
        .dataRetrievalAction

      val result = controller(validData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(Address("value 1", "value 2", None, None, None, "GB")))
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

    "send an audit event when valid data is submitted" in {

      val existingAddress = Address(
        "existing-line-1",
        "existing-line-2",
        None,
        None,
        None,
        "existing-country"
      )

      val selectedAddress = TolerantAddress(None, None, None, None, None, None)

      val data =
        UserAnswers()
          .companyContactAddress(existingAddress)
          .companyContactAddressList(selectedAddress)
          .dataRetrievalAction

      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        ("postCode", "NE1 1NE"),
        "country" -> "GB"
      )

      fakeAuditService.reset()

      val result = controller(data).onSubmit(NormalMode)(postRequest)

      whenReady(result) {
        _ =>
          fakeAuditService.verifySent(
            AddressEvent(
              FakeAuthAction.externalId,
              AddressAction.LookupChanged,
              "Partnership Contact Address",
              Address(
                "value 1",
                "value 2",
                None,
                None,
                Some("NE1 1NE"),
                "GB"
              )
            )
          )
      }

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
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody()
          val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

  }
}
