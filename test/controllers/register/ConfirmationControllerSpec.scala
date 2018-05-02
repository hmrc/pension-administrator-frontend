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

package controllers.register

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.PsaSubscriptionResponseId
import models.{PSAUser, UserType}
import models.register.PsaSubscriptionResponse
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import utils.{FakeNavigator, UserAnswers}
import views.html.register.confirmation

class ConfirmationControllerSpec extends ControllerSpecBase {

  import ConfirmationControllerSpec._

  "Confirmation Controller" must {

    "return OK and the correct view for a GET" in {
      val data = Json.obj(
        PsaSubscriptionResponseId.toString -> PsaSubscriptionResponse(psaId)
      )
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))

      val result = controller(dataRetrievalAction).onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired on a GET when no data exists" in {
      val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page on a successful POST" in {
      val result = controller().onSubmit()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Session Expired on a POST when no data exists" in {
      val result = controller(dontGetAnyData).onSubmit()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

  }

}

object ConfirmationControllerSpec extends ControllerSpecBase {

  private val psaId: String = "A1234567"

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val psaUser = PSAUser(UserType.Individual, None, false, None)
  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new ConfirmationController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeNavigator
    )

  private def viewAsString() =
    confirmation(frontendAppConfig, psaId)(DataRequest(fakeRequest, "cacheId",psaUser, UserAnswers()), messages).toString


}
