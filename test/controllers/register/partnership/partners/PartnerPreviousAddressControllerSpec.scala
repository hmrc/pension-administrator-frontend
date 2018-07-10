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

package controllers.register.partnership.partners

import java.time.LocalDate

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent}
import connectors.FakeDataCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.AddressFormProvider
import identifiers.register.company.CompanyDetailsId
import identifiers.register.partnership.partners.{PartnerDetailsId, PartnerPreviousAddressId}
import models._
import models.register.company.CompanyDetails
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, FakeNavigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class PartnerPreviousAddressControllerSpec extends ControllerSpecBase with ScalaFutures {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val formProvider = new AddressFormProvider(new FakeCountryOptions(environment, frontendAppConfig))
  private val form = formProvider()
  private val index = Index(0)
  private val partnerName = "test first name test middle name test last name"
  private val address = Address("test address line 1", "test address line 2", None, None, None, "GB")

  private val validData = Json.obj(
    CompanyDetailsId.toString -> CompanyDetails(None, None),
    "partners" -> Json.arr(
      Json.obj(
        PartnerDetailsId.toString ->
          PersonDetails("test first name", Some("test middle name"), "test last name", LocalDate.now),
        PartnerPreviousAddressId.toString ->
          address

      ),
      Json.obj(
        PartnerDetailsId.toString ->
          PersonDetails("test", Some("test"), "test", LocalDate.now)
      )
    )
  )

  private val auditService = new StubSuccessfulAuditService()

  private def controller(dataRetrievalAction: DataRetrievalAction = getPartner) =
    new PartnerPreviousAddressController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      new FakeCountryOptions(environment, frontendAppConfig),
      auditService
    )

  private val viewModel =
    ManualAddressViewModel(
      routes.PartnerPreviousAddressController.onSubmit(NormalMode, index),
      countryOptions.options,
      Message("partnerPreviousAddress.title"),
      Message("partnerPreviousAddress.heading"),
      Some(Message(partnerName)),
      Some(Message("partnerPreviousAddress.hint"))
    )

  private def viewAsString(form: Form[_] = form) =
    manualAddress(
      frontendAppConfig,
      form,
      viewModel
    )(fakeRequest, messages).toString

  "PartnerPreviousAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, index)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(Address("test address line 1", "test address line 2", None, None, None, "GB")))
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
          .partnerPreviousAddress(index, existingAddress)
          .partnerPreviousAddressList(index, selectedAddress)
          .dataRetrievalAction

      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        ("postCode", "NE1 1NE"),
        "country" -> "GB"
      )

      auditService.reset()

      val result = controller(data).onSubmit(NormalMode, index)(postRequest)

      whenReady(result) {
        _ =>
          auditService.verifySent(
            AddressEvent(
              FakeAuthAction.externalId,
              AddressAction.LookupChanged,
              s"Partnership Partner Previous Address: $partnerName",
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
