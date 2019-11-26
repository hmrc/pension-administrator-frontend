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

import connectors.AddressLookupConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import models.{Mode, NormalMode, TolerantAddress}
import org.mockito.Matchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.Future

class AdviserAddressPostCodeLookupControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val mockAddressLookupConnector: AddressLookupConnector = mock[AddressLookupConnector]

  private val formProvider = new PostCodeLookupFormProvider()
  private val form = formProvider()
  private val address = TolerantAddress(
    Some("test-address-line-1"),
    Some("test-address-line-2"),
    None,
    None,
    Some("ZZ1 1ZZ"),
    Some("GB")
  )
  private val onwardRoute = controllers.routes.IndexController.onPageLoad()

  "AdviserAddressPostCodeLookup Controller" must {

    "render the view correctly on a GET request" in {

      val app = application

      val view: postcodeLookup = app.injector.instanceOf[postcodeLookup]

      val request = FakeRequest(GET, routes.AdviserAddressPostCodeLookupController.onPageLoad(NormalMode).url)

      val result = route(app, request).value

      status(result) mustBe OK

      contentAsString(result) mustBe view(form, viewModel(NormalMode), NormalMode)(fakeRequest, messages).toString()

      app.stop()
    }

    "redirect to the next page on a POST request" in {
      when(mockAddressLookupConnector.addressLookupByPostCode(any())(any(), any())) thenReturn Future.successful(Seq(address))

      val app = application

      val request = FakeRequest(POST, routes.AdviserAddressPostCodeLookupController.onSubmit(NormalMode).url)
        .withFormUrlEncodedBody("value" -> "ZZ1 1ZZ")

      val result = route(app, request).value

      status(result) mustBe SEE_OTHER

      redirectLocation(result) mustBe Some(onwardRoute.url)

      app.stop()
    }
  }

  def viewModel(mode: Mode): PostcodeLookupViewModel = PostcodeLookupViewModel(
    controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onSubmit(mode),
    controllers.register.adviser.routes.AdviserAddressController.onPageLoad(mode),
    Message("adviserAddressPostCodeLookup.heading", Message("theAdviser")),
    Message("adviserAddressPostCodeLookup.heading", "Test Adviser Name"),
    Message("adviserAddressPostCodeLookup.enterPostcode"),
    Some(Message("adviserAddressPostCodeLookup.enterPostcode.link")),
    Message("adviserAddressPostCodeLookup.formLabel"),
    psaName = None
  )

  def application: Application =
    applicationBuilder(getAdviser).overrides(
      bind[AuthAction].toInstance(FakeAuthAction),
      bind[AddressLookupConnector].toInstance(mockAddressLookupConnector),
      bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
    ).build()
}
