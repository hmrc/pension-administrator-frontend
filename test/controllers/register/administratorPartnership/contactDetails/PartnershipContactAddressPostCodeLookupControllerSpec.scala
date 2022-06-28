/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.register.administratorPartnership.contactDetails

import connectors.AddressLookupConnector
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import identifiers.register.BusinessNameId
import models.{NormalMode, TolerantAddress}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.libs.json.Json
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.annotations.Partnership
import utils.{FakeNavigator, Navigator}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class PartnershipContactAddressPostCodeLookupControllerSpec extends ControllerSpecBase {


  private val partnershipName = "PartnershipName"

  val view: postcodeLookup = app.injector.instanceOf[postcodeLookup]
  def viewModel = PostcodeLookupViewModel(
    routes.PartnershipContactAddressPostCodeLookupController.onSubmit(NormalMode),
    routes.PartnershipContactAddressController.onPageLoad(NormalMode),
    Message("postcode.lookup.heading", Message("thePartnership")),
    Message("postcode.lookup.heading").withArgs(partnershipName),
    Message("manual.entry.text"),
    Some(Message("manual.entry.link")),
    Message("postcode.lookup.form.label"),
    partnershipName = Some(partnershipName),
    displayPartnershipLink = true
  )
  val dataRetrieval = new FakeDataRetrievalAction(Some(Json.obj(
    BusinessNameId.toString -> partnershipName
  )))
  private val formProvider = new PostCodeLookupFormProvider()
  private val validPostcode = "ZZ1 1ZZ"

  private val onwardRoute = routes.PartnershipContactAddressListController.onPageLoad(NormalMode)
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


  "render the view correctly on a GET request" in {
    val request = addCSRFToken(FakeRequest(routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(NormalMode)))
    val result = route(application, request).value
    status(result) mustBe OK
    contentAsString(result) mustBe view(formProvider(), viewModel, NormalMode)(request, messagesApi.preferred(fakeRequest)).toString()

  }

  "redirect to the next page on a POST request" in {
    running(_.overrides(modules(dataRetrieval)++
      Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[Partnership]).toInstance(new FakeNavigator(onwardRoute)),
        bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
        bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector)
      ):_*)) {
      app =>
        val controller = app.injector.instanceOf[PartnershipContactAddressPostCodeLookupController]

        val request = FakeRequest().withFormUrlEncodedBody("value" -> validPostcode)

        val result = controller.onSubmit(NormalMode)(request)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }

  def application: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[AllowAccessActionProvider].to(FakeAllowAccessProvider(config = frontendAppConfig)),
      bind[DataRetrievalAction].toInstance(dataRetrieval),
      bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
      bind[Navigator].qualifiedWith(classOf[Partnership]).toInstance(new FakeNavigator(onwardRoute)),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    ).build()
}



