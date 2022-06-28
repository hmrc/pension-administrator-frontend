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

import controllers.register.administratorPartnership.routes._

import controllers.register.administratorPartnership.partnershipDetails.routes._

import controllers.register.administratorPartnership.contactDetails.routes._
import identifiers.register._
import identifiers.register.partnership._
import identifiers.{Identifier, UpdateContactAddressId}
import models.InternationalRegion._
import models._
import play.api.mvc.Call
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}

@Singleton
class RegisterPartnershipNavigatorV2 @Inject()(countryOptions: CountryOptions) extends Navigator {

//  scalastyle:off cyclomatic.complexity
// scalastyle:off method.length
  override protected def routeMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    case BusinessUTRId =>
      PartnershipNameController.onPageLoad()
    case BusinessNameId =>
      PartnershipIsRegisteredNameController.onPageLoad()
    case IsRegisteredNameId =>
      registeredNameRoutes(ua)
    case HasPAYEId if hasPaye(ua) =>
      PartnershipEnterPAYEController.onPageLoad(NormalMode)
    case HasPAYEId =>
      HasPartnershipVATController.onPageLoad(NormalMode)
    case EnterPAYEId =>
      HasPartnershipVATController.onPageLoad(NormalMode)
    case HasVATId if hasVat(ua) =>
      PartnershipEnterVATController.onPageLoad(NormalMode)
    case HasVATId =>
      detailsCYA
    case EnterVATId =>
      detailsCYA
    case PartnershipSameContactAddressId =>
      sameContactAddress(NormalMode, ua)
    case PartnershipContactAddressPostCodeLookupId =>
      PartnershipContactAddressListController.onPageLoad(NormalMode)
    case PartnershipContactAddressId =>
      PartnershipAddressYearsController.onPageLoad(NormalMode)
    case PartnershipAddressYearsId =>
      partnershipAddressYearsIdRoutes(ua)
    case PartnershipTradingOverAYearId =>
      hasBeenTradingIdRoutes(ua)
    case PartnershipPreviousAddressPostCodeLookupId =>
      PartnershipPreviousAddressListController.onPageLoad(NormalMode)
    case PartnershipPreviousAddressId =>
      PartnershipEmailController.onPageLoad(NormalMode)
    case PartnershipEmailId =>
      PartnershipPhoneController.onPageLoad(NormalMode)
    case PartnershipPhoneId =>
      controllers.register.administratorPartnership.contactDetails.routes.CheckYourAnswersController.onPageLoad()
    case CheckYourAnswersId =>
      partnerRoutes(ua, NormalMode)
    case PartnershipReviewId =>
      controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(NormalMode)
    case PartnershipAddressId =>
      regionBasedNavigation(ua)
    case WhatYouWillNeedId =>
      PartnershipSameContactAddressController.onPageLoad(NormalMode)
    case WhatYouWillNeedIdV2 =>
      HasPartnershipPAYEController.onPageLoad(NormalMode)
  }

  override protected def editRouteMap(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case HasPAYEId if hasPaye(ua) =>
      PartnershipEnterPAYEController.onPageLoad(CheckMode)
    case HasPAYEId =>
      detailsCYA
    case EnterPAYEId =>
      detailsCYA
    case HasVATId if hasVat(ua) =>
      PartnershipEnterVATController.onPageLoad(CheckMode)
    case HasVATId =>
      detailsCYA
    case EnterVATId =>
      detailsCYA
    case PartnershipSameContactAddressId =>
      sameContactAddress(CheckMode, ua)
    case PartnershipContactAddressPostCodeLookupId =>
      PartnershipContactAddressListController.onPageLoad(CheckMode)
    case PartnershipContactAddressId =>
      contactDetailsCYA
    case PartnershipAddressYearsId =>
      partnershipAddressYearsCheckIdRoutes(ua)
    case PartnershipTradingOverAYearId =>
      hasBeenTradingCheckIdRoutes(ua)
    case PartnershipPreviousAddressPostCodeLookupId =>
      PartnershipPreviousAddressListController.onPageLoad(CheckMode)
    case PartnershipPreviousAddressId =>
      contactDetailsCYA
    case PartnershipEmailId =>
      contactDetailsCYA
    case PartnershipPhoneId =>
      contactDetailsCYA
  }

  override protected def updateRouteMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    case PartnershipContactAddressPostCodeLookupId =>
      PartnershipContactAddressListController.onPageLoad(UpdateMode)
    case PartnershipContactAddressId =>
      PartnershipConfirmPreviousAddressController.onPageLoad()
    case PartnershipAddressYearsId =>
      partnershipAddressYearsUpdateIdRoutes(ua)
    case PartnershipConfirmPreviousAddressId =>
      confirmPreviousAddressRoutes(ua)
    case PartnershipPreviousAddressPostCodeLookupId =>
      PartnershipPreviousAddressListController.onPageLoad(UpdateMode)
    case PartnershipPreviousAddressId => finishAmendmentNavigation(ua)
    case PartnershipEmailId => finishAmendmentNavigation(ua)
    case PartnershipPhoneId => finishAmendmentNavigation(ua)
  }

  private def finishAmendmentNavigation(answers: UserAnswers): Call = {
    answers.get(UpdateContactAddressId) match {
      case Some(_) => updateContactAddressCYAPage()
      case _ => anyMoreChanges
    }
  }

  private def updateContactAddressCYAPage():Call = controllers.routes.UpdateContactAddressCYAController.onPageLoad()
  private def detailsCYA: Call =
    controllers.register.administratorPartnership.partnershipDetails.routes.CheckYourAnswersController.onPageLoad()

  private def contactDetailsCYA: Call =
    controllers.register.administratorPartnership.contactDetails.routes.CheckYourAnswersController.onPageLoad()

  private def hasPaye(answers: UserAnswers): Boolean = answers.get(HasPAYEId).getOrElse(false)

  private def hasVat(answers: UserAnswers): Boolean = answers.get(HasVATId).getOrElse(false)

  private def anyMoreChanges: Call = controllers.register.routes.AnyMoreChangesController.onPageLoad()

  private def partnershipAddressYearsIdRoutes(answers: UserAnswers): Call = {
    answers.get(PartnershipAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        PartnershipTradingOverAYearController.onPageLoad(NormalMode)
      case Some(AddressYears.OverAYear) =>
        PartnershipEmailController.onPageLoad(NormalMode)
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def partnershipAddressYearsCheckIdRoutes(answers: UserAnswers): Call = {
    answers.get(PartnershipAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        PartnershipTradingOverAYearController.onPageLoad(CheckMode)
      case Some(AddressYears.OverAYear) =>
        contactDetailsCYA
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def hasBeenTradingIdRoutes(answers: UserAnswers): Call = {
    (answers.get(PartnershipTradingOverAYearId), answers.get(AreYouInUKId)) match {
      case (Some(true), Some(true)) =>
        PartnershipPreviousAddressPostCodeLookupController.onPageLoad(NormalMode)
      case (Some(true), Some(false)) =>
        PartnershipPreviousAddressController.onPageLoad(NormalMode)
      case (Some(false), _) =>
        PartnershipEmailController.onPageLoad(NormalMode)
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def hasBeenTradingCheckIdRoutes(answers: UserAnswers): Call = {
    (answers.get(PartnershipTradingOverAYearId), answers.get(AreYouInUKId)) match {
      case (Some(true), Some(true)) =>
        PartnershipPreviousAddressPostCodeLookupController.onPageLoad(CheckMode)
      case (Some(true), Some(false)) =>
        PartnershipPreviousAddressController.onPageLoad(CheckMode)
      case (Some(false), _) =>
        contactDetailsCYA
      case _ =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def partnershipAddressYearsUpdateIdRoutes(answers: UserAnswers): Call =
    answers.get(PartnershipAddressYearsId) match {
      case Some(AddressYears.UnderAYear) => PartnershipConfirmPreviousAddressController.onPageLoad()
      case Some(AddressYears.OverAYear) => anyMoreChanges
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }

  private def sameContactAddress(mode: Mode, answers: UserAnswers): Call = {
    (answers.get(PartnershipSameContactAddressId), answers.get(AreYouInUKId)) match {
      case (Some(true), _) => PartnershipAddressYearsController.onPageLoad(mode)
      case (Some(false), Some(false)) => PartnershipContactAddressController.onPageLoad(mode)
      case (Some(false), Some(true)) => PartnershipContactAddressPostCodeLookupController.onPageLoad(mode)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def regionBasedNavigation(answers: UserAnswers): Call = {
    answers.get(PartnershipAddressId) map { address =>
      countryOptions.regions(address.countryOpt.getOrElse("")) match {
        case UK => controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(CheckMode)
        case EuEea => controllers.register.administratorPartnership.partnershipDetails.routes.WhatYouWillNeedController.onPageLoad()
        case RestOfTheWorld => OutsideEuEeaController.onPageLoad()
        case _ => controllers.routes.SessionExpiredController.onPageLoad()
      }
    } getOrElse controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def confirmPreviousAddressRoutes(answers: UserAnswers): Call = {
    answers.get(PartnershipConfirmPreviousAddressId) match {
      case Some(false) => PartnershipPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode)
      case Some(true) => finishAmendmentNavigation(answers)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  private def registeredNameRoutes(answers: UserAnswers): Call =
    answers.get(IsRegisteredNameId) match {
      case Some(true) => ConfirmPartnershipDetailsController.onPageLoad()
      case _ => controllers.register.company.routes.CompanyUpdateDetailsController.onPageLoad()
    }

  private def partnerRoutes(answers: UserAnswers, mode: Mode): Call =
    if (answers.allPartnersAfterDelete(mode).isEmpty) {
      controllers.register.partnership.partners.routes.WhatYouWillNeedController.onPageLoad()
    }
    else {
      AddPartnerController.onPageLoad(mode)
    }

}
