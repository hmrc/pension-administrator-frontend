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

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.register.company.routes
import identifiers.register.company.{PhoneId, _}
import identifiers.register.{AreYouInUKId, BusinessTypeId, _}
import models.InternationalRegion._
import models._
import models.register.BusinessType
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}

@Singleton
class RegisterCompanyNavigator @Inject()(
                                          val dataCacheConnector: UserAnswersCacheConnector,
                                          countryOptions: CountryOptions,
                                          appConfig: FrontendAppConfig) extends Navigator {

  //scalastyle:off cyclomatic.complexity
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case BusinessUTRId =>
      NavigateTo.dontSave(routes.CompanyNameController.onPageLoad())
    case BusinessNameId =>
      regionBasedNameNavigation(from.userAnswers)
    case IsRegisteredNameId =>
      registeredNameRoutes(from.userAnswers)
    case ConfirmCompanyAddressId =>
      crnNavigation(from.userAnswers)
    case HasCompanyCRNId if hasCrn(from.userAnswers) =>
      NavigateTo.dontSave(routes.CompanyRegistrationNumberController.onPageLoad(NormalMode))
    case HasCompanyCRNId =>
      NavigateTo.dontSave(routes.HasCompanyPAYEController.onPageLoad(NormalMode))
    case CompanyRegistrationNumberId =>
      NavigateTo.save(routes.HasCompanyPAYEController.onPageLoad(NormalMode))
    case HasPAYEId if hasPaye(from.userAnswers) =>
      NavigateTo.save(routes.CompanyEnterPAYEController.onPageLoad(NormalMode))
    case HasPAYEId =>
      NavigateTo.save(routes.HasCompanyVATController.onPageLoad(NormalMode))
    case EnterPAYEId =>
      NavigateTo.save(routes.HasCompanyVATController.onPageLoad(NormalMode))
    case HasVATId if hasVat(from.userAnswers) =>
      NavigateTo.save(routes.CompanyEnterVATController.onPageLoad(NormalMode))
    case HasVATId =>
      NavigateTo.save(routes.CompanySameContactAddressController.onPageLoad(NormalMode))
    case EnterVATId =>
      NavigateTo.save(routes.CompanySameContactAddressController.onPageLoad(NormalMode))
    case CompanySameContactAddressId =>
      sameContactAddress(NormalMode, from.userAnswers)
    case CompanyContactAddressPostCodeLookupId =>
      NavigateTo.save(routes.CompanyContactAddressListController.onPageLoad(NormalMode))
    case CompanyContactAddressListId =>
      NavigateTo.save(routes.CompanyContactAddressController.onPageLoad(NormalMode))
    case CompanyContactAddressId =>
      NavigateTo.save(routes.CompanyAddressYearsController.onPageLoad(NormalMode))
    case CompanyAddressYearsId =>
      companyAddressYearsIdRoutes(from.userAnswers)
    case CompanyPreviousAddressPostCodeLookupId =>
      NavigateTo.save(routes.CompanyAddressListController.onPageLoad(NormalMode))
    case CompanyAddressListId =>
      NavigateTo.save(routes.CompanyPreviousAddressController.onPageLoad(NormalMode))
    case CompanyPreviousAddressId =>
      NavigateTo.save(routes.EmailController.onPageLoad(NormalMode))
    case EmailId =>
      NavigateTo.save(routes.PhoneController.onPageLoad(NormalMode))
    case PhoneId =>
      NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
    case CheckYourAnswersId =>
      directorRoutes(from.userAnswers, NormalMode)
    case CompanyReviewId =>
      NavigateTo.save(controllers.register.routes.DeclarationController.onPageLoad())
    case CompanyAddressId =>
      regionBasedNavigation(from.userAnswers)
    case WhatYouWillNeedId =>
      NavigateTo.save(routes.CompanySameContactAddressController.onPageLoad(NormalMode))
    case _ => None
  }

  override protected def editRouteMap(from: NavigateFrom, mode: Mode): Option[NavigateTo] = from.id match {
    case HasCompanyCRNId if hasCrn(from.userAnswers) =>
      NavigateTo.dontSave(routes.CompanyRegistrationNumberController.onPageLoad(CheckMode))
    case HasCompanyCRNId =>
      checkYourAnswers
    case CompanyRegistrationNumberId =>
      checkYourAnswers
    case HasPAYEId if hasPaye(from.userAnswers) =>
      NavigateTo.save(routes.CompanyEnterPAYEController.onPageLoad(CheckMode))
    case HasPAYEId =>
      checkYourAnswers
    case EnterPAYEId =>
      checkYourAnswers
    case HasVATId if hasVat(from.userAnswers) =>
      NavigateTo.save(routes.CompanyEnterVATController.onPageLoad(CheckMode))
    case HasVATId =>
      checkYourAnswers
    case EnterVATId =>
      checkYourAnswers
    case CompanySameContactAddressId =>
      sameContactAddress(CheckMode, from.userAnswers)
    case CompanyContactAddressPostCodeLookupId =>
      NavigateTo.save(routes.CompanyContactAddressListController.onPageLoad(CheckMode))
    case CompanyContactAddressListId =>
      NavigateTo.save(routes.CompanyContactAddressController.onPageLoad(CheckMode))
    case CompanyContactAddressId =>
      NavigateTo.save(routes.CompanyAddressYearsController.onPageLoad(CheckMode))
    case CompanyAddressYearsId =>
      companyAddressYearsCheckIdRoutes(from.userAnswers)
    case CompanyPreviousAddressPostCodeLookupId =>
      NavigateTo.save(routes.CompanyAddressListController.onPageLoad(CheckMode))
    case CompanyAddressListId =>
      NavigateTo.save(routes.CompanyPreviousAddressController.onPageLoad(CheckMode))
    case CompanyPreviousAddressId =>
      checkYourAnswers
    case EmailId =>
      checkYourAnswers
    case PhoneId =>
      checkYourAnswers
    case _ => None
  }

  override protected def updateRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case CompanyContactAddressPostCodeLookupId =>
      NavigateTo.save(routes.CompanyContactAddressListController.onPageLoad(UpdateMode))
    case CompanyContactAddressListId =>
      NavigateTo.save(routes.CompanyContactAddressController.onPageLoad(UpdateMode))
    case CompanyContactAddressId =>
      NavigateTo.save(routes.CompanyAddressYearsController.onPageLoad(UpdateMode))
    case CompanyAddressYearsId =>
      companyAddressYearsUpdateIdRoutes(from.userAnswers)
    case CompanyConfirmPreviousAddressId =>
      confirmPreviousAddressRoutes(from.userAnswers)
    case CompanyPreviousAddressPostCodeLookupId =>
      NavigateTo.save(routes.CompanyAddressListController.onPageLoad(UpdateMode))
    case CompanyAddressListId =>
      NavigateTo.save(routes.CompanyPreviousAddressController.onPageLoad(UpdateMode))
    case CompanyPreviousAddressId =>
      anyMoreChanges
    case EmailId =>
      anyMoreChanges
    case PhoneId =>
      anyMoreChanges
  }

  //scalastyle:on cyclomatic.complexity

  private def checkYourAnswers: Option[NavigateTo] =
    NavigateTo.save(controllers.register.company.routes.CheckYourAnswersController.onPageLoad())

  private def hasPaye(answers: UserAnswers): Boolean = answers.get(HasPAYEId).getOrElse(false)

  private def hasCrn(answers: UserAnswers): Boolean = answers.get(HasCompanyCRNId).getOrElse(false)

  private def hasVat(answers: UserAnswers): Boolean = answers.get(HasVATId).getOrElse(false)

  private def anyMoreChanges: Option[NavigateTo] = NavigateTo.dontSave(controllers.register.routes.AnyMoreChangesController.onPageLoad())

  private def companyAddressYearsIdRoutes(answers: UserAnswers): Option[NavigateTo] = {
    (answers.get(CompanyAddressYearsId), answers.get(AreYouInUKId)) match {
      case (Some(AddressYears.UnderAYear), Some(false)) => NavigateTo.save(routes.CompanyPreviousAddressController.onPageLoad(NormalMode))
      case (Some(AddressYears.UnderAYear), Some(true)) => NavigateTo.save(routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(NormalMode))
      case (Some(AddressYears.OverAYear), _) => NavigateTo.save(routes.EmailController.onPageLoad(NormalMode))
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def companyAddressYearsCheckIdRoutes(answers: UserAnswers): Option[NavigateTo] = {
    (answers.get(CompanyAddressYearsId), answers.get(AreYouInUKId)) match {
      case (Some(AddressYears.UnderAYear), Some(false)) => NavigateTo.save(routes.CompanyPreviousAddressController.onPageLoad(CheckMode))
      case (Some(AddressYears.UnderAYear), Some(true)) => NavigateTo.save(routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(CheckMode))
      case (Some(AddressYears.OverAYear), _) => NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def companyAddressYearsUpdateIdRoutes(answers: UserAnswers): Option[NavigateTo] =
    answers.get(CompanyAddressYearsId) match {
      case Some(AddressYears.UnderAYear) => NavigateTo.save(routes.CompanyConfirmPreviousAddressController.onPageLoad())
      case Some(AddressYears.OverAYear) => anyMoreChanges
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }

  private def sameContactAddress(mode: Mode, answers: UserAnswers): Option[NavigateTo] = {
    (answers.get(CompanySameContactAddressId), answers.get(AreYouInUKId)) match {
      case (Some(true), _) => NavigateTo.save(routes.CompanyAddressYearsController.onPageLoad(mode))
      case (Some(false), Some(false)) => NavigateTo.save(routes.CompanyContactAddressController.onPageLoad(mode))
      case (Some(false), Some(true)) => NavigateTo.save(routes.CompanyContactAddressPostCodeLookupController.onPageLoad(mode))
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def regionBasedNavigation(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(CompanyAddressId) flatMap { address =>
      countryOptions.regions(address.country.getOrElse("")) match {
        case UK => NavigateTo.dontSave(controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(CheckMode))
        case EuEea => NavigateTo.dontSave(routes.WhatYouWillNeedController.onPageLoad())
        case RestOfTheWorld => NavigateTo.dontSave(routes.OutsideEuEeaController.onPageLoad())
        case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
      }
    }
  }

  private def confirmPreviousAddressRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(CompanyConfirmPreviousAddressId) match {
      case Some(false) => NavigateTo.dontSave(routes.CompanyPreviousAddressController.onPageLoad(UpdateMode))
      case Some(true) => anyMoreChanges
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def regionBasedNameNavigation(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(AreYouInUKId) match {
      case Some(false) => NavigateTo.dontSave(routes.CompanyRegisteredAddressController.onPageLoad())
      case Some(true) => NavigateTo.dontSave(routes.CompanyIsRegisteredNameController.onPageLoad())
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def crnNavigation(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(BusinessTypeId)
    match {
      case Some(BusinessType.LimitedCompany) =>
        NavigateTo.dontSave(routes.CompanyRegistrationNumberController.onPageLoad(NormalMode))
      case _ =>
        NavigateTo.dontSave(routes.HasCompanyCRNController.onPageLoad(NormalMode))
    }
  }

  private def hasCrnNoNav(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(BusinessTypeId)
    match {
      case Some(BusinessType.LimitedCompany) =>
        NavigateTo.dontSave(routes.CompanyRegistrationNumberController.onPageLoad(NormalMode))
      case _ =>
        NavigateTo.dontSave(routes.HasCompanyPAYEController.onPageLoad(NormalMode))
    }
  }

  private def registeredNameRoutes(answers: UserAnswers): Option[NavigateTo] =
    answers.get(IsRegisteredNameId) match {
      case Some(true) => NavigateTo.dontSave(routes.ConfirmCompanyDetailsController.onPageLoad())
      case _ => NavigateTo.dontSave(routes.CompanyUpdateDetailsController.onPageLoad())
    }

  private def directorRoutes(answers: UserAnswers, mode: Mode): Option[NavigateTo] =
    if (answers.allDirectorsAfterDelete(mode).isEmpty)
      NavigateTo.dontSave(controllers.register.company.directors.routes.WhatYouWillNeedController.onPageLoad())
      else
      NavigateTo.save(routes.AddCompanyDirectorsController.onPageLoad(mode))

}