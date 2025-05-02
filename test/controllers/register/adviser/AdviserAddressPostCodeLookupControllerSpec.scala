/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import forms.address.PostCodeLookupFormProvider
import models.{Mode, NormalMode, TolerantAddress}
import org.mockito.ArgumentMatchers.any
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.Adviser
import utils.{FakeNavigator, Navigator}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup
import org.mockito.Mockito.when

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
  private val onwardRoute = controllers.routes.IndexController.onPageLoad

  "AdviserAddressPostCodeLookup Controller" must {

    "render the view correctly on a GET request" in {

      val view: postcodeLookup = app.injector.instanceOf[postcodeLookup]

      val request = addCSRFToken(FakeRequest(GET, routes.AdviserAddressPostCodeLookupController.onPageLoad(NormalMode).url))

      val result = route(app, request).value

      status(result) mustBe OK

      contentAsString(result) mustBe view(form, viewModel(NormalMode), NormalMode)(request, messagesApi.preferred(request)).toString()


    }

    "redirect to the next page on a POST request" in {
      running(_.overrides(modules(getEmptyData)++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[Adviser]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
          bind[AddressLookupConnector].toInstance(mockAddressLookupConnector)
        )*)) {
        app =>
          when(mockAddressLookupConnector.addressLookupByPostCode(any())(any(), any())).thenReturn(Future.successful(Seq(address)))
          val controller = app.injector.instanceOf[AdviserAddressPostCodeLookupController]

          val request = FakeRequest().withFormUrlEncodedBody("value" -> "ZZ1 1ZZ")

          val result = controller.onSubmit(NormalMode)(request)

          status(result) mustBe SEE_OTHER

          redirectLocation(result) mustBe Some(onwardRoute.url)

      }
    }
  }

  def viewModel(mode: Mode): PostcodeLookupViewModel = PostcodeLookupViewModel(
    controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onSubmit(mode),
    controllers.register.adviser.routes.AdviserAddressController.onPageLoad(mode),
    Message("postcode.lookup.heading", Message("theAdviser")),
    Message("postcode.lookup.heading", "Test Adviser Name"),
    Message("manual.entry.text"),
    Some(Message("manual.entry.link")),
    psaName = Some(companyName),
    returnLink = Some(controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad().url)
  )

  override lazy val app: Application =
    applicationBuilder(getCompanyAndAdvisor).overrides(
      bind[AddressLookupConnector].toInstance(mockAddressLookupConnector),
      bind[Navigator].qualifiedWith(classOf[Adviser]).toInstance(new FakeNavigator(onwardRoute))
    ).build()
}
