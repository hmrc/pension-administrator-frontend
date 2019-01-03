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

package controllers.register.company

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressListFormProvider
import identifiers.register.company.CompanyPreviousAddressPostCodeLookupId
import models.{BusinessDetails, NormalMode, TolerantAddress}
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

class CompanyAddressListControllerSpec extends ControllerSpecBase {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new AddressListFormProvider()
  private val companyName = "ThisCompanyName"
  private val companyDetails = Json.obj("businessDetails" -> BusinessDetails(companyName, Some("Test UTR")))

  private val addresses = Seq(
    address("test post code 1"),
    address("test post code 2")
  )

  private val addressObject = Json.obj(CompanyPreviousAddressPostCodeLookupId.toString -> addresses)

  def address(postCode: String): TolerantAddress = TolerantAddress(
    Some("address line 1"),
    Some("address line 2"),
    Some("test town"),
    Some("test county"),
    Some(postCode),
    Some("United Kingdom")
  )

  val form: Form[Int] = formProvider(Seq(0))

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new CompanyAddressListController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  private lazy val viewModel = AddressListViewModel(
    postCall = routes.CompanyAddressListController.onSubmit(NormalMode),
    manualInputCall = routes.CompanyPreviousAddressController.onPageLoad(NormalMode),
    addresses = addresses,
    Message("common.previousAddressList.title"),
    Message("common.previousAddressList.heading"),
    None,
    Message("common.selectAddress.text"),
    Message("common.selectAddress.link")
  )

  private def viewAsString(form: Form[_] = form) =
    addressList(
      frontendAppConfig,
      form,
      viewModel
    )(fakeRequest, messagesApi.preferred(fakeRequest)).toString()

  "CompanyAddressList Controller" must {

    "return OK and the correct view for a GET" in {
      val dataRetrieval = new FakeDataRetrievalAction(Some(companyDetails ++ addressObject))
      val result = controller(dataRetrieval).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Address look up page" when {
      "no addresses are present after lookup" when {
        "GET" in {
          val dataRetrieval = new FakeDataRetrievalAction(Some(companyDetails))
          val result = controller(dataRetrieval).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(
            controllers.register.company.routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(NormalMode).url)
        }

        "POST" in {

          val dataRetrieval = new FakeDataRetrievalAction(Some(companyDetails))
          val result = controller(dataRetrieval).onSubmit(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(
            controllers.register.company.routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(NormalMode).url)
        }
      }
    }

    "redirect to the next page valid data is submitted" in {
      val dataRetrieval = new FakeDataRetrievalAction(Some(companyDetails ++ addressObject))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "1"))
      val result = controller(dataRetrieval).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val dataRetrieval = new FakeDataRetrievalAction(Some(companyDetails ++ addressObject))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(dataRetrieval).onSubmit(NormalMode)(postRequest)

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
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "1"))
          val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

  }

}
