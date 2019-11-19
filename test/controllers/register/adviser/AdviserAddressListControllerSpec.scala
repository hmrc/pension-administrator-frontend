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

import base.CSRFRequest
import connectors.FakeUserAnswersCacheConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressListFormProvider
import identifiers.register.adviser.{AdviserAddressPostCodeLookupId, AdviserNameId}
import models.{NormalMode, TolerantAddress}
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.Adviser
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.Future

class AdviserAddressListControllerSpec extends ControllerSpecBase with CSRFRequest {

  import AdviserAddressListControllerSpec._

  "Adviser Address List Controller" must {

    "return Ok and the correct view on a GET request" in {
      val viewModel: AddressListViewModel = addressListViewModel(addresses)
      val form = new AddressListFormProvider()(viewModel.addresses)

      requestResult(
        implicit app => addToken(FakeRequest(routes.AdviserAddressListController.onPageLoad(NormalMode))), dataRetrievalAction,
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe addressList(frontendAppConfig, form, addressListViewModel(addresses), NormalMode)(request, messages).toString()
        }
      )
    }

    "redirect to Adviser Address Post Code Lookup if no address data on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.AdviserAddressListController.onPageLoad(NormalMode))), getEmptyData,
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AdviserAddressPostCodeLookupController.onPageLoad(NormalMode).url)
        }
      )
    }

    "redirect to Session Expired controller when no session data exists on a GET request" in {

      requestResult(
        implicit app => addToken(FakeRequest(routes.AdviserAddressListController.onPageLoad(NormalMode))), dontGetAnyData,
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      )
    }

    "redirect to the next page on POST of valid data" in {

      requestResult(
        implicit app => addToken(FakeRequest(routes.AdviserAddressListController.onSubmit(NormalMode))
          .withFormUrlEncodedBody(("value", "0"))), dataRetrievalAction,
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AdviserAddressController.onPageLoad(NormalMode).url)
        }
      )
    }

    "redirect to Session Expired controller when no session data exists on a POST request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.AdviserAddressListController.onSubmit(NormalMode))
          .withFormUrlEncodedBody(("value", "0"))), dontGetAnyData,
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      )
    }

    "redirect to Adviser Address Post Code Lookup if no address data on a POST request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.AdviserAddressListController.onSubmit(NormalMode))
          .withFormUrlEncodedBody(("value", "0"))), getEmptyData,
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AdviserAddressPostCodeLookupController.onPageLoad(NormalMode).url)
        }
      )
    }
  }
}

object AdviserAddressListControllerSpec extends ControllerSpecBase {
  private val onwardRoute = routes.AdviserAddressController.onPageLoad(NormalMode)
  val name = "Adviser name"
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
    .set(AdviserNameId)(name)
      .flatMap(_.set(AdviserAddressPostCodeLookupId)(addresses))
      .asOpt.map(_.json)

  private val dataRetrievalAction = new FakeDataRetrievalAction(data)

  private def addressListViewModel(addresses: Seq[TolerantAddress]): AddressListViewModel = {
    AddressListViewModel(
      routes.AdviserAddressListController.onSubmit(NormalMode),
      routes.AdviserAddressController.onPageLoad(NormalMode),
      addresses,
      Message("adviserAddressList.heading", Message("theAdviser")),
      Message("adviserAddressList.heading", name),
      Message("common.selectAddress.text"),
      Message("common.selectAddress.link")
    )
  }

  private def requestResult[T](request: Application => Request[T], data: FakeDataRetrievalAction,
                               test: (Request[_], Future[Result]) => Unit)(implicit writeable: Writeable[T]): Unit = {
    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
      bind[DataRetrievalAction].toInstance(data),
      bind(classOf[Navigator]).qualifiedWith(classOf[Adviser]).toInstance(new FakeNavigator(desiredRoute = onwardRoute))
    )) { app =>
      val req = request(app)
      val result = route[T](app, req).value
      test(req, result)
    }
  }
}
