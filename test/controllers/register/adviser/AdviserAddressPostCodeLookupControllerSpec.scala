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

package controllers.register.adviser

import base.CSRFRequest
import connectors.AddressLookupConnector
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import models.{Mode, NormalMode, TolerantAddress}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{FakeNavigator, Navigator}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class AdviserAddressPostCodeLookupControllerSpec extends ControllerSpecBase with CSRFRequest {

  import AdviserAddressPostCodeLookupControllerSpec._

  "AdviserAddressPostCodeLookup Controller" must {

    "render the view correctly on a GET request" in {

      val app = application

      val request = FakeRequest(GET, routes.AdviserAddressPostCodeLookupController.onPageLoad(NormalMode).url)

      val result = route(app, request).value

      status(result) mustBe OK

      contentAsString(result) mustBe view(form, viewModel(NormalMode), NormalMode)(request, messages).toString()
    }

    "redirect to the next page on a POST request" in {
      val app = application

      val request = FakeRequest(POST, routes.AdviserAddressPostCodeLookupController.onSubmit(NormalMode).url)
        .withFormUrlEncodedBody("value" -> validPostcode)

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}

object AdviserAddressPostCodeLookupControllerSpec extends ControllerSpecBase {

  private val formProvider = new PostCodeLookupFormProvider()
  private val form = formProvider()
  private val name = "Test Adviser Name"
  private val validPostcode = "ZZ1 1ZZ"

  val view: postcodeLookup = app.injector.instanceOf[postcodeLookup]

  def viewModel(mode: Mode): PostcodeLookupViewModel = PostcodeLookupViewModel(
    controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onSubmit(mode),
    controllers.register.adviser.routes.AdviserAddressController.onPageLoad(mode),
    Message("adviserAddressPostCodeLookup.heading", Message("theAdviser")),
    Message("adviserAddressPostCodeLookup.heading", name),
    Message("adviserAddressPostCodeLookup.enterPostcode"),
    Some(Message("adviserAddressPostCodeLookup.enterPostcode.link")),
    Message("adviserAddressPostCodeLookup.formLabel"),
    psaName = None
  )

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

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()

  def application: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(getAdviser),
      bind[MessagesControllerComponents].to(stubMessagesControllerComponents()),
      bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
      bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    ).build()
}
