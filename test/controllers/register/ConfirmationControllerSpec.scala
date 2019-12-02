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

package controllers.register

import connectors.cache.UserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.PsaSubscriptionResponseId
import models.register.PsaSubscriptionResponse
import models.requests.DataRequest
import models.{NormalMode, PSAUser, UserType}
import org.mockito.Matchers._
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.UserAnswers
import views.html.register.confirmation

import scala.concurrent.Future

class ConfirmationControllerSpec extends ControllerSpecBase {

  private val psaId: String = "A1234567"
  private val fakeUserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val onwardRoute = controllers.routes.LogoutController.onPageLoad()
  private val psaUser = PSAUser(UserType.Individual, None, isExistingPSA = false, None)

  val view: confirmation = app.injector.instanceOf[confirmation]

  "Confirmation Controller" must {

    "return OK and the correct view for a GET" in {
      val data = Json.obj(
        PsaSubscriptionResponseId.toString -> PsaSubscriptionResponse(psaId)
      )
      when(fakeUserAnswersCacheConnector.removeAll(any())(any(), any())) thenReturn Future.successful(Ok)
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))

      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
      verify(fakeUserAnswersCacheConnector, times(1)).removeAll(any())(any(), any())
    }

    "redirect to Session Expired on a GET when no data exists" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page on a successful POST" in {
      val result = controller().onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new ConfirmationController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeUserAnswersCacheConnector,
      stubMessagesControllerComponents(),
      view
    )

  private def viewAsString() =
    view(psaId)(DataRequest(fakeRequest, "cacheId", psaUser, UserAnswers()), messagesApi.preferred(fakeRequest)).toString
}
