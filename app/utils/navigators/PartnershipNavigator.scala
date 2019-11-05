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
import controllers.register.partnership.routes
import controllers.register.partnership.routes.{AddPartnerController, _}
import controllers.register.routes._
import controllers.routes._
import identifiers.Identifier
import identifiers.register._
import identifiers.register.partnership._
import models.InternationalRegion.{EuEea, RestOfTheWorld, UK}
import models._
import play.api.mvc.Call
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}

@Singleton
class PartnershipNavigator @Inject()(
                                      countryOptions: CountryOptions,
                                      appConfig: FrontendAppConfig) extends Navigator {

  //scalastyle:off cyclomatic.complexity
  override protected def routeMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    case BusinessUTRId =>
      PartnershipNameController.onPageLoad()
    case BusinessNameId =>
      regionBasedNameNavigation(ua)
    case IsRegisteredNameId =>
      registeredNameRoutes(ua)
    case ConfirmPartnershipDetailsId =>
      controllers.register.partnership.routes.WhatYouWillNeedController.onPageLoad()
    case WhatYouWillNeedId =>
      PartnershipSameContactAddressController.onPageLoad(NormalMode)
    case PartnershipSameContactAddressId =>
      sameContactAddress(NormalMode, ua)
    case PartnershipContactAddressPostCodeLookupId =>
      PartnershipContactAddressListController.onPageLoad(NormalMode)
    case PartnershipContactAddressListId =>
      PartnershipContactAddressController.onPageLoad(NormalMode)
    case PartnershipContactAddressId =>
      PartnershipAddressYearsController.onPageLoad(NormalMode)
    case PartnershipAddressYearsId =>
      addressYearsIdRoutes(ua, NormalMode)
    case PartnershipTradingOverAYearId =>
      tradingOverAYearRoutes(ua, NormalMode)
    case PartnershipPreviousAddressPostCodeLookupId =>
      PartnershipPreviousAddressListController.onPageLoad(NormalMode)
    case PartnershipPreviousAddressListId =>
      PartnershipPreviousAddressController.onPageLoad(NormalMode)
    case PartnershipPreviousAddressId =>
      PartnershipEmailController.onPageLoad(NormalMode)
    case PartnershipEmailId =>
      PartnershipPhoneController.onPageLoad(NormalMode)
    case PartnershipPhoneId =>
      regionBasedContactDetailsRoutes(ua)
    case HasVATId =>
      vatNavigation(ua, NormalMode)
    case EnterVATId =>
      HasPartnershipPAYEController.onPageLoad(NormalMode)
    case HasPAYEId if hasPaye(ua) =>
      routes.PartnershipEnterPAYEController.onPageLoad(NormalMode)
    case HasPAYEId =>
      routes.CheckYourAnswersController.onPageLoad()
    case EnterPAYEId =>
      routes.CheckYourAnswersController.onPageLoad()
    case CheckYourAnswersId =>
      partnerRoutes(ua)
    case PartnershipReviewId =>
      DeclarationController.onPageLoad()
    case PartnershipRegisteredAddressId =>
      regionBasedNavigation(ua)
    case _ =>
      SessionExpiredController.onPageLoad()
  }

  override protected def editRouteMap(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
      case PartnershipSameContactAddressId =>
        sameContactAddress(CheckMode, ua)
      case PartnershipContactAddressPostCodeLookupId =>
        PartnershipContactAddressListController.onPageLoad(CheckMode)
      case PartnershipContactAddressListId =>
        PartnershipContactAddressController.onPageLoad(CheckMode)
      case PartnershipContactAddressId =>
        PartnershipAddressYearsController.onPageLoad(CheckMode)
      case PartnershipAddressYearsId =>
        addressYearsCheckIdRoutes(ua, CheckMode)
      case PartnershipTradingOverAYearId =>
        tradingOverAYearRoutes(ua, CheckMode)
      case PartnershipPreviousAddressPostCodeLookupId =>
        PartnershipPreviousAddressListController.onPageLoad(CheckMode)
      case PartnershipPreviousAddressListId =>
        PartnershipPreviousAddressController.onPageLoad(CheckMode)
      case PartnershipPreviousAddressId =>
        CheckYourAnswersController.onPageLoad()
      case PartnershipEmailId =>
        CheckYourAnswersController.onPageLoad()
      case PartnershipPhoneId =>
        CheckYourAnswersController.onPageLoad()
      case HasVATId =>
        vatNavigation(ua, mode)
      case EnterVATId =>
        CheckYourAnswersController.onPageLoad()
      case HasPAYEId if hasPaye(ua) =>
        routes.PartnershipEnterPAYEController.onPageLoad(CheckMode)
      case HasPAYEId =>
        routes.CheckYourAnswersController.onPageLoad()
      case EnterPAYEId =>
        routes.CheckYourAnswersController.onPageLoad()
      case _ =>
        SessionExpiredController.onPageLoad()
  }

  override protected def updateRouteMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    case PartnershipContactAddressPostCodeLookupId =>
      PartnershipContactAddressListController.onPageLoad(UpdateMode)
    case PartnershipContactAddressListId =>
      PartnershipContactAddressController.onPageLoad(UpdateMode)
    case PartnershipContactAddressId =>
      PartnershipAddressYearsController.onPageLoad(UpdateMode)
    case PartnershipAddressYearsId =>
      addressYearsCheckIdRoutes(ua, UpdateMode)
    case PartnershipTradingOverAYearId =>
      tradingOverAYearRoutes(ua, UpdateMode)
    case PartnershipPhoneId =>
      controllers.register.routes.AnyMoreChangesController.onPageLoad()
    case PartnershipEmailId =>
      controllers.register.routes.AnyMoreChangesController.onPageLoad()
    case PartnershipPreviousAddressPostCodeLookupId =>
      PartnershipPreviousAddressListController.onPageLoad(UpdateMode)
    case PartnershipPreviousAddressListId =>
      PartnershipPreviousAddressController.onPageLoad(UpdateMode)
    case PartnershipPreviousAddressId =>
      AnyMoreChangesController.onPageLoad()
    case PartnershipConfirmPreviousAddressId =>
      variationManualPreviousAddressRoutes(ua, UpdateMode)
    case _ =>
      SessionExpiredController.onPageLoad()
  }

  private def addressYearsIdRoutes(answers: UserAnswers, mode: Mode): Call = {
    answers.get(PartnershipAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        PartnershipTradingOverAYearController.onPageLoad(NormalMode)
      case Some(AddressYears.OverAYear) =>
        PartnershipEmailController.onPageLoad(NormalMode)
      case _ =>
        SessionExpiredController.onPageLoad()
    }
  }

  private def addressYearsCheckIdRoutes(answers: UserAnswers, mode: Mode): Call = {
    answers.get(PartnershipAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        PartnershipTradingOverAYearController.onPageLoad(mode)
      case Some(AddressYears.OverAYear) =>
        mode match {
          case CheckMode =>
            CheckYourAnswersController.onPageLoad()
          case UpdateMode =>
            AnyMoreChangesController.onPageLoad()
          case _ =>
            SessionExpiredController.onPageLoad()
        }
      case _ =>
        SessionExpiredController.onPageLoad()
    }
  }

  private def hasPaye(ua: UserAnswers): Boolean = ua.get(HasPAYEId).getOrElse(false)

  private def tradingOverAYearRoutes(answers: UserAnswers, mode: Mode): Call = {
    (answers.get(PartnershipTradingOverAYearId), answers.get(AreYouInUKId)) match {
      case (Some(true), Some(false)) =>
        mode match {
          case NormalMode | CheckMode =>
            PartnershipPreviousAddressController.onPageLoad(mode)
          case _ =>
            PartnershipConfirmPreviousAddressController.onPageLoad()
        }
      case (Some(true), Some(true)) =>
        mode match {
          case NormalMode | CheckMode =>
            PartnershipPreviousAddressPostCodeLookupController.onPageLoad(mode)
          case _ =>
            PartnershipConfirmPreviousAddressController.onPageLoad()
        }
      case (Some(false), _) =>
        mode match {
          case NormalMode =>
            PartnershipEmailController.onPageLoad(NormalMode)
          case CheckMode =>
            CheckYourAnswersController.onPageLoad()
          case _ =>
            AnyMoreChangesController.onPageLoad()
        }
      case _ =>
        SessionExpiredController.onPageLoad()
    }
  }

  private def sameContactAddress(mode: Mode, answers: UserAnswers): Call = {
    (answers.get(PartnershipSameContactAddressId), answers.get(AreYouInUKId)) match {
      case (Some(true), _) => PartnershipAddressYearsController.onPageLoad(mode)
      case (Some(false), Some(false)) => PartnershipContactAddressController.onPageLoad(mode)
      case (Some(false), Some(true)) => PartnershipContactAddressPostCodeLookupController.onPageLoad(mode)
      case _ => SessionExpiredController.onPageLoad()
    }
  }


  private def regionBasedNameNavigation(answers: UserAnswers): Call = {
    answers.get(AreYouInUKId) match {
      case Some(false) => PartnershipRegisteredAddressController.onPageLoad()
      case Some(true) => PartnershipIsRegisteredNameController.onPageLoad()
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def regionBasedNavigation(answers: UserAnswers): Call = {
    answers.get(PartnershipRegisteredAddressId) map { address =>
      countryOptions.regions(address.country.getOrElse("")) match {
        case UK => BusinessTypeAreYouInUKController.onPageLoad(CheckMode)
        case EuEea => controllers.register.partnership.routes.WhatYouWillNeedController.onPageLoad()
        case RestOfTheWorld => OutsideEuEeaController.onPageLoad()
        case _ => SessionExpiredController.onPageLoad()
      }
    } getOrElse SessionExpiredController.onPageLoad()
  }

  private def regionBasedContactDetailsRoutes(answers: UserAnswers): Call = {
    answers.get(AreYouInUKId) match {
      case Some(false) => CheckYourAnswersController.onPageLoad()
      case Some(true) => HasPartnershipVATController.onPageLoad(NormalMode)
      case _ => SessionExpiredController.onPageLoad()
    }
  }

  private def variationManualPreviousAddressRoutes(answers: UserAnswers, mode: Mode): Call = {
    answers.get(PartnershipConfirmPreviousAddressId) match {
      case Some(false) => PartnershipPreviousAddressController.onPageLoad(mode)
      case Some(true) => AnyMoreChangesController.onPageLoad()
      case _ => SessionExpiredController.onPageLoad()
    }
  }

  private def registeredNameRoutes(answers: UserAnswers): Call =
    answers.get(IsRegisteredNameId) match {
      case Some(true) => ConfirmPartnershipDetailsController.onPageLoad()
      case _ => controllers.register.company.routes.CompanyUpdateDetailsController.onPageLoad()
    }

  def vatNavigation(userAnswers: UserAnswers, mode: Mode): Call = userAnswers.get(HasVATId) match {
    case Some(true) => PartnershipEnterVATController.onPageLoad(mode)
    case Some(false) if mode == NormalMode => HasPartnershipPAYEController.onPageLoad(mode)
    case Some(false) if mode == CheckMode => CheckYourAnswersController.onPageLoad()
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def partnerRoutes(answers: UserAnswers): Call =
    if (answers.allPartnersAfterDelete(NormalMode).isEmpty) {
      controllers.register.partnership.partners.routes.WhatYouWillNeedController.onPageLoad()
    }
    else {
      AddPartnerController.onPageLoad(NormalMode)
    }
}
