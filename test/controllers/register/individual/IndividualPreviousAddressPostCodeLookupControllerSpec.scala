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

package controllers.register.individual

import base.CSRFRequest
import connectors.AddressLookupConnector
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction}
import forms.address.PostCodeLookupFormProvider
import models.{NormalMode, TolerantAddress}
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeNavigator, Navigator}
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class IndividualPreviousAddressPostCodeLookupControllerSpec extends ControllerSpecBase with CSRFRequest {

  import IndividualPreviousAddressPostCodeLookupController._
  import IndividualPreviousAddressPostCodeLookupControllerSpec._

  "IndividualPreviousAddressPostCodeLookupController" must {

    "render the view correctly on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(NormalMode))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel(NormalMode), NormalMode)(request, messages).toString()
        }
      )
    }

    "redirect to the next page on a POST request" in {
      requestResult(
        implicit App => addToken(FakeRequest(routes.IndividualPreviousAddressPostCodeLookupController.onSubmit(NormalMode))
          .withFormUrlEncodedBody("value" -> validPostcode)),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      )

    }

  }

}

object IndividualPreviousAddressPostCodeLookupControllerSpec extends ControllerSpecBase {

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
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)(implicit writeable: Writeable[T]): Unit = {

    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(getEmptyData),
      bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
      bind[Navigator].toInstance(fakeNavigator),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }

  }

}
