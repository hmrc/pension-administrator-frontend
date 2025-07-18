/*
 * Copyright 2023 HM Revenue & Customs
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
import identifiers.register.individual.IndividualDetailsId
import models.requests.DataRequest
import models.{NormalMode, PSAUser, TolerantIndividual, UserType}
import org.mockito.ArgumentMatchers._
import org.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.test.Helpers._
import utils.UserAnswers
import views.html.register.psaVarianceSuccess

import scala.concurrent.Future

class PSAVarianceSuccessControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val fakeUserAnswersCacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private val psaUser = PSAUser(UserType.Individual, None, isExistingPSA = false, None, None, "")
  val view: psaVarianceSuccess = inject[psaVarianceSuccess]

  private val individual = UserAnswers(Json.obj())
    .set(IndividualDetailsId)(TolerantIndividual(Some("Mark"), None, Some("Wright"))).asOpt.value

  private val dataRetrievalAction = new FakeDataRetrievalAction(Some(individual.json))
  "NoLongerFitAndProperController" must {

    "return OK and the correct view for a GET" in {

      when(fakeUserAnswersCacheConnector.removeAll(any(), any())) thenReturn Future.successful(Ok)

      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(individual)
      verify(fakeUserAnswersCacheConnector, times(1)).removeAll(any(), any())
    }

    "redirect to Session Expired on a GET when no data exists" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }

  private def controller(dataRetrievalAction: DataRetrievalAction) =
    new PSAVarianceSuccessController(
      FakeAuthAction(UserType.Individual),
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeUserAnswersCacheConnector,
      controllerComponents,
      view
    )

  private def viewAsString(userAnswers: UserAnswers) =
    view(None)(DataRequest(fakeRequest, "cacheId", psaUser, userAnswers), messages).toString

}
