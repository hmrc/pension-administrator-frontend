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

package controllers.register.advisor

import base.CSRFRequest
import play.api.libs.json.Json
import utils.UserAnswers
import connectors.{DataCacheConnector, FakeDataCacheConnector}
import controllers.actions._
import play.api.test.Helpers._
import models.{NormalMode, TolerantAddress}
import controllers.ControllerSpecBase
import forms.address.AddressListFormProvider
import identifiers.register.advisor.AdvisorAddressPostCodeLookupId
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.Future

class AdvisorAddressListControllerSpec extends ControllerSpecBase with CSRFRequest {

  import AdvisorAddressListControllerSpec._

  "Advisor Address List Controller" must {

    "return Ok and the correct view on a GET request" in {
      val viewModel: AddressListViewModel = addressListViewModel(addresses)
      val form = new AddressListFormProvider()(viewModel.addresses)

      requestResult(
        implicit app => addToken(FakeRequest(routes.AdvisorAddressListController.onPageLoad(NormalMode))), dataRetrievalAction,
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe addressList(frontendAppConfig, form, addressListViewModel(addresses))(request, messages).toString()
        }
      )
    }

    "redirect to Advisor Address Post Code Lookup if no address data on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.AdvisorAddressListController.onPageLoad(NormalMode))), getEmptyData,
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AdvisorAddressPostCodeLookupController.onPageLoad(NormalMode).url)
        }
      )
    }

    "redirect to Session Expired controller when no session data exists on a GET request" in {

      requestResult(
        implicit app => addToken(FakeRequest(routes.AdvisorAddressListController.onPageLoad(NormalMode))), dontGetAnyData,
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      )
    }

    "redirect to the next page on POST of valid data" ignore {

      requestResult(
        implicit app => addToken(FakeRequest(routes.AdvisorAddressListController.onSubmit(NormalMode))
          .withFormUrlEncodedBody(("value", "0"))), dataRetrievalAction,
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AdvisorAddressController.onPageLoad(NormalMode).url)
        }
      )
    }

    "redirect to Session Expired controller when no session data exists on a POST request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.AdvisorAddressListController.onSubmit(NormalMode))
          .withFormUrlEncodedBody(("value", "0"))), dontGetAnyData,
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      )
    }

    "redirect to Advisor Address Post Code Lookup if no address data on a POST request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.AdvisorAddressListController.onSubmit(NormalMode))
          .withFormUrlEncodedBody(("value", "0"))), getEmptyData,
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AdvisorAddressPostCodeLookupController.onPageLoad(NormalMode).url)
        }
      )
    }
  }
}

object AdvisorAddressListControllerSpec extends ControllerSpecBase {
  private val addresses = Seq(
    TolerantAddress(
      Some("Address 1 Line 1"),
      Some("Address 1 Line 2"),
      Some("Address 1 Line 3"),
      Some("Address 1 Line 4"),
      Some("A1 1PC"),
      Some("GB")
    ),
    TolerantAddress(
      Some("Address 2 Line 1"),
      Some("Address 2 Line 2"),
      Some("Address 2 Line 3"),
      Some("Address 2 Line 4"),
      Some("123"),
      Some("FR")
    )
  )

  private val data =
    UserAnswers(Json.obj())
      .set(AdvisorAddressPostCodeLookupId)(addresses)
      .asOpt.map(_.json)

  private val dataRetrievalAction = new FakeDataRetrievalAction(data)

  private def addressListViewModel(addresses: Seq[TolerantAddress]): AddressListViewModel = {
    AddressListViewModel(
      routes.AdvisorAddressListController.onSubmit(NormalMode),
      routes.AdvisorAddressController.onPageLoad(NormalMode),
      addresses,
      Message("common.selectAddress.title"),
      Message("common.selectAddress.heading"),
      Some(Message("common.advisor.secondary.heading")),
      Message("common.selectAddress.text"),
      Message("common.selectAddress.link")
    )
  }

  private def requestResult[T](request: (Application) => Request[T], data: FakeDataRetrievalAction,
                               test: (Request[_], Future[Result]) => Unit)(implicit writeable: Writeable[T]): Unit = {
    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataCacheConnector].toInstance(FakeDataCacheConnector),
      bind[DataRetrievalAction].toInstance(data)
    )) { app =>
      val req = request(app)
      val result = route[T](app, req).value
      test(req, result)
    }
  }
}
