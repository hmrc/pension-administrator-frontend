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
import identifiers.register.AreYouInUKId
import identifiers.register.company._
import models.InternationalRegion._
import models._
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}

@Singleton
class RegisterCompanyNavigator @Inject()(
                                          val dataCacheConnector: UserAnswersCacheConnector,
                                          countryOptions: CountryOptions,
                                          appConfig: FrontendAppConfig) extends Navigator {

  //scalastyle:off cyclomatic.complexity
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case BusinessDetailsId =>
      regionBasedNameNavigation(from.userAnswers)
    case ConfirmCompanyAddressId =>
      NavigateTo.dontSave(routes.WhatYouWillNeedController.onPageLoad())
    case WhatYouWillNeedId =>
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
      NavigateTo.save(routes.ContactDetailsController.onPageLoad(NormalMode))
    case ContactDetailsId =>
      regionBasedContactDetailsRoutes(from.userAnswers)
    case CompanyDetailsId =>
      NavigateTo.save(routes.CompanyRegistrationNumberController.onPageLoad(NormalMode))
    case CompanyRegistrationNumberId =>
      NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
    case CheckYourAnswersId =>
      NavigateTo.save(routes.AddCompanyDirectorsController.onPageLoad(NormalMode))
    case CompanyReviewId =>
      NavigateTo.save(controllers.register.routes.DeclarationController.onPageLoad())
    case CompanyAddressId =>
      regionBasedNavigation(from.userAnswers)
    case _ => None
  }

  override protected def editRouteMap(from: NavigateFrom, mode: Mode): Option[NavigateTo] = from.id match {
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
    case ContactDetailsId =>
      checkYourAnswers
    case CompanyDetailsId =>
      checkYourAnswers
    case CompanyRegistrationNumberId =>
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
    case CompanyConfirmPreviousAddressId => confirmPreviousAddressRoutes(from.userAnswers)
    case CompanyPreviousAddressPostCodeLookupId =>
      NavigateTo.save(routes.CompanyAddressListController.onPageLoad(UpdateMode))
    case CompanyAddressListId =>
      NavigateTo.save(routes.CompanyPreviousAddressController.onPageLoad(UpdateMode))
    case CompanyPreviousAddressId =>
      anyMoreChanges
    case ContactDetailsId =>
      anyMoreChanges
  }

  //scalastyle:on cyclomatic.complexity

  private def checkYourAnswers: Option[NavigateTo] =
    NavigateTo.save(controllers.register.company.routes.CheckYourAnswersController.onPageLoad())

  private def anyMoreChanges: Option[NavigateTo] = NavigateTo.dontSave(controllers.register.routes.AnyMoreChangesController.onPageLoad())

  private def companyAddressYearsIdRoutes(answers: UserAnswers): Option[NavigateTo] = {
    (answers.get(CompanyAddressYearsId), answers.get(AreYouInUKId)) match {
      case (Some(AddressYears.UnderAYear), Some(false)) => NavigateTo.save(routes.CompanyPreviousAddressController.onPageLoad(NormalMode))
      case (Some(AddressYears.UnderAYear), Some(true)) => NavigateTo.save(routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(NormalMode))
      case (Some(AddressYears.OverAYear), _) => NavigateTo.save(routes.ContactDetailsController.onPageLoad(NormalMode))
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
      case Some(true) => NavigateTo.dontSave(routes.ConfirmCompanyDetailsController.onPageLoad())
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def regionBasedContactDetailsRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(AreYouInUKId) match {
      case Some(false) => NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
      case Some(true) => NavigateTo.save(routes.CompanyDetailsController.onPageLoad(NormalMode))
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
