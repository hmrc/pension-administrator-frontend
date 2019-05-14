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
import connectors.{UserAnswersCacheConnector, FakeUserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.PayeFormProvider
import models.{Mode, NormalMode}
import play.api.Application
import play.api.http.Writeable
import play.api.inject.bind
import play.api.mvc.{Call, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.annotations.Partnership
import utils.{FakeNavigator, Navigator}
import viewmodels.{Message, PayeViewModel}
import views.html.paye

import scala.concurrent.Future

class PartnershipPayeControllerSpec extends ControllerSpecBase with CSRFRequest {

  import PartnershipPayeControllerSpec._

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  "Partnership Paye Controller" must {

    "render the view correctly on a GET request" in {
      requestResult(
        implicit app => addToken(FakeRequest(routes.PartnershipPayeController.onPageLoad(NormalMode))),
        (request, result) => {
          status(result) mustBe OK
          contentAsString(result) mustBe paye(frontendAppConfig, form, viewmodel(NormalMode))(request, messages).toString()
        }
      )
    }

    "redirect to the next page on a POST request" in {
      requestResult(
        implicit App => addToken(FakeRequest(routes.PartnershipPayeController.onSubmit(NormalMode))
          .withFormUrlEncodedBody("paye.hasPaye" -> "true", "paye.paye" -> "123/AB56789")),
        (_, result) => {
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      )
    }
  }
}

object PartnershipPayeControllerSpec extends ControllerSpecBase {
  private val onwardRoute = controllers.routes.IndexController.onPageLoad
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)

  def viewmodel(mode: Mode) = PayeViewModel(
    postCall = routes.PartnershipPayeController.onSubmit(mode),
    title = Message("partnershipPaye.title"),
    heading = Message("partnershipPaye.heading"),
    hint = Some("common.paye.hint")
  )

  val formProvider = new PayeFormProvider()
  val form = formProvider()

  private def requestResult[T](request: (Application) => Request[T], test: (Request[_], Future[Result]) => Unit)(implicit writeable: Writeable[T]): Unit = {

    running(_.overrides(
      bind[AuthAction].to(FakeAuthAction),
      bind[AllowAccessActionProvider].to(FakeAllowAccessProvider()),
      bind[DataRetrievalAction].toInstance(getEmptyData),
      bind(classOf[Navigator]).qualifiedWith(classOf[Partnership]).to(fakeNavigator),
      bind[UserAnswersCacheConnector].toInstance(FakeUserAnswersCacheConnector)
    )) {
      app =>
        val req = request(app)
        val result = route[T](app, req).value
        test(req, result)
    }
  }
}
