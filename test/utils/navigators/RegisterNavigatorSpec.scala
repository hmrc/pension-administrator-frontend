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

package utils.navigators

import base.SpecBase
import connectors.FakeDataCacheConnector
import controllers.register.routes
import identifiers.Identifier
import identifiers.register.{DeclarationFitAndProperId, DeclarationId, DeclarationWorkingKnowledgeId}
import models.NormalMode
import models.register.DeclarationWorkingKnowledge
import models.requests.IdentifiedRequest
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier
import utils.{NavigatorBehaviour2, UserAnswers}

import scala.concurrent.ExecutionContext.Implicits.global

class RegisterNavigatorSpec extends SpecBase with NavigatorBehaviour2 {
  import RegisterNavigatorSpec._
  val navigator = new RegisterNavigator(FakeDataCacheConnector)

  //scalastyle:off line.size.limit
  def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                          "User Answers",                  "Next Page (Normal Mode)",       "Save(NormalMode)",  "Next Page (Check Mode)", "Save(CheckMode"),
    (DeclarationId,                 emptyAnswers,                    declarationWorkingKnowledgePage, true,                None,                     false),
    (DeclarationWorkingKnowledgeId, haveDeclarationWorkingKnowledge, declarationFitAndProperPage,     true,                None,                     false),
    (DeclarationWorkingKnowledgeId, haveAnAdviser,                   adviserDetailsPage,              true,                None,                     false),
    (DeclarationWorkingKnowledgeId, emptyAnswers,                    sessionExpiredPage,              false,               None,                     false),
    (DeclarationFitAndProperId,     emptyAnswers,                    confirmationPage,                false,               None,                     false)
  )
  //scalastyle:on line.size.limit

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routes())
  }

}

object RegisterNavigatorSpec extends OptionValues {
  lazy val emptyAnswers = UserAnswers(Json.obj())
  lazy val sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad()
  lazy val declarationWorkingKnowledgePage: Call = routes.DeclarationWorkingKnowledgeController.onPageLoad(NormalMode)
  lazy val declarationFitAndProperPage: Call = routes.DeclarationFitAndProperController.onPageLoad()
  lazy val adviserDetailsPage: Call = controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(NormalMode)
  lazy val confirmationPage: Call = routes.ConfirmationController.onPageLoad()
  lazy val surveyPage: Call = controllers.routes.LogoutController.onPageLoad()

  val haveDeclarationWorkingKnowledge: UserAnswers = UserAnswers(Json.obj())
    .set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.WorkingKnowledge).asOpt.value
  val haveAnAdviser: UserAnswers = UserAnswers(Json.obj())
    .set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.Adviser).asOpt.value

  implicit val ex: IdentifiedRequest = new IdentifiedRequest() {val externalId: String = "test-external-id"}
  implicit val hc: HeaderCarrier = HeaderCarrier()

}
