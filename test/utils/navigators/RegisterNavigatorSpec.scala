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
import identifiers.Identifier
import identifiers.register.{ConfirmationId, DeclarationFitAndProperId, DeclarationId, DeclarationWorkingKnowledgeId}
import models.NormalMode
import models.register.DeclarationWorkingKnowledge
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor4
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers
import controllers.register._

class RegisterNavigatorSpec extends SpecBase with NavigatorBehaviour {
 import RegisterNavigatorSpec._
  val navigator = new RegisterNavigator

  val routes: TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id",                              "User Answers",                       "Next Page (Normal Mode)",             "Next Page (Check Mode)"),
    (DeclarationId,                       emptyAnswers,                         declarationWorkingKnowledgePage,        None),
    (DeclarationWorkingKnowledgeId,       haveDeclarationWorkingKnowledge,      declarationFitAndProperPage,            None),
    (DeclarationWorkingKnowledgeId,       haveAnAdviser,                        adviserDetailsPage,                     None),
    (DeclarationWorkingKnowledgeId,       emptyAnswers,                         sessionExpiredPage,                     None),
    (DeclarationFitAndProperId,           emptyAnswers,                         confirmationPage,                       None)
  )

  navigator.getClass.getSimpleName must {
    behave like navigatorWithRoutes(navigator, routes)
  }
}

object RegisterNavigatorSpec extends OptionValues {
  val emptyAnswers = UserAnswers(Json.obj())
  val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  val declarationWorkingKnowledgePage = routes.DeclarationWorkingKnowledgeController.onPageLoad(NormalMode)
  val declarationFitAndProperPage = routes.DeclarationFitAndProperController.onPageLoad()
  val adviserDetailsPage = controllers.register.advisor.routes.AdvisorDetailsController.onPageLoad(NormalMode)
  val confirmationPage = routes.ConfirmationController.onPageLoad()
  val surveyPage = controllers.routes.LogoutController.onPageLoad()


  val haveDeclarationWorkingKnowledge = UserAnswers(Json.obj())
    .set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.WorkingKnowledge).asOpt.value
  val haveAnAdviser = UserAnswers(Json.obj())
    .set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.Adviser).asOpt.value
}
