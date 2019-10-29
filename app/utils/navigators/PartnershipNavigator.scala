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

import com.google.inject.Inject
import com.google.inject.Singleton
import config.FrontendAppConfig
import connectors.UserAnswersCacheConnector
import controllers.register.partnership.routes
import identifiers.register.partnership._
import identifiers.register.AreYouInUKId
import models._
import models.InternationalRegion.EuEea
import models.InternationalRegion.RestOfTheWorld
import models.InternationalRegion.UK
import utils.Navigator
import utils.UserAnswers
import utils.countryOptions.CountryOptions
import controllers.register.routes._
import controllers.register.partnership.routes._
import controllers.routes._

@Singleton
class PartnershipNavigator @Inject()(
                                      val dataCacheConnector: UserAnswersCacheConnector,
                                      countryOptions: CountryOptions,
                                      appConfig: FrontendAppConfig) extends Navigator {

  //scalastyle:off cyclomatic.complexity
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case PartnershipDetailsId =>
      regionBasedNameNavigation(from.userAnswers)
    case ConfirmPartnershipDetailsId =>
      NavigateTo.dontSave(routes.WhatYouWillNeedController.onPageLoad())
    case WhatYouWillNeedId =>
      NavigateTo.save(PartnershipSameContactAddressController.onPageLoad(NormalMode))
    case PartnershipSameContactAddressId =>
      sameContactAddress(NormalMode, from.userAnswers)
    case PartnershipContactAddressPostCodeLookupId =>
      NavigateTo.save(PartnershipContactAddressListController.onPageLoad(NormalMode))
    case PartnershipContactAddressListId =>
      NavigateTo.save(PartnershipContactAddressController.onPageLoad(NormalMode))
    case PartnershipContactAddressId =>
      NavigateTo.save(PartnershipAddressYearsController.onPageLoad(NormalMode))
    case PartnershipAddressYearsId =>
      addressYearsIdRoutes(from.userAnswers, NormalMode)
    case PartnershipTradingOverAYearId =>
      tradingOverAYearRoutes(from.userAnswers, NormalMode)
    case PartnershipPreviousAddressPostCodeLookupId =>
      NavigateTo.save(PartnershipPreviousAddressListController.onPageLoad(NormalMode))
    case PartnershipPreviousAddressListId =>
      NavigateTo.save(PartnershipPreviousAddressController.onPageLoad(NormalMode))
    case PartnershipPreviousAddressId =>
      NavigateTo.save(PartnershipContactDetailsController.onPageLoad(NormalMode))
    case PartnershipContactDetailsId =>
      regionBasedContactDetailsRoutes(from.userAnswers)
    case PartnershipVatId =>
      NavigateTo.save(PartnershipPayeController.onPageLoad(NormalMode))
    case PartnershipPayeId =>
      NavigateTo.save(CheckYourAnswersController.onPageLoad())
    case CheckYourAnswersId =>
      NavigateTo.save(AddPartnerController.onPageLoad(NormalMode))
    case PartnershipReviewId =>
      NavigateTo.save(DeclarationController.onPageLoad())
    case PartnershipRegisteredAddressId =>
      regionBasedNavigation(from.userAnswers)
    case _ =>
      NavigateTo.dontSave(SessionExpiredController.onPageLoad())
  }

  override protected def editRouteMap(from: NavigateFrom, mode: Mode): Option[NavigateTo] = {
    from.id match {
      case PartnershipSameContactAddressId =>
        sameContactAddress(CheckMode, from.userAnswers)
      case PartnershipContactAddressPostCodeLookupId =>
        NavigateTo.save(PartnershipContactAddressListController.onPageLoad(CheckMode))
      case PartnershipContactAddressListId =>
        NavigateTo.save(PartnershipContactAddressController.onPageLoad(CheckMode))
      case PartnershipContactAddressId =>
        NavigateTo.save(PartnershipAddressYearsController.onPageLoad(CheckMode))
      case PartnershipAddressYearsId =>
        addressYearsCheckIdRoutes(from.userAnswers, CheckMode)
      case PartnershipTradingOverAYearId =>
        tradingOverAYearRoutes(from.userAnswers, CheckMode)
      case PartnershipPreviousAddressPostCodeLookupId =>
        NavigateTo.save(PartnershipPreviousAddressListController.onPageLoad(CheckMode))
      case PartnershipPreviousAddressListId =>
        NavigateTo.save(PartnershipPreviousAddressController.onPageLoad(CheckMode))
      case PartnershipPreviousAddressId =>
        NavigateTo.save(CheckYourAnswersController.onPageLoad())
      case PartnershipContactDetailsId =>
        NavigateTo.save(CheckYourAnswersController.onPageLoad())
      case PartnershipVatId =>
        NavigateTo.save(CheckYourAnswersController.onPageLoad())
      case PartnershipPayeId =>
        NavigateTo.save(CheckYourAnswersController.onPageLoad())
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  override protected def updateRouteMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case PartnershipContactAddressPostCodeLookupId =>
        NavigateTo.save(PartnershipContactAddressListController.onPageLoad(UpdateMode))
      case PartnershipContactAddressListId =>
        NavigateTo.save(PartnershipContactAddressController.onPageLoad(UpdateMode))
      case PartnershipContactAddressId =>
        NavigateTo.dontSave(PartnershipAddressYearsController.onPageLoad(UpdateMode))
      case PartnershipAddressYearsId =>
        addressYearsCheckIdRoutes(from.userAnswers, UpdateMode)
      case PartnershipTradingOverAYearId =>
        tradingOverAYearRoutes(from.userAnswers, UpdateMode)
      case PartnershipContactDetailsId =>
        NavigateTo.dontSave(AnyMoreChangesController.onPageLoad())
      case PartnershipPreviousAddressPostCodeLookupId =>
        NavigateTo.dontSave(PartnershipPreviousAddressListController.onPageLoad(UpdateMode))
      case PartnershipPreviousAddressListId =>
        NavigateTo.dontSave(PartnershipPreviousAddressController.onPageLoad(UpdateMode))
      case PartnershipPreviousAddressId =>
        NavigateTo.dontSave(AnyMoreChangesController.onPageLoad())
      case PartnershipConfirmPreviousAddressId =>
        variationManualPreviousAddressRoutes(from.userAnswers, UpdateMode)
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def addressYearsIdRoutes(answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    answers.get(PartnershipAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(PartnershipTradingOverAYearController.onPageLoad(NormalMode))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(PartnershipContactDetailsController.onPageLoad(NormalMode))
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def addressYearsCheckIdRoutes(answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    answers.get(PartnershipAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(routes.PartnershipTradingOverAYearController.onPageLoad(mode))
      case Some(AddressYears.OverAYear) =>
        mode match {
          case CheckMode =>
            NavigateTo.save(CheckYourAnswersController.onPageLoad())
          case UpdateMode =>
            NavigateTo.save(AnyMoreChangesController.onPageLoad())
          case _ =>
            NavigateTo.dontSave(SessionExpiredController.onPageLoad())
        }
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def tradingOverAYearRoutes(answers: UserAnswers, mode:Mode): Option[NavigateTo] = {
    (answers.get(PartnershipTradingOverAYearId), answers.get(AreYouInUKId)) match {
      case (Some(true), Some(false)) =>
        mode match {
          case NormalMode | CheckMode =>
            NavigateTo.dontSave(PartnershipPreviousAddressController.onPageLoad(mode))
          case _ =>
            NavigateTo.dontSave(PartnershipConfirmPreviousAddressController.onPageLoad())
        }
      case (Some(true), Some(true)) =>
        mode match {
          case NormalMode | CheckMode =>
            NavigateTo.dontSave(PartnershipPreviousAddressPostCodeLookupController.onPageLoad(mode))
          case _ =>
            NavigateTo.dontSave(PartnershipConfirmPreviousAddressController.onPageLoad())
        }
      case (Some(false), _) =>
        mode match {
          case NormalMode =>
            NavigateTo.dontSave(PartnershipContactDetailsController.onPageLoad(NormalMode))
          case CheckMode =>
            NavigateTo.dontSave(CheckYourAnswersController.onPageLoad())
          case _ =>
            NavigateTo.dontSave(AnyMoreChangesController.onPageLoad())
        }
      case _ =>
        NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def sameContactAddress(mode: Mode, answers: UserAnswers): Option[NavigateTo] = {
    (answers.get(PartnershipSameContactAddressId), answers.get(AreYouInUKId)) match {
      case (Some(true), _) => NavigateTo.save(PartnershipAddressYearsController.onPageLoad(mode))
      case (Some(false), Some(false)) => NavigateTo.save(PartnershipContactAddressController.onPageLoad(mode))
      case (Some(false), Some(true)) => NavigateTo.save(PartnershipContactAddressPostCodeLookupController.onPageLoad(mode))
      case _ => NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }


  private def regionBasedNameNavigation(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(AreYouInUKId) match {
      case Some(false) => NavigateTo.dontSave(PartnershipRegisteredAddressController.onPageLoad())
      case Some(true) => NavigateTo.dontSave(ConfirmPartnershipDetailsController.onPageLoad())
      case _ => NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def regionBasedNavigation(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnershipRegisteredAddressId) flatMap { address =>
      countryOptions.regions(address.country.getOrElse("")) match {
        case UK => NavigateTo.dontSave(BusinessTypeAreYouInUKController.onPageLoad(CheckMode))
        case EuEea => NavigateTo.dontSave(routes.WhatYouWillNeedController.onPageLoad())
        case RestOfTheWorld => NavigateTo.dontSave(OutsideEuEeaController.onPageLoad())
        case _ => NavigateTo.dontSave(SessionExpiredController.onPageLoad())
      }
    }
  }

  private def regionBasedContactDetailsRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(AreYouInUKId) match {
      case Some(false) => NavigateTo.save(CheckYourAnswersController.onPageLoad())
      case Some(true) => NavigateTo.save(PartnershipVatController.onPageLoad(NormalMode))
      case _ => NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }

  private def variationManualPreviousAddressRoutes(answers: UserAnswers, mode:Mode): Option[NavigateTo] = {
    answers.get(PartnershipConfirmPreviousAddressId) match {
      case Some(false) =>NavigateTo.dontSave(PartnershipPreviousAddressController.onPageLoad(mode))
      case Some(true) => NavigateTo.dontSave(AnyMoreChangesController.onPageLoad())
      case _ => NavigateTo.dontSave(SessionExpiredController.onPageLoad())
    }
  }
}
