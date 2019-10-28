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

@Singleton
class PartnershipNavigator @Inject()(
                                      val dataCacheConnector: UserAnswersCacheConnector,
                                      countryOptions: CountryOptions,
                                      appConfig: FrontendAppConfig) extends Navigator {

  //scalastyle:off cyclomatic.complexity
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case PartnershipDetailsId => regionBasedNameNavigation(from.userAnswers)
    case ConfirmPartnershipDetailsId =>
      NavigateTo.dontSave(routes.WhatYouWillNeedController.onPageLoad())
    case WhatYouWillNeedId =>
      NavigateTo.save(routes.PartnershipSameContactAddressController.onPageLoad(NormalMode))
    case PartnershipSameContactAddressId =>
      sameContactAddress(NormalMode, from.userAnswers)
    case PartnershipContactAddressPostCodeLookupId =>
      NavigateTo.save(routes.PartnershipContactAddressListController.onPageLoad(NormalMode))
    case PartnershipContactAddressListId =>
      NavigateTo.save(routes.PartnershipContactAddressController.onPageLoad(NormalMode))
    case PartnershipContactAddressId =>
      NavigateTo.save(routes.PartnershipAddressYearsController.onPageLoad(NormalMode))
    case PartnershipAddressYearsId =>
      addressYearsRoutes(from.userAnswers, NormalMode)
    case PartnershipPreviousAddressPostCodeLookupId =>
      NavigateTo.save(routes.PartnershipPreviousAddressListController.onPageLoad(NormalMode))
    case PartnershipPreviousAddressListId =>
      NavigateTo.save(routes.PartnershipPreviousAddressController.onPageLoad(NormalMode))
    case PartnershipPreviousAddressId =>
      NavigateTo.save(routes.PartnershipEmailController.onPageLoad(NormalMode))
    case PartnershipEmailId =>
      NavigateTo.save(routes.PartnershipPhoneController.onPageLoad(NormalMode))
    case PartnershipPhoneId =>
      regionBasedContactDetailsRoutes(from.userAnswers)
    case PartnershipVatId =>
      NavigateTo.save(routes.PartnershipPayeController.onPageLoad(NormalMode))
    case PartnershipPayeId =>
      NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
    case CheckYourAnswersId =>
      NavigateTo.save(routes.AddPartnerController.onPageLoad(NormalMode))
    case PartnershipReviewId =>
      NavigateTo.save(controllers.register.routes.DeclarationController.onPageLoad())
    case PartnershipRegisteredAddressId =>
      regionBasedNavigation(from.userAnswers)
    case _ =>
      NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  override protected def editRouteMap(from: NavigateFrom, mode: Mode): Option[NavigateTo] = {
    from.id match {
      case PartnershipSameContactAddressId =>
        sameContactAddress(CheckMode, from.userAnswers)
      case PartnershipContactAddressPostCodeLookupId =>
        NavigateTo.save(routes.PartnershipContactAddressListController.onPageLoad(CheckMode))
      case PartnershipContactAddressListId =>
        NavigateTo.save(routes.PartnershipContactAddressController.onPageLoad(CheckMode))
      case PartnershipContactAddressId =>
        NavigateTo.save(routes.PartnershipAddressYearsController.onPageLoad(CheckMode))
      case PartnershipAddressYearsId =>
        addressYearsRoutes(from.userAnswers, CheckMode)
      case PartnershipPreviousAddressPostCodeLookupId =>
        NavigateTo.save(routes.PartnershipPreviousAddressListController.onPageLoad(CheckMode))
      case PartnershipPreviousAddressListId =>
        NavigateTo.save(routes.PartnershipPreviousAddressController.onPageLoad(CheckMode))
      case PartnershipPreviousAddressId =>
        NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
      case PartnershipEmailId =>
        NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
      case PartnershipPhoneId =>
        NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
      case PartnershipVatId =>
        NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
      case PartnershipPayeId =>
        NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  override protected def updateRouteMap(from: NavigateFrom): Option[NavigateTo] = {
    from.id match {
      case PartnershipContactAddressPostCodeLookupId =>
        NavigateTo.save(routes.PartnershipContactAddressListController.onPageLoad(UpdateMode))
      case PartnershipContactAddressListId =>
        NavigateTo.save(routes.PartnershipContactAddressController.onPageLoad(UpdateMode))
      case PartnershipContactAddressId =>
        NavigateTo.dontSave(routes.PartnershipAddressYearsController.onPageLoad(UpdateMode))
      case PartnershipAddressYearsId =>
        addressYearsRoutes(from.userAnswers, UpdateMode)
      case PartnershipPhoneId =>
        NavigateTo.dontSave(controllers.register.routes.AnyMoreChangesController.onPageLoad())
      case PartnershipEmailId =>
        NavigateTo.dontSave(controllers.register.routes.AnyMoreChangesController.onPageLoad())
      case PartnershipPreviousAddressPostCodeLookupId =>
        NavigateTo.dontSave(routes.PartnershipPreviousAddressListController.onPageLoad(UpdateMode))
      case PartnershipPreviousAddressListId =>
        NavigateTo.dontSave(routes.PartnershipPreviousAddressController.onPageLoad(UpdateMode))
      case PartnershipPreviousAddressId =>
        NavigateTo.dontSave(controllers.register.routes.AnyMoreChangesController.onPageLoad())
      case PartnershipConfirmPreviousAddressId =>
        variationManualPreviousAddressRoutes(from.userAnswers, UpdateMode)
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }


  private def addressYearsRoutes(answers: UserAnswers, mode:Mode): Option[NavigateTo] = {
    (answers.get(PartnershipAddressYearsId), answers.get(AreYouInUKId)) match {
      case (Some(AddressYears.UnderAYear), Some(false)) =>
        mode match {
          case NormalMode | CheckMode => NavigateTo.dontSave(routes.PartnershipPreviousAddressController.onPageLoad(mode))
          case UpdateMode | CheckUpdateMode => NavigateTo.dontSave(routes.PartnershipConfirmPreviousAddressController.onPageLoad())
        }

      case (Some(AddressYears.UnderAYear), Some(true)) =>
        mode match {
          case NormalMode | CheckMode => NavigateTo.dontSave(routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(mode))
          case UpdateMode | CheckUpdateMode => NavigateTo.dontSave(routes.PartnershipConfirmPreviousAddressController.onPageLoad())
        }
      case (Some(AddressYears.OverAYear), _) =>
        mode match {
          case NormalMode => NavigateTo.dontSave(routes.PartnershipEmailController.onPageLoad(NormalMode))
          case CheckMode => NavigateTo.dontSave(routes.CheckYourAnswersController.onPageLoad())
          case UpdateMode | CheckUpdateMode => NavigateTo.dontSave(controllers.register.routes.AnyMoreChangesController.onPageLoad())
        }
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def sameContactAddress(mode: Mode, answers: UserAnswers): Option[NavigateTo] = {
    (answers.get(PartnershipSameContactAddressId), answers.get(AreYouInUKId)) match {
      case (Some(true), _) => NavigateTo.save(routes.PartnershipAddressYearsController.onPageLoad(mode))
      case (Some(false), Some(false)) => NavigateTo.save(routes.PartnershipContactAddressController.onPageLoad(mode))
      case (Some(false), Some(true)) => NavigateTo.save(routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(mode))
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }


  private def regionBasedNameNavigation(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(AreYouInUKId) match {
      case Some(false) => NavigateTo.dontSave(routes.PartnershipRegisteredAddressController.onPageLoad())
      case Some(true) => NavigateTo.dontSave(routes.ConfirmPartnershipDetailsController.onPageLoad())
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def regionBasedNavigation(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnershipRegisteredAddressId) flatMap { address =>
      countryOptions.regions(address.country.getOrElse("")) match {
        case UK => NavigateTo.dontSave(controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(CheckMode))
        case EuEea => NavigateTo.dontSave(routes.WhatYouWillNeedController.onPageLoad())
        case RestOfTheWorld => NavigateTo.dontSave(routes.OutsideEuEeaController.onPageLoad())
        case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
      }
    }
  }

  private def regionBasedContactDetailsRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(AreYouInUKId) match {
      case Some(false) => NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
      case Some(true) => NavigateTo.save(routes.PartnershipVatController.onPageLoad(NormalMode))
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def variationManualPreviousAddressRoutes(answers: UserAnswers, mode:Mode): Option[NavigateTo] = {
    answers.get(PartnershipConfirmPreviousAddressId) match {
      case Some(false) =>NavigateTo.dontSave(routes.PartnershipPreviousAddressController.onPageLoad(mode))
      case Some(true) => NavigateTo.dontSave(controllers.register.routes.AnyMoreChangesController.onPageLoad())
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
