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

package navigators

import com.google.inject.Inject
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import identifiers.register.adviser.{AdviserNameId, ConfirmDeleteAdviserId, IsNewAdviserId}
import identifiers.register._
import models.{CheckUpdateMode, Mode, UpdateMode}
import utils.{Enumerable, Navigator, UserAnswers}

class VariationsNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                    config: FrontendAppConfig)extends Navigator with Enumerable.Implicits {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = None

  override protected def editRouteMap(from: NavigateFrom, mode: Mode): Option[NavigateTo] = from.id match {
    case VariationWorkingKnowledgeId => variationWorkingKnowledgeEditRoute(from)
    case _ => None
  }

  override protected def updateRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {

    case ConfirmDeleteAdviserId => deleteAdviserRoute(from)

    case AnyMoreChangesId => anyMoreChangesRoute(from)

    case VariationWorkingKnowledgeId => variationWorkingKnowledgeRoute(from)

    case VariationStillDeclarationWorkingKnowledgeId => variationStillWorkingKnowledgeRoute(from)

    case DeclarationFitAndProperId => declarationFitAndProperRoute(from)

    case DeclarationChangedId => declarationChange(from)

    case DeclarationId => NavigateTo.dontSave(controllers.register.routes.PSAVarianceSuccessController.onPageLoad())

    case _ => None
  }

  private def deleteAdviserRoute(from: NavigateFrom): Option[NavigateTo] = from.userAnswers.get(ConfirmDeleteAdviserId) match {
    case Some(true) => NavigateTo.dontSave(controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(UpdateMode))
    case Some(false) => NavigateTo.dontSave(controllers.routes.PsaDetailsController.onPageLoad())
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  private def anyMoreChangesRoute(from: NavigateFrom): Option[NavigateTo] = from.userAnswers.get(AnyMoreChangesId) match {
    case Some(true) => NavigateTo.dontSave(controllers.routes.PsaDetailsController.onPageLoad())
    case Some(false) => declarationChange(from)
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  private def variationWorkingKnowledgeRoute(from: NavigateFrom): Option[NavigateTo] = from.userAnswers.get(VariationWorkingKnowledgeId) match {
    case Some(true) => NavigateTo.dontSave(controllers.register.routes.VariationDeclarationFitAndProperController.onPageLoad())
    case Some(false) => NavigateTo.dontSave(controllers.register.adviser.routes.AdviserNameController.onPageLoad(UpdateMode))
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  private def variationWorkingKnowledgeEditRoute(from: NavigateFrom): Option[NavigateTo] = from.userAnswers.get(VariationWorkingKnowledgeId) match {
    case Some(true) => NavigateTo.dontSave(controllers.routes.PsaDetailsController.onPageLoad())
    case Some(false) => NavigateTo.dontSave(controllers.register.adviser.routes.AdviserNameController.onPageLoad(CheckUpdateMode))
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  private def variationStillWorkingKnowledgeRoute(from: NavigateFrom): Option[NavigateTo] =
    from.userAnswers.get(VariationStillDeclarationWorkingKnowledgeId) match {
      case Some(true) => NavigateTo.dontSave(controllers.register.routes.VariationDeclarationFitAndProperController.onPageLoad())
      case Some(false) => NavigateTo.dontSave(controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(UpdateMode))
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  private def declarationFitAndProperRoute(from: NavigateFrom): Option[NavigateTo] = from.userAnswers.get(DeclarationFitAndProperId) match {
    case Some(true) => NavigateTo.dontSave(controllers.register.routes.VariationDeclarationController.onPageLoad())
    case Some(false) => NavigateTo.dontSave(controllers.register.routes.VariationNoLongerFitAndProperController.onPageLoad())
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  private def doesAdviserStillExist(ua:UserAnswers):Boolean  =
    ua.get(AdviserNameId).isDefined && ua.get(IsNewAdviserId).isEmpty

  private def declarationChange(from: NavigateFrom): Option[NavigateTo] = {
    if (doesAdviserStillExist(from.userAnswers)) {
      NavigateTo.dontSave(controllers.register.routes.StillUseAdviserController.onPageLoad())
    } else {
      workingKonwldgeOrFnProute(from)
    }
  }

  private def workingKonwldgeOrFnProute(from: NavigateFrom): Option[NavigateTo] = from.userAnswers.get(VariationWorkingKnowledgeId) match {
    case Some(_) => NavigateTo.dontSave(controllers.register.routes.VariationDeclarationFitAndProperController.onPageLoad())
    case _ => NavigateTo.dontSave(controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(UpdateMode))
  }

}
