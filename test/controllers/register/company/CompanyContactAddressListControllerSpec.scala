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

import base.CSRFRequest
import connectors.{UserAnswersCacheConnector, FakeUserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction}
import controllers.register.individual.IndividualContactAddressPostCodeLookupControllerSpec.getEmptyData
import forms.address.AddressListFormProvider
import models.{NormalMode, TolerantAddress}
import org.scalatest.OptionValues
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, redirectLocation, route, running, status, _}
import utils.annotations.RegisterCompany
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.Future

class CompanyContactAddressListControllerSpec extends ControllerSpecBase with CSRFRequest {

  import CompanyContactAddressListControllerSpec._

  "company Contact Address List Controller" must {

    "return Ok and the correct view on a GET request" in {
      requestResult(dataRetrievalAction,
        implicit App => addToken(FakeRequest(routes.CompanyContactAddressListController.onPageLoad(NormalMode))
          .withFormUrlEncodedBody("value" -> "0")),
        (request, result) => {
          status(result) mustBe OK
          val viewModel: AddressListViewModel = addressListViewModel(addresses)
          val form = new AddressListFormProvider()(viewModel.addresses)

          contentAsString(result) mustBe addressList(frontendAppConfig, form, viewModel, NormalMode)(request, messages).toString
        }
      )
    }

    "redirect to Company Address Post Code Lookup if no address data on a GET request" in {
      requestResult(getEmptyData,
        implicit App => addToken(FakeRequest(routes.CompanyContactAddressListController.onPageLoad(NormalMode))
          .withFormUrlEncodedBody("value" -> "0")),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.CompanyContactAddressPostCodeLookupController.onPageLoad(NormalMode).url)
        }
      )
    }

    "redirect to Session Expired controller when no session data exists on a GET request" in {
      requestResult(dontGetAnyData,
        implicit App => addToken(FakeRequest(routes.CompanyContactAddressListController.onPageLoad(NormalMode))
          .withFormUrlEncodedBody("value" -> "0")),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      )
    }

    "redirect to the next page on POST of valid data" in {
      requestResult(dataRetrievalAction,
        implicit App => addToken(FakeRequest(routes.CompanyContactAddressListController.onSubmit(NormalMode))
          .withFormUrlEncodedBody("value" -> "0")),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      )
    }

    "redirect to Session Expired controller when no session data exists on a POST request" in {
      requestResult(dontGetAnyData,
        implicit App => addToken(FakeRequest(routes.CompanyContactAddressListController.onSubmit(NormalMode))
          .withFormUrlEncodedBody("value" -> "0")),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      )
    }

    "redirect to Company Address Post Code Lookup if no address data on a POST request" in {
      requestResult(getEmptyData,
        implicit App => addToken(FakeRequest(routes.CompanyContactAddressListController.onSubmit(NormalMode))
          .withFormUrlEncodedBody("value" -> "0")),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.CompanyContactAddressPostCodeLookupController.onPageLoad(NormalMode).url)
        }
      )
    }
  }
}

object CompanyContactAddressListControllerSpec extends OptionValues {
  val onwardRoute: Call = routes.CompanyContactAddressController.onPageLoad(NormalMode)

  private def addressListViewModel(addresses: Seq[TolerantAddress]): AddressListViewModel = {
    AddressListViewModel(
      routes.CompanyContactAddressListController.onSubmit(NormalMode),
      routes.CompanyContactAddressController.onPageLoad(NormalMode),
      addresses,
      Message("company.contactAddressList.title"),
      Message("company.contactAddressList.heading").withArgs("test company"),
      Message("common.selectAddress.text"),
      Message("common.selectAddress.link")
    )
  }

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

  private val dataRetrievalAction =
    UserAnswers().businessDetails.companyContactAddressList(addresses).dataRetrievalAction

  private def requestResult[T](data: DataRetrievalAction = getEmptyData,
                               request: Application => Request[T],
                               test: (Request[_], Future[Result]) => Unit)(implicit writeable: Writeable[T]): Unit = {
    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(data),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
      bind(classOf[Navigator]).qualifiedWith(classOf[RegisterCompany]).toInstance(new FakeNavigator(desiredRoute = onwardRoute))
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }
}
