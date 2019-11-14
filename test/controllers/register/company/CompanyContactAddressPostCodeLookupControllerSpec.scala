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
import connectors.cache.UserAnswersCacheConnector
import connectors.{AddressLookupConnector, FakeUserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.register.BusinessNameId
import models.{NormalMode, TolerantAddress}
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.annotations.RegisterCompany
import utils.{FakeNavigator, Navigator}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class CompanyContactAddressPostCodeLookupControllerSpec extends ControllerSpecBase with CSRFRequest {

  import CompanyContactAddressPostCodeLookupControllerSpec._

  "render the view correctly on a GET request" in {
    requestResult(
      implicit app => addToken(FakeRequest(routes.CompanyContactAddressPostCodeLookupController.onPageLoad(NormalMode))),
      (request, result) => {
        status(result) mustBe OK
        contentAsString(result) mustBe postcodeLookup(frontendAppConfig, formProvider(), viewModel, NormalMode)(request, messages).toString()
      }
    )
  }

  "redirect to the next page on a POST request" in {
    requestResult(
      implicit App => addToken(FakeRequest(routes.CompanyContactAddressPostCodeLookupController.onSubmit(NormalMode))
        .withFormUrlEncodedBody("value" -> validPostcode)),
      (_, result) => {
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    )
  }
}

object CompanyContactAddressPostCodeLookupControllerSpec extends ControllerSpecBase {
  private val formProvider = new PostCodeLookupFormProvider()
  private val validPostcode = "ZZ1 1ZZ"

  private val companyName = "CompanyName"

  private val onwardRoute = controllers.register.company.routes.CompanyContactAddressPostCodeLookupController.onPageLoad(NormalMode)
  private val address = TolerantAddress(
    Some("test-address-line-1"),
    Some("test-address-line-2"),
    None,
    None,
    Some(validPostcode),
    Some("GB")
  )

  private val fakeAddressLookupConnector = new AddressLookupConnector {
    override def addressLookupByPostCode(postcode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[TolerantAddress]] = {
      Future.successful(Seq(address))
    }
  }

  val viewModel = PostcodeLookupViewModel(
    routes.CompanyContactAddressPostCodeLookupController.onSubmit(NormalMode),
    routes.CompanyContactAddressController.onPageLoad(NormalMode),
    Message("companyContactAddressPostCodeLookup.title"),
    Message("companyContactAddressPostCodeLookup.heading").withArgs(companyName),
    Message("common.postcodeLookup.enterPostcode"),
    Some(Message("common.postcodeLookup.enterPostcode.link")),
    Message("address.postcode")
  )

  val dataRetrieval = new FakeDataRetrievalAction(Some(Json.obj(
    BusinessNameId.toString -> companyName
  )))

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)(implicit writeable: Writeable[T]): Unit = {
    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(dataRetrieval),
      bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
      bind[Navigator].qualifiedWith(classOf[RegisterCompany]).toInstance(new FakeNavigator(onwardRoute)),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }
}

