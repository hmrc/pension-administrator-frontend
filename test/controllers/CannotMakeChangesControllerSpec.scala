/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.actions._
import identifiers.register.individual.IndividualDetailsId
import models._
import models.requests.DataRequest
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.UserAnswers
import views.html.cannotMakeChanges

class CannotMakeChangesControllerSpec extends ControllerSpecBase {
  private val psaName: String = "Mark Wright"
  private val psaUser = PSAUser(UserType.Individual, None, false, None)

  private val individual = UserAnswers(Json.obj()).registrationInfo(RegistrationInfo(
    RegistrationLegalStatus.Individual, "", false, RegistrationCustomerType.UK, None, None))
    .set(IndividualDetailsId)(TolerantIndividual(Some("Mark"), None, Some("Wright"))).asOpt.value

  private val dataRetrievalAction = new FakeDataRetrievalAction(Some(individual.json))

  "CannotMakeChangesController" must {

    "return OK and the correct view for a GET" in {
      val result = controller(dataRetrievalAction).onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(individual)
    }

    "redirect to Session Expired on a GET when no data exists" in {
      val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new CannotMakeChangesController(
      frontendAppConfig,
      FakeAuthAction(UserType.Individual),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      stubMessagesControllerComponents(),
      view
    )

  lazy val view: cannotMakeChanges = inject[cannotMakeChanges]

  private def viewAsString(userAnswers: UserAnswers) =
    view(Some(psaName), UpdateMode)(DataRequest(fakeRequest, "cacheId", psaUser, userAnswers), messages).toString
}





