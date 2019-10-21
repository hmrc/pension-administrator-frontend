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
import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import controllers.register.company.routes.CompanyAddressYearsController
import forms.address.AddressYearsFormProvider
import identifiers.register.{BusinessNameId, BusinessUTRId}
import models.{AddressYears, NormalMode}
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.RegisterCompany
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.html.address.addressYears

import scala.concurrent.Future

class CompanyAddressYearsControllerSpec extends ControllerSpecBase with CSRFRequest {

  import CompanyAddressYearsControllerSpec._

  "render the view correctly on a GET request" in {
    requestResult(
      implicit app => addToken(FakeRequest(CompanyAddressYearsController.onPageLoad(NormalMode))),
      (request, result) => {
        status(result) mustBe OK
        contentAsString(result) mustBe addressYears(frontendAppConfig, form, viewModel, NormalMode)(request, messages).toString
      }
    )
  }

  "redirect to the next page on a POST request" in {
    requestResult(
      implicit App => addToken(FakeRequest(CompanyAddressYearsController.onSubmit(NormalMode))
        .withFormUrlEncodedBody("value" -> AddressYears.OverAYear.toString)),
      (_, result) => {
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(FakeNavigator.desiredRoute.url)
      }
    )
  }
}
object CompanyAddressYearsControllerSpec extends CompanyAddressYearsControllerSpec {

  val companyName = "Test Company Name"

  val dataRetrieval: DataRetrievalAction = UserAnswers()
    .set(BusinessNameId)(companyName).flatMap(_.set(BusinessUTRId)("Test UTR")).asOpt.value
    .dataRetrievalAction

  val viewModel = AddressYearsViewModel(
    CompanyAddressYearsController.onSubmit(NormalMode),
    title = Message("addressYears.heading", Message("theCompany").resolve),
    heading = Message("addressYears.heading", companyName),
    legend = Message("addressYears.heading", companyName)
  )

  val form = new AddressYearsFormProvider()(companyName)

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                              (implicit writeable: Writeable[T]): Unit = {
    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[AllowAccessActionProvider].to(FakeAllowAccessProvider()),
      bind[DataRetrievalAction].toInstance(dataRetrieval),
      bind[Navigator].qualifiedWith(classOf[RegisterCompany]).toInstance(FakeNavigator),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }

}

