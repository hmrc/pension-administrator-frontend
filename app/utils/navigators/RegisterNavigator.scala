/*
 * Copyright 2022 HM Revenue & Customs
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

import config.FrontendAppConfig
import controllers.register.individual.routes
import identifiers.Identifier
import identifiers.register._
import javax.inject.Inject
import models.register.{BusinessType, DeclarationWorkingKnowledge, NonUKBusinessType}
import models.{Mode, NormalMode}
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

class RegisterNavigator @Inject()(appConfig: FrontendAppConfig
                                 ) extends Navigator {

  override protected def routeMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    case AreYouInUKId => countryOfRegistrationRoutes(ua)
    case RegisterAsBusinessId => individualOrOganisationRoutes(ua)
    case BusinessTypeId => businessTypeRoutes(ua)
    case NonUKBusinessTypeId => nonUkBusinessTypeRoutes(ua)
    case DeclarationWorkingKnowledgeId => declarationWorkingKnowledgeRoutes(ua)
    case DeclarationFitAndProperId => controllers.register.routes.DeclarationController.onPageLoad()
    case DeclarationId => controllers.register.routes.ConfirmationController.onPageLoad()
  }

  override protected def updateRouteMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    case _ => controllers.routes.IndexController.onPageLoad()
  }

  private def businessTypeRoutes(userAnswers: UserAnswers): Call = {
    userAnswers.get(BusinessTypeId) match {
      case Some(BusinessType.UnlimitedCompany) =>
        controllers.register.company.routes.CompanyUTRController.onPageLoad()
      case Some(BusinessType.LimitedCompany) =>
        controllers.register.company.routes.CompanyUTRController.onPageLoad()
      case Some(BusinessType.LimitedLiabilityPartnership) =>
        controllers.register.partnership.routes.PartnershipUTRController.onPageLoad()
      case Some(BusinessType.LimitedPartnership) =>
        controllers.register.partnership.routes.PartnershipUTRController.onPageLoad()
      case Some(BusinessType.BusinessPartnership) =>
        controllers.register.partnership.routes.PartnershipUTRController.onPageLoad()
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def declarationWorkingKnowledgeRoutes(userAnswers: UserAnswers): Call = {
    userAnswers.get(DeclarationWorkingKnowledgeId) match {
      case Some(DeclarationWorkingKnowledge.WorkingKnowledge) =>
        controllers.register.routes.DeclarationFitAndProperController.onPageLoad()
      case Some(DeclarationWorkingKnowledge.Adviser) =>
        controllers.register.adviser.routes.AdviserNameController.onPageLoad(NormalMode)
      case Some(DeclarationWorkingKnowledge.WhatYouWillNeed) =>
        controllers.register.company.workingknowledge.routes.WhatYouWillNeedController.onPageLoad()
      case None => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def countryOfRegistrationRoutes(userAnswers: UserAnswers): Call = {
    userAnswers.get(AreYouInUKId) match {
      case Some(false) =>
        controllers.register.routes.NonUKBusinessTypeController.onPageLoad()
      case Some(true) =>
        controllers.register.routes.BusinessTypeController.onPageLoad(NormalMode)
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def countryOfRegistrationEditRoutes(userAnswers: UserAnswers): Call =
      (userAnswers.get(AreYouInUKId), userAnswers.get(NonUKBusinessTypeId), userAnswers.get(BusinessNameId)) match {
        case (Some(false), None, _) => controllers.register.routes.RegisterAsBusinessController.onPageLoad()
        case (Some(false), Some(NonUKBusinessType.Company), Some(_)) =>
          controllers.register.company.routes.CompanyRegisteredAddressController.onPageLoad()
        case (Some(false), Some(NonUKBusinessType.BusinessPartnership), Some(_)) =>
          controllers.register.partnership.routes.PartnershipRegisteredAddressController.onPageLoad()
        case (Some(true), _, _) =>
          controllers.register.routes.RegisterAsBusinessController.onPageLoad()
        case _ => controllers.routes.SessionExpiredController.onPageLoad()
      }

  private def individualOrOganisationRoutes(userAnswers: UserAnswers): Call = {
    userAnswers.get(RegisterAsBusinessId) match {
      case None => controllers.routes.SessionExpiredController.onPageLoad()
      case Some(true) =>
        controllers.register.routes.WhatYouWillNeedController.onPageLoad(NormalMode)
      case _ =>
        routes.WhatYouWillNeedController.onPageLoad()
    }
  }

  override protected def editRouteMap(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case AreYouInUKId => countryOfRegistrationEditRoutes(ua)
  }

  private def nonUkBusinessTypeRoutes(userAnswers: UserAnswers): Call = {
    userAnswers.get(NonUKBusinessTypeId) match {
      case Some(NonUKBusinessType.Company) =>
        controllers.register.company.routes.CompanyRegisteredNameController.onPageLoad(NormalMode)
      case Some(NonUKBusinessType.BusinessPartnership) =>
        controllers.register.partnership.routes.PartnershipRegisteredNameController.onPageLoad()
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }
}
