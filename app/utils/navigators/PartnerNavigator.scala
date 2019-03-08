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
import controllers.register.partnership.partners.routes
import identifiers.register.partnership.partners._
import identifiers.register.partnership.{AddPartnersId, MoreThanTenPartnersId}
import models._
import models.Mode.journeyMode
import utils.{Navigator, UserAnswers}

@Singleton
class PartnerNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector, config: FrontendAppConfig) extends Navigator {

  private def checkYourAnswers(index: Int, mode: Mode = NormalMode): Option[NavigateTo] =
    NavigateTo.save(routes.CheckYourAnswersController.onPageLoad(index, mode))

  private def anyMoreChanges: Option[NavigateTo] =
    NavigateTo.dontSave(controllers.register.routes.AnyMoreChangesController.onPageLoad())


  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case MoreThanTenPartnersId => NavigateTo.save(controllers.register.partnership.routes.PartnershipReviewController.onPageLoad())
    case _ => commonMap(from, NormalMode)
  }

  override protected def updateRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case MoreThanTenPartnersId => NavigateTo.dontSave(controllers.register.routes.AnyMoreChangesController.onPageLoad())
    case PartnerConfirmPreviousAddressId(index) => confirmPreviousAddressRoutes(index, from.userAnswers)
    case _ => commonMap(from, UpdateMode)
  }

  private def confirmPreviousAddressRoutes(index: Int, answers: UserAnswers): Option[NavigateTo] =
    answers.get(PartnerConfirmPreviousAddressId(index)) match {
      case Some(true) => anyMoreChanges
      case Some(false) => NavigateTo.save(routes.PartnerPreviousAddressController.onPageLoad(UpdateMode, index))
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }

  private def previousAddressRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    answers.get(PartnerDetailsId(index)).map { person =>
      (person.isNew, mode) match {
        case (_, NormalMode) | (true, _) => NavigateTo.save(routes.PartnerContactDetailsController.onPageLoad(mode, index))
        case (false, UpdateMode) => anyMoreChanges
        case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
      }
    }.getOrElse(NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad()))
  }

  private def contactDetailsRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    answers.get(PartnerDetailsId(index)).map { person =>
      (person.isNew, mode) match {
        case (_, NormalMode) | (true, _) => checkYourAnswers(index, mode)
        case (false, UpdateMode) => anyMoreChanges
        case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
      }
    }.getOrElse(NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad()))
  }

  //noinspection ScalaStyle
  private def commonMap(from: NavigateFrom, mode: Mode): Option[NavigateTo] = from.id match {
    case AddPartnersId => addPartnerRoutes(from.userAnswers, mode)
    case PartnerDetailsId(index) => NavigateTo.save(routes.PartnerNinoController.onPageLoad(mode, index))
    case PartnerNinoId(index) => NavigateTo.save(routes.PartnerUniqueTaxReferenceController.onPageLoad(mode, index))
    case PartnerUniqueTaxReferenceId(index) => NavigateTo.save(routes.PartnerAddressPostCodeLookupController.onPageLoad(mode, index))
    case PartnerAddressPostCodeLookupId(index) => NavigateTo.dontSave(routes.PartnerAddressListController.onPageLoad(mode, index))
    case PartnerAddressListId(index) => NavigateTo.save(routes.PartnerAddressController.onPageLoad(mode, index))
    case PartnerAddressId(index) => NavigateTo.save(routes.PartnerAddressYearsController.onPageLoad(mode, index))
    case PartnerAddressYearsId(index) => partnerAddressYearsRoutes(index, from.userAnswers, mode)
    case PartnerPreviousAddressPostCodeLookupId(index) => NavigateTo.dontSave(routes.PartnerPreviousAddressListController.onPageLoad(mode, index))
    case PartnerPreviousAddressListId(index) => NavigateTo.save(routes.PartnerPreviousAddressController.onPageLoad(mode, index))
    case PartnerPreviousAddressId(index) => previousAddressRoutes(index, from.userAnswers, mode)
    case PartnerContactDetailsId(index) => contactDetailsRoutes(index, from.userAnswers, mode)
    case CheckYourAnswersId => NavigateTo.save(controllers.register.partnership.routes.AddPartnerController.onPageLoad(mode))
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  //noinspection ScalaStyle
  override protected def editRouteMap(from: NavigateFrom, mode: Mode): Option[NavigateTo] = from.id match {
    case PartnerDetailsId(index) => checkYourAnswers(index, journeyMode(mode))
    case PartnerNinoId(index) => checkYourAnswers(index, journeyMode(mode))
    case PartnerUniqueTaxReferenceId(index) => checkYourAnswers(index, journeyMode(mode))
    case PartnerAddressId(index) => checkYourAnswers(index, journeyMode(mode))
    case PartnerAddressYearsId(index) => partnerAddressYearsCheckRoutes(index, from.userAnswers, mode)
    case PartnerPreviousAddressId(index) => checkYourAnswers(index, journeyMode(mode))
    case PartnerContactDetailsId(index) => checkYourAnswers(index, journeyMode(mode))
    case _ => commonMap(from, mode)
  }

  private def partnerAddressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    (answers.get(PartnerAddressYearsId(index)), mode) match {
      case (Some(AddressYears.UnderAYear), NormalMode) => NavigateTo.save(routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, index))
      case (Some(AddressYears.UnderAYear), UpdateMode) =>
        answers.get(PartnerDetailsId(index)).map { person =>
          person.isNew match {
            case true => NavigateTo.save(routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, index))
            case false => NavigateTo.save(routes.PartnerConfirmPreviousAddressController.onPageLoad(index))
          }
        }.getOrElse(NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad()))

      case (Some(AddressYears.OverAYear), NormalMode) => NavigateTo.save(routes.PartnerContactDetailsController.onPageLoad(mode, index))
      case (Some(AddressYears.OverAYear), UpdateMode) => {
        answers.get(PartnerDetailsId(index)).map { person =>
          person.isNew match {
            case true => NavigateTo.save(routes.PartnerContactDetailsController.onPageLoad(mode, index))
            case false => anyMoreChanges
          }
        }.getOrElse(NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad()))
      }
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addPartnerRoutes(answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    answers.get(AddPartnersId) match {
      case Some(false) if mode == NormalMode => NavigateTo.save(controllers.register.partnership.routes.PartnershipReviewController.onPageLoad())
      case Some(false) if mode == UpdateMode => anyMoreChanges
      case _ =>
        val index = answers.allPartnersAfterDelete(mode).length
        if (index >= config.maxPartners) {
          NavigateTo.dontSave(controllers.register.partnership.routes.MoreThanTenPartnersController.onPageLoad(mode))
        } else {
          NavigateTo.save(controllers.register.partnership.partners.routes.PartnerDetailsController.onPageLoad(mode, answers.partnersCount))
        }
    }
  }

  private def partnerAddressYearsCheckRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    answers.get(PartnerAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, index))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(routes.CheckYourAnswersController.onPageLoad(index, journeyMode(mode)))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
