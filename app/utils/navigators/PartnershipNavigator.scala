/*
 * Copyright 2021 HM Revenue & Customs
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
import connectors.cache.UserAnswersCacheConnector
import controllers.register.partnership.routes.{AddPartnerController, _}
import controllers.register.routes._
import controllers.routes._
import identifiers.{Identifier, UpdateContactAddressId}
import identifiers.register.partnership._
import identifiers.register.{AreYouInUKId, BusinessNameId, BusinessUTRId, EnterVATId, IsRegisteredNameId, _}
import models.InternationalRegion.{EuEea, RestOfTheWorld, UK}
import models._
import play.api.mvc.Call
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}

@Singleton
class PartnershipNavigator @Inject()(
                                      val dataCacheConnector: UserAnswersCacheConnector,
                                      countryOptions: CountryOptions,
                                      appConfig: FrontendAppConfig) extends Navigator {

  //scalastyle:off cyclomatic.complexity
  //scalastyle:off method.length
  override protected def routeMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    case BusinessUTRId =>
      PartnershipNameController.onPageLoad()
    case BusinessNameId =>
      regionBasedNameNavigation(ua)
    case IsRegisteredNameId =>
      registeredNameRoutes(ua)
    case ConfirmPartnershipDetailsId =>
      HasPartnershipPAYEController.onPageLoad(NormalMode)
    case HasPAYEId if hasPaye(ua) =>
      PartnershipEnterPAYEController.onPageLoad(NormalMode)
    case HasPAYEId =>
      HasPartnershipVATController.onPageLoad(NormalMode)
    case EnterPAYEId =>
      HasPartnershipVATController.onPageLoad(NormalMode)
    case HasVATId =>
      vatNavigation(ua, NormalMode)
    case EnterVATId =>
      PartnershipSameContactAddressController.onPageLoad(NormalMode)
    case PartnershipSameContactAddressId =>
      sameContactAddress(NormalMode, ua)
    case PartnershipContactAddressPostCodeLookupId =>
      PartnershipContactAddressListController.onPageLoad(NormalMode)
    case PartnershipContactAddressId =>
      PartnershipAddressYearsController.onPageLoad(NormalMode)
    case PartnershipAddressYearsId =>
      addressYearsRoutes(ua, NormalMode)
    case PartnershipTradingOverAYearId =>
      tradingOverAYearRoutes(ua, NormalMode)
    case PartnershipPreviousAddressPostCodeLookupId =>
      PartnershipPreviousAddressListController.onPageLoad(NormalMode)
    case PartnershipPreviousAddressId =>
      PartnershipEmailController.onPageLoad(NormalMode)
    case PartnershipEmailId =>
      PartnershipPhoneController.onPageLoad(NormalMode)
    case PartnershipPhoneId =>
      CheckYourAnswersController.onPageLoad()
    case CheckYourAnswersId =>
      partnerRoutes(ua)
    case PartnershipReviewId =>
      controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(NormalMode)
    case PartnershipRegisteredAddressId =>
      regionBasedNavigation(ua)
  }

  override protected def editRouteMap(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case HasPAYEId if hasPaye(ua) =>
      PartnershipEnterPAYEController.onPageLoad(CheckMode)
    case HasPAYEId =>
      CheckYourAnswersController.onPageLoad()
    case EnterPAYEId =>
      CheckYourAnswersController.onPageLoad()
    case HasVATId =>
      vatNavigation(ua, mode)
    case EnterVATId =>
      CheckYourAnswersController.onPageLoad()
    case PartnershipSameContactAddressId =>
      sameContactAddress(CheckMode, ua)
    case PartnershipContactAddressPostCodeLookupId =>
      PartnershipContactAddressListController.onPageLoad(CheckMode)
    case PartnershipContactAddressId =>
      CheckYourAnswersController.onPageLoad()
    case PartnershipAddressYearsId =>
      addressYearsRoutes(ua, CheckMode)
    case PartnershipTradingOverAYearId =>
      tradingOverAYearRoutes(ua, CheckMode)
    case PartnershipPreviousAddressPostCodeLookupId =>
      PartnershipPreviousAddressListController.onPageLoad(CheckMode)
    case PartnershipPreviousAddressId =>
      CheckYourAnswersController.onPageLoad()
    case PartnershipEmailId =>
      CheckYourAnswersController.onPageLoad()
    case PartnershipPhoneId =>
      CheckYourAnswersController.onPageLoad()
  }

  override protected def updateRouteMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    case PartnershipContactAddressPostCodeLookupId =>
      PartnershipContactAddressListController.onPageLoad(UpdateMode)
    case PartnershipContactAddressId =>
      PartnershipConfirmPreviousAddressController.onPageLoad()
    case PartnershipAddressYearsId =>
      addressYearsRoutes(ua, UpdateMode)
    case PartnershipTradingOverAYearId =>
      tradingOverAYearRoutes(ua, UpdateMode)
    case PartnershipPreviousAddressPostCodeLookupId =>
      PartnershipPreviousAddressListController.onPageLoad(UpdateMode)
    case PartnershipPreviousAddressId => finishAmendmentNavigation(ua)
    case PartnershipConfirmPreviousAddressId =>
      variationManualPreviousAddressRoutes(ua, UpdateMode)
    case PartnershipPhoneId => finishAmendmentNavigation(ua)
    case PartnershipEmailId => finishAmendmentNavigation(ua)
    }

  private def addressYearsRoutes(answers: UserAnswers, mode: Mode): Call = {
    answers.get(PartnershipAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        PartnershipTradingOverAYearController.onPageLoad(mode)
      case Some(AddressYears.OverAYear) =>
        mode match {
          case NormalMode =>
            PartnershipEmailController.onPageLoad(mode)
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

  private def finishAmendmentNavigation(answers: UserAnswers): Call =
    answers.get(UpdateContactAddressId) match {
      case Some(_) => updateContactAddressCYAPage()
      case _ => AnyMoreChangesController.onPageLoad()
    }

  private def updateContactAddressCYAPage():Call = controllers.routes.UpdateContactAddressCYAController.onPageLoad()
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
      case _ => SessionExpiredController.onPageLoad()
    }
  }

  private def regionBasedNavigation(answers: UserAnswers): Call = {
    answers.get(PartnershipRegisteredAddressId) map { address =>
      countryOptions.regions(address.country.getOrElse("")) match {
        case UK => BusinessTypeAreYouInUKController.onPageLoad(CheckMode)
        case EuEea => controllers.register.partnership.routes.PartnershipSameContactAddressController.onPageLoad(NormalMode)
        case RestOfTheWorld => OutsideEuEeaController.onPageLoad()
        case _ => SessionExpiredController.onPageLoad()
      }
    } getOrElse SessionExpiredController.onPageLoad()
  }

  private def variationManualPreviousAddressRoutes(answers: UserAnswers, mode: Mode): Call = {
    answers.get(PartnershipConfirmPreviousAddressId) match {
      case Some(false) => PartnershipPreviousAddressPostCodeLookupController.onPageLoad(mode)
      case Some(true) => finishAmendmentNavigation(answers)
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
    case Some(false) if mode == NormalMode => PartnershipSameContactAddressController.onPageLoad(NormalMode)
    case Some(false) if mode == CheckMode => CheckYourAnswersController.onPageLoad()
    case _ => SessionExpiredController.onPageLoad()
  }

  private def partnerRoutes(answers: UserAnswers): Call =
    if (answers.allPartnersAfterDelete(NormalMode).isEmpty) {
      controllers.register.partnership.partners.routes.WhatYouWillNeedController.onPageLoad()
    }
    else {
      AddPartnerController.onPageLoad(NormalMode)
    }
}
