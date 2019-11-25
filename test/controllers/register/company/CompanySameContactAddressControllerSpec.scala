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
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import forms.address.SameContactAddressFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.company.CompanyAddressId
import models.{NormalMode, TolerantAddress}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.RegisterCompany
import utils.countryOptions.CountryOptions
import utils.{FakeNavigator, Navigator}
import viewmodels.Message
import viewmodels.address.SameContactAddressViewModel
import views.html.address.sameContactAddress

class CompanySameContactAddressControllerSpec extends ControllerSpecBase with CSRFRequest {

  val controller: CompanySameContactAddressController = app.injector.instanceOf[CompanySameContactAddressController]
  val formProvider: SameContactAddressFormProvider = app.injector.instanceOf[SameContactAddressFormProvider]
  val postCall: Call = routes.CompanySameContactAddressController.onSubmit(NormalMode)
  val address: TolerantAddress = TolerantAddress(Some("Add1"), Some("Add2"), None, None, None, Some("GB"))
  val companyName: String = "CompanyName"

  val view: sameContactAddress = app.injector.instanceOf[sameContactAddress]

  val dataRetrieval = new FakeDataRetrievalAction(Some(Json.obj(
    CompanyAddressId.toString -> address,
    BusinessNameId.toString -> companyName
  )))

  val viewModel = SameContactAddressViewModel(
    postCall,
    Message("company.same.contact.address.title"),
    Message("company.same.contact.address.heading").withArgs(companyName),
    None,
    address,
    "Test name",
    NormalMode
  )

  val countryOptions = new CountryOptions(environment, frontendAppConfig)

  "render the view correctly on a GET request" in {
    val request = FakeRequest(routes.CompanySameContactAddressController.onPageLoad(NormalMode))
    val result = route(application, request).value
        status(result) mustBe OK
        contentAsString(result) mustBe view(formProvider(), viewModel, countryOptions)(request, messages).toString()

  }

  "redirect to the next page on a POST request" in {
    val request = FakeRequest(routes.CompanySameContactAddressController.onSubmit(NormalMode))
        .withFormUrlEncodedBody("value" -> "true")
    val result = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(postCall.url)

  }

  def application: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(dataRetrieval),
      bind[Navigator].qualifiedWith(classOf[RegisterCompany]).toInstance(new FakeNavigator(postCall)),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    ).build()

}
