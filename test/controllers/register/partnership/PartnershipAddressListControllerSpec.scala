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

package controllers.register.partnership

import base.CSRFRequest
import connectors.{DataCacheConnector, FakeDataCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import forms.address.AddressListFormProvider
import identifiers.register.partnership.{PartnershipContactAddressListId, PartnershipDetailsId}
import models.requests.{AuthenticatedRequest, OptionalDataRequest}
import models.{BusinessDetails, Index, NormalMode}
import org.scalatest.MustMatchers
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator}
import utils.annotations.Partnership
import viewmodels.address.AddressListViewModel
import views.html.address.addressList

import scala.concurrent.Future

class PartnershipAddressListControllerSpec extends ControllerSpecBase with MustMatchers with CSRFRequest {

  import PartnershipAddressListControllerSpec._

  "PartnershipAddressListController" must {

    "render the view correctly on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.PartnershipAddressListController.onPageLoad(NormalMode, firstIndex))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe addressList(frontendAppConfig, form, viewModel)(request, messages).toString()
        }
      )
    }

    "redirect to the next page on a POST request" in {
      requestResult(
        implicit App => addToken(FakeRequest(routes.PartnershipAddressListController.onSubmit(NormalMode, firstIndex))
          .withFormUrlEncodedBody("value" -> "2")),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(FakeNavigator.desiredRoute)
        }
      )
    }

  }

}

object PartnershipAddressListControllerSpec extends PartnershipAddressListControllerSpec {

  val firstIndex = Index(0)

  val form = new AddressListFormProvider()(Seq.empty)

  val testName = "Partnership Name"

  val viewModel = AddressListViewModel(
    routes.PartnershipAddressListController.onSubmit(NormalMode, firstIndex),
    routes.PartnershipAddressListController.onSubmit(NormalMode, firstIndex),
    Seq.empty,
    subHeading = Some(testName)
  )

  val retrieval = new FakeDataRetrievalAction(Some(Json.obj(
    "partnership" -> Json.arr(
      Json.obj(
        PartnershipDetailsId.toString -> BusinessDetails(
          testName, "UTR"
        )
      )
    )
  )))

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                              (implicit writeable: Writeable[T]): Unit = {

    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(retrieval),
      bind(classOf[Navigator]).qualifiedWith(classOf[Partnership]).toInstance(FakeNavigator),
      bind[DataCacheConnector].toInstance(FakeDataCacheConnector)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }

}