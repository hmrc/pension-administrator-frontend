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

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import controllers.register.company.routes
import identifiers.{Identifier, UpdateContactAddressId}
import identifiers.register.company.{CompanyPhoneId, _}
import identifiers.register.{AreYouInUKId, BusinessTypeId, _}
import models.InternationalRegion._
import models._
import models.register.BusinessType
import play.api.mvc.Call
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}

@Singleton
class RegisterCompanyNavigator @Inject()(countryOptions: CountryOptions,
                                         appConfig: FrontendAppConfig) extends Navigator {

//  scalastyle:off cyclomatic.complexity
// scalastyle:off method.length
  override protected def routeMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    case BusinessUTRId =>
      routes.CompanyNameController.onPageLoad()
    case BusinessNameId =>
      regionBasedNameNavigation(ua)
    case IsRegisteredNameId =>
      registeredNameRoutes(ua)
    case ConfirmCompanyAddressId =>
      crnNavigation(ua)
    case HasCompanyCRNId if hasCrn(ua) =>
      routes.CompanyRegistrationNumberController.onPageLoad(NormalMode)
    case HasCompanyCRNId =>
      routes.HasCompanyPAYEController.onPageLoad(NormalMode)
    case CompanyRegistrationNumberId =>
      routes.HasCompanyPAYEController.onPageLoad(NormalMode)
    case HasPAYEId if hasPaye(ua) =>
      routes.CompanyEnterPAYEController.onPageLoad(NormalMode)
    case HasPAYEId =>
      routes.HasCompanyVATController.onPageLoad(NormalMode)
    case EnterPAYEId =>
      routes.HasCompanyVATController.onPageLoad(NormalMode)
    case HasVATId if hasVat(ua) =>
      routes.CompanyEnterVATController.onPageLoad(NormalMode)
    case HasVATId =>
      routes.CompanySameContactAddressController.onPageLoad(NormalMode)
    case EnterVATId =>
      routes.CompanySameContactAddressController.onPageLoad(NormalMode)
    case CompanySameContactAddressId =>
      sameContactAddress(NormalMode, ua)
    case CompanyContactAddressPostCodeLookupId =>
      routes.CompanyContactAddressListController.onPageLoad(NormalMode)
    case CompanyContactAddressId =>
      routes.CompanyAddressYearsController.onPageLoad(NormalMode)
    case CompanyAddressYearsId =>
      companyAddressYearsIdRoutes(ua)
    case CompanyTradingOverAYearId =>
      hasBeenTradingIdRoutes(ua)
    case CompanyPreviousAddressPostCodeLookupId =>
      routes.CompanyAddressListController.onPageLoad(NormalMode)
    case CompanyPreviousAddressId =>
      routes.CompanyEmailController.onPageLoad(NormalMode)
    case CompanyEmailId =>
      routes.CompanyPhoneController.onPageLoad(NormalMode)
    case CompanyPhoneId =>
      routes.CheckYourAnswersController.onPageLoad()
    case CheckYourAnswersId =>
      directorRoutes(ua, NormalMode)
    case CompanyReviewId =>
      controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(NormalMode)
    case CompanyAddressId =>
      regionBasedNavigation(ua)
    case WhatYouWillNeedId =>
      routes.CompanySameContactAddressController.onPageLoad(NormalMode)
  }

  override protected def editRouteMap(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case HasCompanyCRNId if hasCrn(ua) =>
      routes.CompanyRegistrationNumberController.onPageLoad(CheckMode)
    case HasCompanyCRNId =>
      checkYourAnswers
    case CompanyRegistrationNumberId =>
      checkYourAnswers
    case HasPAYEId if hasPaye(ua) =>
      routes.CompanyEnterPAYEController.onPageLoad(CheckMode)
    case HasPAYEId =>
      checkYourAnswers
    case EnterPAYEId =>
      checkYourAnswers
    case HasVATId if hasVat(ua) =>
      routes.CompanyEnterVATController.onPageLoad(CheckMode)
    case HasVATId =>
      checkYourAnswers
    case EnterVATId =>
      checkYourAnswers
    case CompanySameContactAddressId =>
      sameContactAddress(CheckMode, ua)
    case CompanyContactAddressPostCodeLookupId =>
      routes.CompanyContactAddressListController.onPageLoad(CheckMode)
    case CompanyContactAddressId =>
      checkYourAnswers
    case CompanyAddressYearsId =>
      companyAddressYearsCheckIdRoutes(ua)
    case CompanyTradingOverAYearId =>
      hasBeenTradingCheckIdRoutes(ua)
    case CompanyPreviousAddressPostCodeLookupId =>
      routes.CompanyAddressListController.onPageLoad(CheckMode)
    case CompanyPreviousAddressId =>
      checkYourAnswers
    case CompanyEmailId =>
      checkYourAnswers
    case CompanyPhoneId =>
      checkYourAnswers
  }

  override protected def updateRouteMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    case CompanyContactAddressPostCodeLookupId =>
      routes.CompanyContactAddressListController.onPageLoad(UpdateMode)
    case CompanyContactAddressId =>
      routes.CompanyConfirmPreviousAddressController.onPageLoad()
    case CompanyAddressYearsId =>
      companyAddressYearsUpdateIdRoutes(ua)
    case CompanyConfirmPreviousAddressId =>
      confirmPreviousAddressRoutes(ua)
    case CompanyPreviousAddressPostCodeLookupId =>
      routes.CompanyAddressListController.onPageLoad(UpdateMode)
    case CompanyAddressListId =>
      routes.CompanyPreviousAddressController.onPageLoad(UpdateMode)
    case CompanyPreviousAddressId => finishAmendmentNavigation(ua)
    case CompanyEmailId => finishAmendmentNavigation(ua)
    case CompanyPhoneId => finishAmendmentNavigation(ua)
  }

  private def finishAmendmentNavigation(answers: UserAnswers): Call = {
    answers.get(UpdateContactAddressId) match {
      case Some(_) => updateContactAddressCYAPage()
      case _ => anyMoreChanges
    }
  }

  private def updateContactAddressCYAPage():Call = controllers.routes.UpdateContactAddressCYAController.onPageLoad()
  private def checkYourAnswers: Call =
    controllers.register.company.routes.CheckYourAnswersController.onPageLoad()

  private def hasPaye(answers: UserAnswers): Boolean = answers.get(HasPAYEId).getOrElse(false)

  private def hasCrn(answers: UserAnswers): Boolean = answers.get(HasCompanyCRNId).getOrElse(false)

  private def hasVat(answers: UserAnswers): Boolean = answers.get(HasVATId).getOrElse(false)

  private def anyMoreChanges: Call = controllers.register.routes.AnyMoreChangesController.onPageLoad()

  private def companyAddressYearsIdRoutes(answers: UserAnswers): Call = {
    answers.get(CompanyAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        routes.CompanyTradingOverAYearController.onPageLoad(NormalMode)
      case Some(AddressYears.OverAYear) =>
        routes.CompanyEmailController.onPageLoad(NormalMode)
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def companyAddressYearsCheckIdRoutes(answers: UserAnswers): Call = {
    answers.get(CompanyAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        routes.CompanyTradingOverAYearController.onPageLoad(CheckMode)
      case Some(AddressYears.OverAYear) =>
        routes.CheckYourAnswersController.onPageLoad()
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def hasBeenTradingIdRoutes(answers: UserAnswers): Call = {
    (answers.get(CompanyTradingOverAYearId), answers.get(AreYouInUKId)) match {
      case (Some(true), Some(true)) =>
        routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(NormalMode)
      case (Some(true), Some(false)) =>
        routes.CompanyPreviousAddressController.onPageLoad(NormalMode)
      case (Some(false), _) =>
        routes.CompanyEmailController.onPageLoad(NormalMode)
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def hasBeenTradingCheckIdRoutes(answers: UserAnswers): Call = {
    (answers.get(CompanyTradingOverAYearId), answers.get(AreYouInUKId)) match {
      case (Some(true), Some(true)) =>
        routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(CheckMode)
      case (Some(true), Some(false)) =>
        routes.CompanyPreviousAddressController.onPageLoad(CheckMode)
      case (Some(false), _) =>
        routes.CheckYourAnswersController.onPageLoad()
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def companyAddressYearsUpdateIdRoutes(answers: UserAnswers): Call =
    answers.get(CompanyAddressYearsId) match {
      case Some(AddressYears.UnderAYear) => routes.CompanyConfirmPreviousAddressController.onPageLoad()
      case Some(AddressYears.OverAYear) => anyMoreChanges
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }

  private def sameContactAddress(mode: Mode, answers: UserAnswers): Call = {
    (answers.get(CompanySameContactAddressId), answers.get(AreYouInUKId)) match {
      case (Some(true), _) => routes.CompanyAddressYearsController.onPageLoad(mode)
      case (Some(false), Some(false)) => routes.CompanyContactAddressController.onPageLoad(mode)
      case (Some(false), Some(true)) => routes.CompanyContactAddressPostCodeLookupController.onPageLoad(mode)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def regionBasedNavigation(answers: UserAnswers): Call = {
    answers.get(CompanyAddressId) map { address =>
      countryOptions.regions(address.countryOpt.getOrElse("")) match {
        case UK => controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(CheckMode)
        case EuEea => routes.WhatYouWillNeedController.onPageLoad()
        case RestOfTheWorld => routes.OutsideEuEeaController.onPageLoad()
        case _ => controllers.routes.SessionExpiredController.onPageLoad()
      }
    } getOrElse controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def confirmPreviousAddressRoutes(answers: UserAnswers): Call = {
    answers.get(CompanyConfirmPreviousAddressId) match {
      case Some(false) => routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode)
      case Some(true) => finishAmendmentNavigation(answers)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def regionBasedNameNavigation(answers: UserAnswers): Call = {
    answers.get(AreYouInUKId) match {
      case Some(false) => routes.CompanyRegisteredAddressController.onPageLoad()
      case Some(true) => routes.CompanyIsRegisteredNameController.onPageLoad()
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def crnNavigation(answers: UserAnswers): Call = {
    answers.get(BusinessTypeId)
    match {
      case Some(BusinessType.LimitedCompany) =>
        routes.CompanyRegistrationNumberController.onPageLoad(NormalMode)
      case _ =>
        routes.HasCompanyCRNController.onPageLoad(NormalMode)
    }
  }

  private def registeredNameRoutes(answers: UserAnswers): Call =
    answers.get(IsRegisteredNameId) match {
      case Some(true) => routes.ConfirmCompanyDetailsController.onPageLoad()
      case _ => routes.CompanyUpdateDetailsController.onPageLoad()
    }

  private def directorRoutes(answers: UserAnswers, mode: Mode): Call =
    if (answers.allDirectorsAfterDelete(mode).isEmpty) {
      controllers.register.company.directors.routes.WhatYouWillNeedController.onPageLoad()
    } else {
      routes.AddCompanyDirectorsController.onPageLoad(mode)
    }

}
