/*
 * Copyright 2024 HM Revenue & Customs
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

import com.google.inject.Inject
import identifiers.{Identifier, SecondPartnerId, UpdateContactAddressId}
import identifiers.register._
import identifiers.register.adviser.AdviserNameId
import identifiers.register.adviser.ConfirmDeleteAdviserId
import models.RegistrationLegalStatus.Individual
import models.RegistrationLegalStatus.LimitedCompany
import models.RegistrationLegalStatus.Partnership
import models.CheckUpdateMode
import models.Mode
import models.UpdateMode
import play.api.mvc.Call
import utils.dataCompletion.DataCompletion
import utils.Enumerable
import utils.Navigator
import utils.UserAnswers

class VariationsNavigator @Inject()(dataCompletion: DataCompletion) extends Navigator with Enumerable.Implicits {

  override protected def routeMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    case _ => controllers.routes.IndexController.onPageLoad
  }

  override protected def editRouteMap(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case VariationWorkingKnowledgeId => variationWorkingKnowledgeEditRoute(ua)
  }

  // scalastyle:off cyclomatic.complexity
  override protected def updateRouteMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {

    case ConfirmDeleteAdviserId => deleteAdviserRoute(ua)

    case AnyMoreChangesId => anyMoreChangesRoute(ua)

    case UpdateContactAddressCYAId => updateContactAddressDeclaration(ua)

    case VariationWorkingKnowledgeId => variationWorkingKnowledgeRoute(ua)

    case VariationStillDeclarationWorkingKnowledgeId => variationStillWorkingKnowledgeRoute(ua)

    case DeclarationFitAndProperId => declarationFitAndProperRoute(ua)

    case DeclarationChangedId => declarationChange(ua)

    case SecondPartnerId => secondPartnerRoute(ua)

    case DeclarationId => controllers.register.routes.PSAVarianceSuccessController.onPageLoad()

    case UpdateContactAddressId => updateContactAddress(ua)
  }

  private def updateContactAddress(ua:UserAnswers): Call = {
    ua.get(RegistrationInfoId) match {
      case Some(regInfo) =>
        regInfo.legalStatus match {
          case LimitedCompany => controllers.register.company.routes.CompanyContactAddressPostCodeLookupController.onPageLoad(UpdateMode)
          case Individual => controllers.register.individual.routes.IndividualContactAddressPostCodeLookupController.onPageLoad(UpdateMode)
          case Partnership => controllers.register.partnership.routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(UpdateMode)
        }
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad
    }
  }

  private def deleteAdviserRoute(ua: UserAnswers): Call = ua.get(ConfirmDeleteAdviserId) match {
    case Some(true) => controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(UpdateMode)
    case Some(false) => controllers.routes.PsaDetailsController.onPageLoad()
    case _ => controllers.routes.SessionExpiredController.onPageLoad
  }

  private def anyMoreChangesRoute(ua: UserAnswers): Call = ua.get(AnyMoreChangesId) match {
    case Some(true) => controllers.routes.PsaDetailsController.onPageLoad()
    case Some(false) => declarationChange(ua)
    case _ => controllers.routes.SessionExpiredController.onPageLoad
  }

  private def variationWorkingKnowledgeRoute(ua: UserAnswers): Call = ua.get(VariationWorkingKnowledgeId) match {
    case Some(true) => controllers.register.routes.AnyMoreChangesController.onPageLoad()
    case Some(false) => controllers.register.adviser.routes.AdviserNameController.onPageLoad(UpdateMode)
    case _ => controllers.routes.SessionExpiredController.onPageLoad
  }

  private def variationWorkingKnowledgeEditRoute(ua: UserAnswers): Call = ua.get(VariationWorkingKnowledgeId) match {
    case Some(true) => controllers.register.routes.VariationDeclarationFitAndProperController.onPageLoad()
    case Some(false) => controllers.register.adviser.routes.AdviserNameController.onPageLoad(UpdateMode)
    case _ => controllers.routes.SessionExpiredController.onPageLoad
  }

  private def variationStillWorkingKnowledgeRoute(ua: UserAnswers): Call =
    ua.get(VariationStillDeclarationWorkingKnowledgeId) match {
      case Some(true) => controllers.register.routes.VariationDeclarationFitAndProperController.onPageLoad()
      case Some(false) => controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(CheckUpdateMode)
      case _ => controllers.routes.SessionExpiredController.onPageLoad
    }

  private def declarationFitAndProperRoute(ua: UserAnswers): Call = ua.get(DeclarationFitAndProperId) match {
    case Some(true) => controllers.register.routes.VariationDeclarationController.onPageLoad()
    case Some(false) => controllers.register.routes.VariationNoLongerFitAndProperController.onPageLoad()
    case _ => controllers.routes.SessionExpiredController.onPageLoad
  }

  private def declarationChange(ua: UserAnswers): Call =
    if (dataCompletion.psaUpdateDetailsInCompleteAlert(ua).nonEmpty) {
      controllers.register.routes.IncompleteChangesController.onPageLoad()
    } else {
      ua.get(DeclarationChangedId) match {
        case Some(true) => controllers.register.routes.VariationDeclarationFitAndProperController.onPageLoad()
        case _ =>
          if (ua.get(AdviserNameId).isDefined) {
            controllers.register.routes.StillUseAdviserController.onPageLoad()
          } else {
            controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(CheckUpdateMode)
          }
      }
    }

  private def updateContactAddressDeclaration(ua: UserAnswers): Call =
    dataCompletion.psaUpdateDetailsInCompleteAlert(ua) match {
      case Some("incomplete.alert.message.less.partners") => controllers.routes.SecondPartnerController.onPageLoad()
      case _ => declarationChange(ua)
    }

  private def secondPartnerRoute(ua: UserAnswers): Call =
    ua.get(SecondPartnerId) match {
      case Some(true) => controllers.register.partnership.routes.AddPartnerController.onPageLoad(UpdateMode)
      case _ => ua.get(DeclarationChangedId) match {
        case Some(true) => controllers.register.routes.VariationDeclarationFitAndProperController.onPageLoad()
        case _ =>
          if (ua.get(AdviserNameId).isDefined) {
            controllers.register.routes.StillUseAdviserController.onPageLoad()
          } else {
            controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(CheckUpdateMode)
          }
      }
  }
}
