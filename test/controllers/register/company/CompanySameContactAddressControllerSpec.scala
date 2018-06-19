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

package controllers.register.company

import base.CSRFRequest
import connectors.{DataCacheConnector, FakeDataCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import forms.address.SameContactAddressFormProvider
import identifiers.register.company.{BusinessDetailsId, CompanyAddressId}
import models.register.company.BusinessDetails
import models.{NormalMode, TolerantAddress}
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.RegisterCompany
import utils.{FakeNavigator2, Navigator2}
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel
import views.html.address.sameContactAddress

import scala.concurrent.Future

class CompanySameContactAddressControllerSpec extends ControllerSpecBase with CSRFRequest {

  val controller: CompanySameContactAddressController = app.injector.instanceOf[CompanySameContactAddressController]
  val formProvider: SameContactAddressFormProvider = app.injector.instanceOf[SameContactAddressFormProvider]
  val postCall: Call = routes.CompanySameContactAddressController.onSubmit(NormalMode)
  val address: TolerantAddress = TolerantAddress(Some("Add1"), Some("Add2"), None, None, None, Some("GB"))
  val companyName: String = "CompanyName"

  val dataRetrieval = new FakeDataRetrievalAction(Some(Json.obj(
    CompanyAddressId.toString -> address,
    BusinessDetailsId.toString -> BusinessDetails(companyName, "UTR")
  )))

  val viewModel = SameContactAddressViewModel(
    postCall,
    Message("individual.same.contact.address.title"),
    Message("company.same.contact.address.heading").withArgs(companyName),
    Some("site.secondaryHeader"),
    Some(Message("company.same.contact.address.hint").withArgs(companyName)),
    address
  )

  "render the view correctly on a GET request" in {
    requestResult(
      implicit app => addToken(FakeRequest(routes.CompanySameContactAddressController.onPageLoad(NormalMode))),
      (request, result) => {
        status(result) mustBe OK
        contentAsString(result) mustBe sameContactAddress(frontendAppConfig, formProvider(), viewModel)(request, messages).toString()
      }
    )
  }

  "redirect to the next page on a POST request" in {
    requestResult(
      implicit App => addToken(FakeRequest(routes.CompanySameContactAddressController.onSubmit(NormalMode))
        .withFormUrlEncodedBody("value" -> "true")),
      (_, result) => {
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(postCall.url)
      }
    )
  }

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                              (implicit writeable: Writeable[T]): Unit = {
    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(dataRetrieval),
      bind[Navigator2].qualifiedWith(classOf[RegisterCompany]).toInstance(new FakeNavigator2(postCall)),
      bind[DataCacheConnector].toInstance(FakeDataCacheConnector)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }
}
