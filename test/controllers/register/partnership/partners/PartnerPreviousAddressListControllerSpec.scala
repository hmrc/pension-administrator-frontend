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

import java.time.LocalDate

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressListFormProvider
import identifiers.register.partnership.partners.{PartnerDetailsId, PartnerPreviousAddressPostCodeLookupId}
import models.{NormalMode, PersonDetails, TolerantAddress}
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

class PartnerPreviousAddressListControllerSpec extends ControllerSpecBase {

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new AddressListFormProvider()
  private val form: Form[Int] = formProvider(Seq.empty)

  private val partner = PersonDetails("firstName", Some("middle"), "lastName", LocalDate.now())

  private val addresses = Seq(
    address("test post code 1"),
    address("test post code 2")
  )

  private def address(postCode: String): TolerantAddress = TolerantAddress(
    Some("address line 1"),
    Some("address line 2"),
    Some("test town"),
    Some("test county"),
    Some(postCode),
    Some("United Kingdom")
  )

  private val validData: JsValue = Json.obj(
    "partners" -> Json.arr(
      Json.obj(
        PartnerDetailsId.toString -> partner,
        PartnerPreviousAddressPostCodeLookupId.toString -> addresses
      )
    )
  )

  private val data = new FakeDataRetrievalAction(Some(validData))

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new PartnerPreviousAddressListController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  private lazy val viewModel =
    AddressListViewModel(
      postCall = routes.PartnerPreviousAddressListController.onSubmit(NormalMode, firstIndex),
      manualInputCall = routes.PartnerPreviousAddressController.onPageLoad(NormalMode, firstIndex),
      addresses = addresses,
      Message("common.previousAddressList.title"),
      Message("common.previousAddressList.heading"),
      Some(Message(partner.fullName)),
      Message("common.selectAddress.text"),
      Message("common.selectAddress.link")
    )

  private def viewAsString(form: Form[_] = form): String =
    addressList(
      frontendAppConfig,
      form,
      viewModel,
      NormalMode
    )(fakeRequest, messages).toString

  "PartnerPreviousAddressList Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(data).onPageLoad(NormalMode, firstIndex)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "1"))

      val result = controller(data).onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Address look up page" when {
      "no addresses are present after lookup" when {
        "GET" in {

          val validData: JsValue = Json.obj(
            "partners" -> Json.arr(
              Json.obj(
                PartnerDetailsId.toString -> partner
              )
            )
          )

          val data = new FakeDataRetrievalAction(Some(validData))

          val result = controller(data).onPageLoad(NormalMode, firstIndex)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(NormalMode, firstIndex).url)
        }

        "POST" in {

          val validData: JsValue = Json.obj(
            "partners" -> Json.arr(
              Json.obj(
                PartnerDetailsId.toString -> partner
              )
            )
          )

          val data = new FakeDataRetrievalAction(Some(validData))

          val result = controller(data).onSubmit(NormalMode, firstIndex)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(NormalMode, firstIndex).url)
        }
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(data).onSubmit(NormalMode, firstIndex)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad(NormalMode, firstIndex)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "1"))
          val result = controller(dontGetAnyData).onSubmit(NormalMode, firstIndex)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

  }
}
