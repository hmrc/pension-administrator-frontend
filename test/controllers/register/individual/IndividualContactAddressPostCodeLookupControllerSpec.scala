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
import connectors.cache.UserAnswersCacheConnector
import connectors.{AddressLookupConnector, FakeUserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction}
import forms.address.PostCodeLookupFormProvider
import models.{Mode, NormalMode, TolerantAddress}
import play.api.Application
import play.api.http.Writeable
import play.api.inject._
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.http.HeaderCarrier
import utils.annotations.Individual
import utils.{FakeNavigator, Navigator}
import viewmodels.Message
import viewmodels.address.PostcodeLookupViewModel
import views.html.address.postcodeLookup

import scala.concurrent.{ExecutionContext, Future}

class IndividualContactAddressPostCodeLookupControllerSpec extends ControllerSpecBase with CSRFRequest {

  import IndividualContactAddressPostCodeLookupControllerSpec._

  "render the view correctly on a GET request" in {
    requestResult(
      implicit app => addToken(FakeRequest(routes.IndividualContactAddressPostCodeLookupController.onPageLoad(NormalMode))),
      (request, result) => {
        status(result) mustBe OK
        contentAsString(result) mustBe postcodeLookup(frontendAppConfig, form, viewModel(NormalMode), NormalMode)(request, messages).toString()
      }
    )
  }

  "redirect to the next page on a POST request" in {
    requestResult(
      implicit App => addToken(FakeRequest(routes.IndividualContactAddressPostCodeLookupController.onSubmit(NormalMode))
        .withFormUrlEncodedBody("value" -> validPostcode)),
      (_, result) => {
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    )
  }
}

object IndividualContactAddressPostCodeLookupControllerSpec extends ControllerSpecBase {
  private val formProvider = new PostCodeLookupFormProvider()
  private val form = formProvider()
  private val validPostcode = "ZZ1 1ZZ"

  def viewModel(mode: Mode): PostcodeLookupViewModel =
    PostcodeLookupViewModel(
      routes.IndividualContactAddressPostCodeLookupController.onSubmit(mode),
      routes.IndividualContactAddressController.onPageLoad(mode),
      Message("individualContactAddressPostCodeLookup.title"),
      Message("individualContactAddressPostCodeLookup.heading"),
      Message("individualContactAddressPostCodeLookup.enterPostcode"),
      Some(Message("individualContactAddressPostCodeLookup.enterPostcode.link")),
      Message("individualContactAddressPostCodeLookup.formLabel"),
      psaName = None,
      findAddressMessageKey = "site.save_and_continue"
    )

  private val onwardRoute = controllers.register.individual.routes.IndividualContactAddressPostCodeLookupController.onPageLoad(NormalMode)
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

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)(implicit writeable: Writeable[T]): Unit = {
    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(getEmptyData),
      bind[AddressLookupConnector].toInstance(fakeAddressLookupConnector),
      bind[Navigator].qualifiedWith(classOf[Individual]).toInstance(new FakeNavigator(onwardRoute)),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }
}
