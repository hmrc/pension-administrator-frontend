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

import connectors.AddressLookupConnector
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.register.BusinessNameId
import models.{NormalMode, TolerantAddress}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.annotations.RegisterCompany
import utils.{FakeNavigator, Navigator}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class CompanyContactAddressPostCodeLookupControllerSpec extends ControllerSpecBase {

  import CompanyContactAddressPostCodeLookupControllerSpec._

  "render the view correctly on a GET request" in {
    val request = FakeRequest(routes.CompanyContactAddressPostCodeLookupController.onPageLoad(NormalMode))
    val result = route(application, request).value
        status(result) mustBe OK
        contentAsString(result) mustBe view(formProvider(), viewModel, NormalMode)(request, messages).toString()

  }

  "redirect to the next page on a POST request" in {
    val request = FakeRequest(routes.CompanyContactAddressPostCodeLookupController.onSubmit(NormalMode))
        .withFormUrlEncodedBody("value" -> validPostcode)
    val result = route(application, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)

  }

}

object CompanyContactAddressPostCodeLookupControllerSpec extends ControllerSpecBase {
  private val formProvider = new PostCodeLookupFormProvider()
  private val validPostcode = "ZZ1 1ZZ"

  val view: postcodeLookup = app.injector.instanceOf[postcodeLookup]

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
    override def addressLookupByPostCode(postcode: String)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[Seq[TolerantAddress]] = {
      Future.successful(Seq(address))
    }
  }

  val viewModel = PostcodeLookupViewModel(
    routes.CompanyContactAddressPostCodeLookupController.onSubmit(NormalMode),
    routes.CompanyContactAddressController.onPageLoad(NormalMode),
    Message("contactAddressPostCodeLookup.heading", Message("theCompany")),
    Message("contactAddressPostCodeLookup.heading").withArgs(companyName),
    Message("common.postcodeLookup.enterPostcode"),
    Some(Message("common.postcodeLookup.enterPostcode.link")),
    Message("address.postcode")
  )

  val dataRetrieval = new FakeDataRetrievalAction(Some(Json.obj(
    BusinessNameId.toString -> companyName
  )))

  def application: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(dataRetrieval),
      bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
      bind[Navigator].qualifiedWith(classOf[RegisterCompany]).toInstance(new FakeNavigator(onwardRoute)),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
      bind[MessagesControllerComponents].to(stubMessagesControllerComponents())
    ).build()
}

