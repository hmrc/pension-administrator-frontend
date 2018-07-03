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

package controllers.register.partnership

import base.CSRFRequest
import connectors.{DataCacheConnector, FakeDataCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction}
import forms.register.VatFormProvider
import models.NormalMode
import org.scalatest.MustMatchers
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.mvc.{Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.Partnership
import utils.{FakeNavigator, Navigator}
import viewmodels.{Message, VatViewModel}
import views.html.register.vat

import scala.concurrent.Future

class PartnershipVatControllerSpec extends ControllerSpecBase with MustMatchers with CSRFRequest {

  import PartnershipVatControllerSpec._

  "PartnershipVatController" must {

    "render the view correctly on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.PartnershipVatController.onPageLoad(NormalMode))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe vat(frontendAppConfig, form, viewModel)(request, messages).toString()
        }
      )
    }

    "redirect to the next page on a POST request" in {
      requestResult(
        implicit App => addToken(FakeRequest(routes.PartnershipVatController.onSubmit(NormalMode))
          .withFormUrlEncodedBody(("vat.hasVat", "true"), ("vat.vat", "123456789"))),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(FakeNavigator.desiredRoute.url)
        }
      )
    }

  }

}

object PartnershipVatControllerSpec extends PartnershipVatControllerSpec {

  val form = new VatFormProvider()()

  val viewModel = VatViewModel(
    routes.PartnershipVatController.onSubmit(NormalMode),
    Message("partnershipVat.title"),
    Message("partnershipVat.heading"),
    Message("partnershipVat.hint"),
    Some(Message("site.secondaryHeader"))
  )

  private def requestResult[T](request: Application => Request[T], test: (Request[_], Future[Result]) => Unit)
                              (implicit writeable: Writeable[T]): Unit = {

    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[DataRetrievalAction].toInstance(getEmptyData),
      bind(classOf[Navigator]).qualifiedWith(classOf[Partnership]).toInstance(FakeNavigator),
      bind[DataCacheConnector].toInstance(FakeDataCacheConnector)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }

}
