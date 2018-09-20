/*
 * Copyright 2018 HM Revenue & Customs
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
import connectors.{AddressLookupConnector, UserAnswersCacheConnector, FakeUserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import models.{NormalMode, TolerantAddress}
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.annotations.Adviser
import utils.{FakeNavigator, Navigator}
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class AdviserAddressPostCodeLookupControllerSpec extends ControllerSpecBase with CSRFRequest {

  import AdviserAddressPostCodeLookupController._
  import AdviserAddressPostCodeLookupControllerSpec._

  "AdviserAddressPostCodeLookup Controller" must {

    "render the view correctly on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.AdviserAddressPostCodeLookupController.onPageLoad(NormalMode))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe postcodeLookup(frontendAppConfig, form, viewModel(NormalMode))(request, messages).toString()
        }
      )
    }

    "redirect to the next page on a POST request" in {
      requestResult(
        implicit App => addToken(FakeRequest(routes.AdviserAddressPostCodeLookupController.onSubmit(NormalMode))
          .withFormUrlEncodedBody("value" -> validPostcode)),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      )
    }
  }
}

object AdviserAddressPostCodeLookupControllerSpec extends ControllerSpecBase {

  private val formProvider = new PostCodeLookupFormProvider()
  private val form = formProvider()

  private val validPostcode = "ZZ1 1ZZ"

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

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)(implicit writeable: Writeable[T]): Unit = {

    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(getEmptyData),
      bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
      bind(classOf[Navigator]).qualifiedWith(classOf[Adviser]).toInstance(fakeNavigator),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }
}
