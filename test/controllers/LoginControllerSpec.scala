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

package controllers

import config.FrontendAppConfig
import connectors.FakeUserAnswersCacheConnector
import controllers.actions.AuthAction
import models.UserType.UserType
import models.requests.AuthenticatedRequest
import models.{NormalMode, PSAUser, UserType}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._

import scala.concurrent.Future

class LoginControllerSpec extends ControllerSpecBase {

  def loginController(appConfig: FrontendAppConfig = frontendAppConfig, userType: UserType = UserType.Organisation) = new LoginController(
    appConfig, messagesApi, FakeUserAnswersCacheConnector, fakeAuthAction(userType)
  )

  def fakeAuthAction(userType: UserType) = new AuthAction {
    override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
      block(AuthenticatedRequest(request, "id", PSAUser(userType, None, false, None)))
  }

  appRunning()

  "Login Controller" must {

    "redirect to are you in the UK page for Individual" in {

      val appConfig = new GuiceApplicationBuilder().configure(
        "features.non-uk-journeys" -> true
      ).build().injector.instanceOf[FrontendAppConfig]

      val result = loginController(appConfig, userType = UserType.Individual).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(register.individual.routes.IndividualAreYouInUKController.onPageLoad(NormalMode).url)
    }

    "redirect to are you in the UK page for organisation" in {

      val appConfig = new GuiceApplicationBuilder().configure(
        "features.non-uk-journeys" -> true
      ).build().injector.instanceOf[FrontendAppConfig]

      val result = loginController(appConfig).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(register.routes.BusinessTypeAreYouInUKController.onPageLoad(NormalMode).url)
    }
  }
}
