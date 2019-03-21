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
import identifiers.register.adviser.{AdviserNameId, ConfirmDeleteAdviserId}
import identifiers.register._
import models._
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
    ("Id", "User Answers", "Next Page (UpdateMode)", "Save(UpdateMode)", "Next Page (CheckUpdateMode)", "Save(CheckUpdateMode"),
    (ConfirmDeleteAdviserId, confirmDeleteYes, variationWorkingKnowledgePage(UpdateMode), false, None, false),
    (ConfirmDeleteAdviserId, confirmDeleteNo, checkYourAnswersPage, false, None, false),
    (ConfirmDeleteAdviserId, emptyAnswers, sessionExpiredPage, false, None, false),

    (AnyMoreChangesId, haveMoreChanges, checkYourAnswersPage, false, None, false),
    (AnyMoreChangesId, noMoreChangesAdviserUnchanged, variationWorkingKnowledgePage(CheckUpdateMode), false, None, false),
    (AnyMoreChangesId, noMoreChangesAdviserChanged, variationDeclarationFitAndProperPage, false, None, false),
    (AnyMoreChangesId, emptyAnswers, sessionExpiredPage, false, None, false),

    (VariationWorkingKnowledgeId, haveWorkingKnowledge, anyMoreChangesPage, false, Some(variationDeclarationFitAndProperPage), false),
    (VariationWorkingKnowledgeId, noWorkingKnowledge, adviserNamePage, false, Some(adviserNamePage), false),
    (VariationWorkingKnowledgeId, emptyAnswers, sessionExpiredPage, false, None, false),

    (VariationStillDeclarationWorkingKnowledgeId, emptyAnswers, sessionExpiredPage, false, None, false),
    (VariationStillDeclarationWorkingKnowledgeId, stillHaveWorkingKnowledge, variationDeclarationFitAndProperPage, false, None, false),
    (VariationStillDeclarationWorkingKnowledgeId, stillNotHaveWorkingKnowledge, variationWorkingKnowledgePage(CheckUpdateMode), false, None, false),

    (DeclarationFitAndProperId, haveFitAndProper, variationDeclarationPage, false, None, false),
    (DeclarationFitAndProperId, noFitAndProper, variationNoLongerFitAndProperPage, false, None, false),
    (DeclarationFitAndProperId, emptyAnswers, sessionExpiredPage, false, None, false),

    (DeclarationChangedId, declarationChangedWithIncompleteIndividual, incompleteChangesPage, false, None, false),
    (DeclarationChangedId, declarationChangedWithCompleteIndividual, variationDeclarationFitAndProperPage, false, None, false),
    (DeclarationChangedId, declarationNotChangedWithAdviser, variationStillWorkingKnowledgePage, false, None, false),
    (DeclarationChangedId, completeIndividual, variationWorkingKnowledgePage(CheckUpdateMode), false, None, false),

    (DeclarationId, emptyAnswers, variationSuccessPage, false, None, false)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes(), dataDescriber, UpdateMode)
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


