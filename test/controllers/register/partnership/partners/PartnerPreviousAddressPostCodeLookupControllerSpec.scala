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

package controllers.register.partnership.partners

import connectors.{AddressLookupConnector, FakeUserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.partnership.partners.{PartnerNameId, PartnerPreviousAddressPostCodeLookupId}
import models.{PersonName, _}
import org.mockito.Matchers
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class PartnerPreviousAddressPostCodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new PostCodeLookupFormProvider()
  private val form = formProvider()
  private val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]

  private val index = Index(0)
  private val partnerName = "test first name test middle name test last name"

  private def controller(dataRetrievalAction: DataRetrievalAction = getPartner) =
    new PartnerPreviousAddressPostCodeLookupController(
      frontendAppConfig,
      FakeUserAnswersCacheConnector,
      fakeAddressLookupConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      messagesApi,
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  private val testAnswer = "AB12 1AB"

  private lazy val viewModel =
    PostcodeLookupViewModel(
      routes.PartnerPreviousAddressPostCodeLookupController.onSubmit(NormalMode, index),
      routes.PartnerPreviousAddressController.onPageLoad(NormalMode, index),
      Message("previousAddressPostCode.heading", Message("thePartner")),
      Message("previousAddressPostCode.heading", "test first name test last name"),
      Message("common.previousAddress.enterPostcode"),
      Some(Message("common.selectAddress.link")),
      Message("partnerPreviousAddressPostCodeLookup.input.text")
    )

  private def viewAsString(form: Form[_] = form) =
    postcodeLookup(
      frontendAppConfig,
      form,
      viewModel,
      NormalMode
    )(fakeRequest, messages).toString

  private def fakeAddress(postCode: String): TolerantAddress = TolerantAddress(
    Some("Address Line 1"),
    Some("Address Line 2"),
    Some("Address Line 3"),
    Some("Address Line 4"),
    Some(postCode),
    Some("GB")
  )

  private val validData = Json.obj(
    BusinessNameId.toString -> "Test Partnership Name",
    "partners" -> Json.arr(
      Json.obj(
        PartnerNameId.toString ->
          PersonName("test first name", "test last name"),
        PartnerPreviousAddressPostCodeLookupId.toString ->
          Seq(fakeAddress(testAnswer))
      ),
      Json.obj(
        PartnerNameId.toString ->
          PersonName("test", "test")
      )
    )
  )

  "PartnerPreviousAddressPostCodeLookup Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "not populate the view correctly on a GET even when the question has previously been answered" in {
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode, index)(fakeRequest)

      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))
      when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(testAnswer))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Seq(fakeAddress(testAnswer))))

      val result = controller().onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors" when {
      "invalid data as blank value is submitted " in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", ""))
        val boundForm = form.bind(Map("value" -> ""))

        val result = controller().onSubmit(NormalMode, index)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }

      "invalid data as value exceeding max length is submitted " in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "AB12 12AB"))
        val boundForm = form.bind(Map("value" -> "AB12 12AB"))

        val result = controller().onSubmit(NormalMode, index)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }

      "invalid data as invalid postcode is submitted " in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "12AB AB1"))
        val boundForm = form.bind(Map("value" -> "12AB AB1"))

        val result = controller().onSubmit(NormalMode, index)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode, index)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testAnswer))
      val result = controller(dontGetAnyData).onSubmit(NormalMode, index)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
