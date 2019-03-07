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

package utils.navigators

import base.SpecBase
import connectors.FakeUserAnswersCacheConnector
import identifiers.Identifier
import identifiers.register.{DeclarationFitAndProperId, VariationWorkingKnowledgeId}
import identifiers.vary.AnyMoreChangesId
import models.UpdateMode
import models.requests.IdentifiedRequest
import navigators.VariationsNavigator
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{NavigatorBehaviour, UserAnswers}

class VariationsNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import VariationsNavigatorSpec._

  val navigator = new VariationsNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)

  def updateRoutes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (NormalMode)", "Save(NormalMode)", "Next Page (CheckMode)", "Save(CheckMode"),
    (AnyMoreChangesId, haveMoreChanges, checkYourAnswersPage, false, None, false),
    (AnyMoreChangesId, noMoreChanges, variationWorkingKnowledgePage, false, None, false),
    (AnyMoreChangesId, emptyAnswers, sessionExpiredPage, false, None, false),
    (VariationWorkingKnowledgeId, haveWorkingKnowledge, variationDeclarationFitAndProperPage, false, None, false),
    (VariationWorkingKnowledgeId, noWorkingKnowledge, adviserDetailsPage, false, None, false),
    (VariationWorkingKnowledgeId, emptyAnswers, sessionExpiredPage, false, None, false),
    (DeclarationFitAndProperId, haveFitAndProper, variationDeclarationPage, false, None, false),
    (DeclarationFitAndProperId, noFitAndProper, variationNoLongerFitAndProperPage, false, None, false),
    (DeclarationFitAndProperId, emptyAnswers, sessionExpiredPage, false, None, false)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes(), dataDescriber, UpdateMode)
  }
}

object VariationsNavigatorSpec extends OptionValues {
  private val emptyAnswers = UserAnswers(Json.obj())

  private val haveMoreChanges: UserAnswers = UserAnswers(Json.obj()).set(AnyMoreChangesId)(true).asOpt.value
  private val noMoreChanges: UserAnswers = UserAnswers(Json.obj()).set(AnyMoreChangesId)(false).asOpt.value

  private val haveWorkingKnowledge: UserAnswers = UserAnswers(Json.obj()).set(VariationWorkingKnowledgeId)(true).asOpt.value
  private val noWorkingKnowledge: UserAnswers = UserAnswers(Json.obj()).set(VariationWorkingKnowledgeId)(false).asOpt.value

  private val haveFitAndProper: UserAnswers = UserAnswers(Json.obj()).set(DeclarationFitAndProperId)(true).asOpt.value
  private val noFitAndProper: UserAnswers = UserAnswers(Json.obj()).set(DeclarationFitAndProperId)(false).asOpt.value

  private val checkYourAnswersPage: Call = controllers.routes.PsaDetailsController.onPageLoad()
  private val variationWorkingKnowledgePage: Call = controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad()

  private val variationDeclarationFitAndProperPage: Call = controllers.register.routes.VariationDeclarationFitAndProperController.onPageLoad()
  private val adviserDetailsPage: Call = controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(UpdateMode)

  private val variationDeclarationPage: Call = controllers.register.routes.VariationDeclarationController.onPageLoad()
  private val variationNoLongerFitAndProperPage: Call = controllers.register.routes.VariationNoLongerFitAndProperController.onPageLoad()


  private val sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad()

  implicit val ex: IdentifiedRequest = new IdentifiedRequest() {
    val externalId: String = "test-external-id"
  }

}


