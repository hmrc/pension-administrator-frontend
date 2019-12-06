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

package controllers.register.adviser

import audit.testdoubles.StubSuccessfulAuditService
import audit.{AddressAction, AddressEvent}
import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.AddressFormProvider
import identifiers.register.adviser.{AdviserAddressId, AdviserNameId}
import models.{Address, NormalMode, TolerantAddress}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils._
import utils.countryOptions.CountryOptions
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.manualAddress

class AdviserAddressControllerSpec extends ControllerSpecBase with MockitoSugar with ScalaFutures with OptionValues {

  val view: manualAddress = app.injector.instanceOf[manualAddress]

  val formProvider = new AddressFormProvider(countryOptions)
  val form: Form[Address] = formProvider()
  val name = "Test Adviser Name"

  val addressViewModel: ManualAddressViewModel = ManualAddressViewModel(
    postCall = routes.AdviserAddressController.onSubmit(NormalMode),
    countryOptions = countryOptions.options,
    Message("enter.address.heading", Message("theAdviser")),
    Message("enter.address.heading", name)
  )

  val fakeAuditService = new StubSuccessfulAuditService()

  "AdviserAddress Controller" must {

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
          .adviserAddress(existingAddress)
          .adviserAddressList(selectedAddress)
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
              "Adviser Address",
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
      "no existing data is found on a GET" in {
        val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
      "no existing data is found on a POST" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody()
        val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  def controller(dataRetrievalAction: DataRetrievalAction = getAdviser) =
    new AdviserAddressController(frontendAppConfig, FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute), FakeAllowAccessProvider(),
      FakeAuthAction, dataRetrievalAction, new DataRequiredActionImpl, formProvider,
      countryOptions, fakeAuditService, stubMessagesControllerComponents(), view)

  def viewAsString(form: Form[_] = form): String = view(form, addressViewModel, NormalMode)(fakeRequest, messages).toString
}
