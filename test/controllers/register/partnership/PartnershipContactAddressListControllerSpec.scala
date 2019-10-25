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

package controllers.register.partnership

import base.CSRFRequest
import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.AddressListFormProvider
import identifiers.register.partnership.PartnershipContactAddressPostCodeLookupId
import models.{BusinessDetails, NormalMode, TolerantAddress}
import org.scalatest.MustMatchers
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.Partnership
import utils.{FakeNavigator, Navigator}
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.Future

class PartnershipContactAddressListControllerSpec extends ControllerSpecBase with MustMatchers with CSRFRequest {

  import PartnershipContactAddressListControllerSpec._

  "PartnershipAddressListController" must {

    "render the view correctly on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.PartnershipContactAddressListController.onPageLoad(NormalMode))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe addressList(frontendAppConfig, form, viewModel, NormalMode)(request, messages).toString()
        }
      )
    }

    "redirect to the next page on a POST request" in {
      requestResult(
        implicit App => addToken(FakeRequest(routes.PartnershipContactAddressListController.onSubmit(NormalMode))
          .withFormUrlEncodedBody("value" -> "0")),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(FakeNavigator.desiredRoute.url)
        }
      )
    }

  }

}

object PartnershipContactAddressListControllerSpec extends PartnershipContactAddressListControllerSpec {

  val testName = "Partnership Name"

  val addresses = Seq(
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

  val form = new AddressListFormProvider()(addresses)

  val viewModel = AddressListViewModel(
    routes.PartnershipContactAddressListController.onSubmit(NormalMode),
    routes.PartnershipContactAddressController.onPageLoad(NormalMode),
    addresses
  )

  val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
    BusinessNameId.toString -> BusinessDetails(testName, Some("UTR")),
    PartnershipContactAddressPostCodeLookupId.toString -> addresses
  )))

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                              (implicit writeable: Writeable[T]): Unit = {

    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[AllowAccessActionProvider].to(FakeAllowAccessProvider()),
      bind[DataRetrievalAction].toInstance(retrieval),
      bind(classOf[Navigator]).qualifiedWith(classOf[Partnership]).toInstance(FakeNavigator),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }

}
