/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.individual.IndividualDetailsId
import models._
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.UserAnswers
import views.html.register.incompleteChanges

class IncompleteChangesControllerSpec extends ControllerSpecBase {
  private val psaName: String = "Mark Wright"
  private val psaUser = PSAUser(UserType.Individual, None, isExistingPSA = false, None, None, "")

  private val individual = UserAnswers(Json.obj()).registrationInfo(RegistrationInfo(
    RegistrationLegalStatus.Individual, "", noIdentifier = false, RegistrationCustomerType.UK, None, None))
    .set(IndividualDetailsId)(TolerantIndividual(Some("Mark"), None, Some("Wright"))).asOpt.value

  private val dataRetrievalAction = new FakeDataRetrievalAction(Some(individual.json))

  val view: incompleteChanges = app.injector.instanceOf[incompleteChanges]

  "NoLongerFitAndProperController" must {

    "return OK and the correct view for a GET" in {
      val result = controller(dataRetrievalAction).onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(individual)
    }

    "redirect to Session Expired on a GET when no data exists" in {
      val result = controller(dontGetAnyData).onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

  private def controller(dataRetrievalAction: DataRetrievalAction) =
    new IncompleteChangesController(
      frontendAppConfig,
      FakeAuthAction(UserType.Individual),
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      FakeUserAnswersCacheConnector,
      stubMessagesControllerComponents(),
      view
    )

  private def viewAsString(userAnswers: UserAnswers) =
    view(Some(psaName), UpdateMode)(DataRequest(fakeRequest, "cacheId", psaUser, userAnswers), messages).toString
}





