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
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction}
import models.{AddressYears, Index, NormalMode}
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.Partnership
import utils.{FakeNavigator, Navigator}
import controllers.register.partnership.routes.PartnershipAddressYearsController
import forms.address.AddressYearsFormProvider
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import scala.concurrent.Future

class PartnershipAddressYearsControllerSpec extends ControllerSpecBase with CSRFRequest {

  import PartnershipAddressYearsControllerSpec._

  val index = Index(0)

  val viewModel = AddressYearsViewModel(
    PartnershipAddressYearsController.onSubmit(NormalMode, index),
    "", "", ""
  )

  val form = new AddressYearsFormProvider()("error.required")

  "render the view correctly on a GET request" in {
    requestResult(
      implicit app => addToken(FakeRequest(PartnershipAddressYearsController.onPageLoad(NormalMode, index))),
      (_, result) => {
        status(result) mustBe OK
        contentAsString(result) mustBe addressYears(frontendAppConfig, form, viewModel)(fakeRequest, messages)
      }
    )
  }

  "redirect to the next page on a POST request" in {
    requestResult(
      implicit App => addToken(FakeRequest(PartnershipAddressYearsController.onSubmit(NormalMode, index))
        .withFormUrlEncodedBody("value" -> AddressYears.OverAYear.toString)),
      (_, result) => {
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(FakeNavigator)
      }
    )
  }
}

object PartnershipAddressYearsControllerSpec extends PartnershipAddressYearsControllerSpec {


  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                              (implicit writeable: Writeable[T]): Unit = {
    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(getEmptyData),
      bind[Navigator].qualifiedWith(classOf[Partnership]).toInstance(FakeNavigator),
      bind[DataCacheConnector].toInstance(FakeDataCacheConnector)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }

}