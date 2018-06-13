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

import connectors.FakeDataCacheConnector
import controllers.actions.AuthAction
import models.{NormalMode, PSAUser, UserType}
import models.UserType.UserType
import models.requests.AuthenticatedRequest
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._

import scala.concurrent.Future

class LoginControllerSpec extends ControllerSpecBase {

  def loginController(userType: UserType = UserType.Organisation) = new LoginController(
    frontendAppConfig, messagesApi, FakeDataCacheConnector, fakeAuthAction(userType)
  )

  def fakeAuthAction(userType: UserType) = new AuthAction {
    override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
      block(AuthenticatedRequest(request, "id", PSAUser(userType, None, false, None)))
  }

  "Login Controller" must {

    "redirect to Individual details correct page for an Individual" in {
      val result = loginController(UserType.Individual).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(register.individual.routes.IndividualDetailsCorrectController.onPageLoad(NormalMode).url)
    }

    "redirect to business type page for an Organisation" in {
      val result = loginController().onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(register.company.routes.BusinessTypeController.onPageLoad(NormalMode).url)
    }
  }
}
