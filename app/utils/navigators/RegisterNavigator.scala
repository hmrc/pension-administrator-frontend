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

import config.{FeatureSwitchManagementService, FrontendAppConfig}
import connectors.UserAnswersCacheConnector
import controllers.register.individual.routes
import identifiers.register._
import identifiers.register.company.BusinessDetailsId
import identifiers.register.partnership.PartnershipDetailsId
import javax.inject.Inject
import models.register.{BusinessType, DeclarationWorkingKnowledge, NonUKBusinessType}
import models.{Mode, NormalMode}
import utils.{Navigator, UserAnswers}

class RegisterNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                  appConfig: FrontendAppConfig
                                 ) extends Navigator {

  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case AreYouInUKId => countryOfRegistrationRoutes(from.userAnswers)
    case RegisterAsBusinessId => individualOrOganisationRoutes(from.userAnswers)
    case BusinessTypeId => businessTypeRoutes(from.userAnswers)
    case NonUKBusinessTypeId => nonUkBusinessTypeRoutes(from.userAnswers)
    case DeclarationId => NavigateTo.save(controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(NormalMode))
    case DeclarationWorkingKnowledgeId => declarationWorkingKnowledgeRoutes(from.userAnswers)
    case DeclarationFitAndProperId => NavigateTo.dontSave(controllers.register.routes.ConfirmationController.onPageLoad())
    case _ => None
  }

  override protected def updateRouteMap(from: NavigateFrom): Option[NavigateTo] = None

  private def businessTypeRoutes(userAnswers: UserAnswers): Option[NavigateTo] = {
    userAnswers.get(BusinessTypeId) match {
      case Some(BusinessType.UnlimitedCompany) =>
        NavigateTo.dontSave(controllers.register.company.routes.CompanyUTRController.onPageLoad())
      case Some(BusinessType.LimitedCompany) =>
        NavigateTo.dontSave(controllers.register.company.routes.CompanyUTRController.onPageLoad())
      case Some(BusinessType.LimitedLiabilityPartnership) =>
        NavigateTo.dontSave(controllers.register.partnership.routes.PartnershipBusinessDetailsController.onPageLoad())
      case Some(BusinessType.LimitedPartnership) =>
        NavigateTo.dontSave(controllers.register.partnership.routes.PartnershipBusinessDetailsController.onPageLoad())
      case Some(BusinessType.BusinessPartnership) =>
        NavigateTo.dontSave(controllers.register.partnership.routes.PartnershipBusinessDetailsController.onPageLoad())
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def declarationWorkingKnowledgeRoutes(userAnswers: UserAnswers): Option[NavigateTo] = {
    userAnswers.get(DeclarationWorkingKnowledgeId) match {
      case Some(DeclarationWorkingKnowledge.WorkingKnowledge) =>
        NavigateTo.save(controllers.register.routes.DeclarationFitAndProperController.onPageLoad())
      case Some(DeclarationWorkingKnowledge.Adviser) =>
        NavigateTo.save(controllers.register.adviser.routes.AdviserNameController.onPageLoad(NormalMode))
      case None => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def countryOfRegistrationRoutes(userAnswers: UserAnswers): Option[NavigateTo] = {
    userAnswers.get(AreYouInUKId) match {
      case Some(false) =>
        NavigateTo.dontSave(controllers.register.routes.NonUKBusinessTypeController.onPageLoad())
      case Some(true) =>
        NavigateTo.dontSave(controllers.register.routes.BusinessTypeController.onPageLoad(NormalMode))
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def countryOfRegistrationEditRoutes(userAnswers: UserAnswers): Option[NavigateTo] =
    NavigateTo.dontSave(
      (userAnswers.get(AreYouInUKId), userAnswers.get(NonUKBusinessTypeId), userAnswers.get(BusinessNameId), userAnswers.get(PartnershipDetailsId)) match {
        case (Some(false), None, _, _) => controllers.register.routes.RegisterAsBusinessController.onPageLoad()
        case (Some(false), Some(NonUKBusinessType.Company), Some(_), _) =>
          controllers.register.company.routes.CompanyRegisteredAddressController.onPageLoad()
        case (Some(false), Some(NonUKBusinessType.BusinessPartnership), _, Some(_)) =>
          controllers.register.partnership.routes.PartnershipRegisteredAddressController.onPageLoad()
        case (Some(true), _, _, _) =>
          controllers.register.routes.RegisterAsBusinessController.onPageLoad()
        case _ => controllers.routes.SessionExpiredController.onPageLoad()
      }
    )

  private def individualOrOganisationRoutes(userAnswers: UserAnswers): Option[NavigateTo] = {
    userAnswers.get(RegisterAsBusinessId) match {
      case None => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
      case Some(true) =>
        NavigateTo.dontSave(controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(NormalMode))
      case _ =>
        NavigateTo.dontSave(routes.IndividualAreYouInUKController.onPageLoad(NormalMode))
    }
  }

  override protected def editRouteMap(from: NavigateFrom, mode: Mode): Option[NavigateTo] = from.id match {
    case AreYouInUKId => countryOfRegistrationEditRoutes(from.userAnswers)
    case _ => None
  }

  private def nonUkBusinessTypeRoutes(userAnswers: UserAnswers): Option[NavigateTo] = {
    userAnswers.get(NonUKBusinessTypeId) match {
      case Some(NonUKBusinessType.Company) =>
        NavigateTo.dontSave(controllers.register.company.routes.CompanyRegisteredNameController.onPageLoad(NormalMode))
      case Some(NonUKBusinessType.BusinessPartnership) =>
        NavigateTo.dontSave(controllers.register.partnership.routes.PartnershipRegisteredNameController.onPageLoad())
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
