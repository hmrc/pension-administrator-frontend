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

import connectors.AddressLookupConnector
import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.register.partnership.partners.{PartnerAddressPostCodeLookupId, PartnerNameId}
import models.{PersonName, _}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.{Form, FormError}
import play.api.libs.json._
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpException
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class PartnerAddressPostCodeLookupControllerSpec extends ControllerSpecBase {

  import PartnerAddressPostCodeLookupControllerSpec._

  private def controller(dataRetrievalAction: DataRetrievalAction = getRequiredData) = new PartnerAddressPostCodeLookupController(
    frontendAppConfig,
    FakeUserAnswersCacheConnector,
    fakeAddressLookupConnector,
    new FakeNavigator(desiredRoute = onwardRoute),
    FakeAuthAction,
    FakeAllowAccessProvider(),
    dataRetrievalAction,
    new DataRequiredActionImpl,
    formProvider,
    stubMessagesControllerComponents(),
    view
  )

  val view: postcodeLookup = app.injector.instanceOf[postcodeLookup]

  private def viewAsString(boundForm: Form[String] = form) = view(boundForm, viewModel(firstIndex), NormalMode)(fakeRequest, messages).toString

  "PartnerAddressPostCodeLookupController" when {

    "on a GET" must {
      "return Ok and the correct view" in {
        val result = controller().onPageLoad(NormalMode, firstIndex)(fakeRequest)
        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }

      "not populate the view even when the question has previously been answered" in {
        val validData = requiredData ++ Json.obj(
          PartnerAddressPostCodeLookupId.toString -> Seq(fakeAddress(testPostCode))
        )
        val getRelevantData = new FakeDataRetrievalAction(Some(validData))
        val result = controller(getRelevantData).onPageLoad(NormalMode, firstIndex)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString()
      }
    }

    "on a POST" must {
      val postRequest = fakeRequest.withFormUrlEncodedBody("value" -> testPostCode)

      "redirect to the next page when valid data is submitted" in {
        when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(testPostCode))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Seq(fakeAddress(testPostCode))))
        val result = controller().onSubmit(NormalMode, firstIndex)(postRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe onwardRoute.url
      }

      "save the list of addresses for the given postcode when valid data is submitted" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testPostCode))
        val expected = Seq(fakeAddress(testPostCode))

        when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.eq(testPostCode))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Seq(fakeAddress(testPostCode))))

        controller().onSubmit(NormalMode, firstIndex)(postRequest)

        FakeUserAnswersCacheConnector.verify(PartnerAddressPostCodeLookupId(firstIndex), expected)
      }

      "return the Bad Request when invalid data is submitted" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody("value" -> "")
        val boundForm = form.withError(FormError("value", "error.postcode.required"))
        val result = controller().onSubmit(NormalMode, firstIndex)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }

      "return a Bad Request when postcode lookup fails" in {
        val boundForm = form.withError(FormError("value", "error.postcode.failed"))
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testPostCode))

        when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.failed(new HttpException("Failed", INTERNAL_SERVER_ERROR)))

        val result = controller().onSubmit(NormalMode, firstIndex)(postRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(boundForm)
      }

      "return a message when post code lookup returns zero results" in {
        val boundForm = form.withError(FormError("value", Message("error.postcode.noResults").withArgs(testPostCode)))
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", testPostCode))

        when(fakeAddressLookupConnector.addressLookupByPostCode(Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Nil))

        val result = controller().onSubmit(NormalMode, firstIndex)(postRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(boundForm)
      }
    }
  }
}

object PartnerAddressPostCodeLookupControllerSpec extends MockitoSugar {
  private val fakeAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]
  private val onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new PostCodeLookupFormProvider()
  private val fooBar = PersonName("Foo", "Bar")
  private val testPostCode = "AB12 1AB"
  private val form = formProvider()
  private val requiredData = Json.obj(
    "partners" -> Seq(
      Json.obj(
        PartnerNameId.toString -> fooBar
      )
    )
  )
  private val getRequiredData = new FakeDataRetrievalAction(Some(requiredData))

  private def viewModel(index: Int) = PostcodeLookupViewModel(
    routes.PartnerAddressPostCodeLookupController.onSubmit(NormalMode, index),
    routes.PartnerAddressController.onPageLoad(NormalMode, index),
    Message("contactAddressPostCodeLookup.heading", Message("thePartner")),
    Message("contactAddressPostCodeLookup.heading", "Foo Bar"),
    Message("common.postcodeLookup.enterPostcode"),
    Some(Message("common.postcodeLookup.enterPostcode.link")),
    Message("common.address.enterPostcode.formLabel")
  )

  private def fakeAddress(postCode: String): TolerantAddress = TolerantAddress(
    Some("Address Line 1"),
    Some("Address Line 2"),
    Some("Address Line 3"),
    Some("Address Line 4"),
    Some(postCode),
    Some("GB")
  )
}
