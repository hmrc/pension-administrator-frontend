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

package controllers.register.partnership

import base.CSRFRequest
import connectors.AddressLookupConnector
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.PostCodeLookupFormProvider
import models._
import models.requests.DataRequest
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.annotations.Partnership
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class PartnershipPreviousAddressPostCodeLookupControllerSpec extends ControllerSpecBase with CSRFRequest {

  implicit val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), "cacheId",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None), UserAnswers())

  import PartnershipPreviousAddressPostCodeLookupControllerSpec._

  "PartnershipPreviousAddressPostCodeLookupController" must {

    "render the view correctly on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(NormalMode))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe view(form, viewModel(NormalMode), NormalMode)(request, messages).toString()
        }
      )
    }

    "redirect to the next page on a POST request" in {
      requestResult(
        implicit App => addToken(FakeRequest(routes.PartnershipPreviousAddressPostCodeLookupController.onSubmit(NormalMode))
          .withFormUrlEncodedBody("value" -> validPostcode)),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      )

    }

  }

}

object PartnershipPreviousAddressPostCodeLookupControllerSpec extends ControllerSpecBase {

  implicit val request: DataRequest[AnyContent] =
    DataRequest(fakeRequest, "", PSAUser(UserType.Individual, None, isExistingPSA = false, None, None), UserAnswers())
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

  private val onwardRoute = controllers.routes.SessionExpiredController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)(implicit writeable: Writeable[T]): Unit = {

    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[AllowAccessActionProvider].to(FakeAllowAccessProvider()),
      bind[DataRetrievalAction].toInstance(getPartnership),
      bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
      bind(classOf[Navigator]).qualifiedWith(classOf[Partnership]).to(fakeNavigator),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }

  def viewModel(mode: Mode)(implicit request: DataRequest[AnyContent]) = PostcodeLookupViewModel(
    routes.PartnershipPreviousAddressPostCodeLookupController.onSubmit(mode),
    routes.PartnershipPreviousAddressController.onPageLoad(mode),
    Message("previousAddressPostCode.heading", Message("thePartnership").resolve),
    Message("previousAddressPostCode.heading", "Test Partnership Name"),
    Message("common.previousAddress.enterPostcode"),
    Some(Message("common.previousAddress.enterPostcode.link")),
    Message("common.address.enterPostcode.formLabel"),
    None
  )
}


