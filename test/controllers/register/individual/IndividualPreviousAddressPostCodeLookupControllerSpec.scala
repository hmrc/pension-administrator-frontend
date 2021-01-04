/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers.register.individual

import connectors.AddressLookupConnector
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction}
import forms.address.PostCodeLookupFormProvider
import models.{NormalMode, TolerantAddress}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import play.api.test.CSRFTokenHelper.addCSRFToken
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.annotations.Individual
import utils.{FakeNavigator, Navigator}
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class IndividualPreviousAddressPostCodeLookupControllerSpec extends ControllerSpecBase {

  import IndividualPreviousAddressPostCodeLookupController._

  private val formProvider = new PostCodeLookupFormProvider()
  private val form = formProvider()

  private val validPostcode = "ZZ1 1ZZ"

  val view: postcodeLookup = app.injector.instanceOf[postcodeLookup]


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

  private val onwardRoute = controllers.register.individual.routes.IndividualPreviousAddressListController.onPageLoad(NormalMode)

  "IndividualPreviousAddressPostCodeLookupController" must {

    "render the view correctly on a GET request" in {
      running(_.overrides(modules(getEmptyData)++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[Individual]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
          bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector)
        ):_*)) {
        app =>
          val controller = app.injector.instanceOf[IndividualPreviousAddressPostCodeLookupController]

          val request = addCSRFToken(FakeRequest(routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(NormalMode)))

          val result = controller.onPageLoad(NormalMode)(request)

          status(result) mustBe OK

          contentAsString(result) mustBe view(form, viewModel(NormalMode), NormalMode)(request, messagesApi.preferred(fakeRequest)).toString()
      }
    }

    "redirect to the next page on a POST request" in {
      running(_.overrides(modules(getEmptyData)++
        Seq[GuiceableModule](bind[Navigator].qualifiedWith(classOf[Individual]).toInstance(new FakeNavigator(onwardRoute)),
          bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector),
          bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector)
        ):_*)) {
        app =>
          val controller = app.injector.instanceOf[IndividualPreviousAddressPostCodeLookupController]

          val request = FakeRequest().withFormUrlEncodedBody("value" -> validPostcode)

          val result = controller.onSubmit(NormalMode)(request)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}
