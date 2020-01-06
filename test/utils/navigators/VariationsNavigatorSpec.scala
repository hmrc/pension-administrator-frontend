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

package utils.navigators

import base.SpecBase
import identifiers.Identifier
import identifiers.register._
import identifiers.register.adviser.{AdviserNameId, ConfirmDeleteAdviserId}
import models._
import models.requests.IdentifiedRequest
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor4
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{NavigatorBehaviour, UserAnswers}

class VariationsNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import VariationsNavigatorSpec._

  val navigator = new VariationsNavigator(frontendAppConfig)

  def updateRoutes(): TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id", "User Answers", "Next Page (UpdateMode)", "Next Page (CheckUpdateMode)"),
    (ConfirmDeleteAdviserId, confirmDeleteYes, variationWorkingKnowledgePage(UpdateMode), None),
    (ConfirmDeleteAdviserId, confirmDeleteNo, checkYourAnswersPage, None),
    (ConfirmDeleteAdviserId, emptyAnswers, sessionExpiredPage, None),

    (AnyMoreChangesId, haveMoreChanges, checkYourAnswersPage, None),
    (AnyMoreChangesId, noMoreChangesAdviserUnchanged, variationWorkingKnowledgePage(CheckUpdateMode), None),
    (AnyMoreChangesId, noMoreChangesAdviserChanged, variationDeclarationFitAndProperPage, None),
    (AnyMoreChangesId, emptyAnswers, sessionExpiredPage, None),

    (VariationWorkingKnowledgeId, haveWorkingKnowledge, anyMoreChangesPage, Some(variationDeclarationFitAndProperPage)),
    (VariationWorkingKnowledgeId, noWorkingKnowledge, adviserNamePage, Some(adviserNamePage)),
    (VariationWorkingKnowledgeId, emptyAnswers, sessionExpiredPage, None),

    (VariationStillDeclarationWorkingKnowledgeId, emptyAnswers, sessionExpiredPage, None),
    (VariationStillDeclarationWorkingKnowledgeId, stillHaveWorkingKnowledge, variationDeclarationFitAndProperPage, None),
    (VariationStillDeclarationWorkingKnowledgeId, stillNotHaveWorkingKnowledge, variationWorkingKnowledgePage(CheckUpdateMode), None),

    (DeclarationFitAndProperId, haveFitAndProper, variationDeclarationPage, None),
    (DeclarationFitAndProperId, noFitAndProper, variationNoLongerFitAndProperPage, None),
    (DeclarationFitAndProperId, emptyAnswers, sessionExpiredPage, None),

    (DeclarationChangedId, declarationChangedWithIncompleteIndividual, incompleteChangesPage, None),
    (DeclarationChangedId, declarationChangedWithCompleteIndividual, variationDeclarationFitAndProperPage, None),
    (DeclarationChangedId, declarationNotChangedWithAdviser, variationStillWorkingKnowledgePage, None),
    (DeclarationChangedId, completeIndividual, variationWorkingKnowledgePage(CheckUpdateMode), None),

    (DeclarationId, emptyAnswers, variationSuccessPage, None)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, updateRoutes(), dataDescriber, UpdateMode)
  }
}

object VariationsNavigatorSpec extends OptionValues {
  private val declarationChangedWithIncompleteIndividual = UserAnswers(Json.obj()).registrationInfo(
    RegistrationInfo(
      RegistrationLegalStatus.Individual, "", false, RegistrationCustomerType.UK, None, None)
  ).individualAddressYears(AddressYears.OverAYear)

  private val completeIndividual = declarationChangedWithIncompleteIndividual.variationWorkingKnowledge(true)

  private val declarationChangedWithCompleteIndividual: UserAnswers = completeIndividual.set(DeclarationChangedId)(true).asOpt.value

  private val haveMoreChanges: UserAnswers = completeIndividual.set(AnyMoreChangesId)(true).asOpt.value
  private val confirmDeleteYes: UserAnswers = UserAnswers(Json.obj()).set(ConfirmDeleteAdviserId)(true).asOpt.value
  private val confirmDeleteNo: UserAnswers = UserAnswers(Json.obj()).set(ConfirmDeleteAdviserId)(false).asOpt.value
  private val noMoreChangesAdviserUnchanged: UserAnswers = completeIndividual.set(AnyMoreChangesId)(false).asOpt.value
  private val noMoreChangesAdviserChanged: UserAnswers = completeIndividual
    .set(AnyMoreChangesId)(false).asOpt.value
    .set(DeclarationChangedId)(true).asOpt.value

  private val haveWorkingKnowledge: UserAnswers = UserAnswers(Json.obj()).set(VariationWorkingKnowledgeId)(true).asOpt.value
  private val noWorkingKnowledge: UserAnswers = UserAnswers(Json.obj()).set(VariationWorkingKnowledgeId)(false).asOpt.value

  private val stillHaveWorkingKnowledge: UserAnswers = UserAnswers(Json.obj()).set(VariationStillDeclarationWorkingKnowledgeId)(true).asOpt.value
  private val stillNotHaveWorkingKnowledge: UserAnswers = UserAnswers(Json.obj()).set(VariationStillDeclarationWorkingKnowledgeId)(false).asOpt.value

  private val declarationNotChangedWithAdviser: UserAnswers = completeIndividual
    .set(AdviserNameId)("adviser-Name").asOpt.value

  private val haveFitAndProper: UserAnswers = UserAnswers(Json.obj()).set(DeclarationFitAndProperId)(true).asOpt.value
  private val noFitAndProper: UserAnswers = UserAnswers(Json.obj()).set(DeclarationFitAndProperId)(false).asOpt.value

  private val checkYourAnswersPage: Call = controllers.routes.PsaDetailsController.onPageLoad()
  private val incompleteChangesPage: Call = controllers.register.routes.IncompleteChangesController.onPageLoad()

  private def variationWorkingKnowledgePage(mode: Mode): Call = controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(mode)

  private val variationStillWorkingKnowledgePage: Call = controllers.register.routes.StillUseAdviserController.onPageLoad()

  private val variationDeclarationFitAndProperPage: Call = controllers.register.routes.VariationDeclarationFitAndProperController.onPageLoad()
  private val adviserNamePage: Call = controllers.register.adviser.routes.AdviserNameController.onPageLoad(UpdateMode)

  private val variationDeclarationPage: Call = controllers.register.routes.VariationDeclarationController.onPageLoad()
  private val variationNoLongerFitAndProperPage: Call = controllers.register.routes.VariationNoLongerFitAndProperController.onPageLoad()

  private val variationSuccessPage: Call = controllers.register.routes.PSAVarianceSuccessController.onPageLoad()
  private val anyMoreChangesPage: Call = controllers.register.routes.AnyMoreChangesController.onPageLoad()

  private val sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad()

  implicit val ex: IdentifiedRequest = new IdentifiedRequest() {
    val externalId: String = "test-external-id"
  }

}


