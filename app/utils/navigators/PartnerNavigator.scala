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
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

@Singleton
class PartnerNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector, config: FrontendAppConfig) extends Navigator {

  private def checkYourAnswersPage(index: Int, mode: Mode = NormalMode) =  routes.CheckYourAnswersController.onPageLoad(index, mode)
  private def checkYourAnswers(index: Int, mode: Mode = NormalMode): Option[NavigateTo] = NavigateTo.save(checkYourAnswersPage(index, mode))
  private def anyMoreChangesPage = controllers.register.routes.AnyMoreChangesController.onPageLoad()
  private def anyMoreChanges: Option[NavigateTo] = NavigateTo.dontSave(anyMoreChangesPage)
  private def sessionExpired: Option[NavigateTo] = NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())


  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case MoreThanTenPartnersId => NavigateTo.save(controllers.register.partnership.routes.PartnershipReviewController.onPageLoad())
    case _ => commonRouteMap(from, NormalMode)
  }

  override protected def updateRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case MoreThanTenPartnersId => anyMoreChanges
    case PartnerConfirmPreviousAddressId(index) => confirmPreviousAddressRoutes(index, from.userAnswers)
    case _ => commonRouteMap(from, UpdateMode)
  }

  //noinspection ScalaStyle
  private def commonRouteMap(from: NavigateFrom, mode: Mode): Option[NavigateTo] = from.id match {
    case AddPartnersId => addPartnerRoutes(from.userAnswers, mode)
    case PartnerNameId(index) => NavigateTo.save(routes.PartnerDOBController.onPageLoad(mode, index))
    case PartnerDOBId(index) => NavigateTo.save(routes.PartnerNinoController.onPageLoad(mode, index))
    case PartnerNinoId(index) => ninoRoutes(index, from.userAnswers, mode)
    case PartnerUniqueTaxReferenceId(index) => utrRoutes(index, from.userAnswers, mode)
    case PartnerAddressPostCodeLookupId(index) => NavigateTo.dontSave(routes.PartnerAddressListController.onPageLoad(mode, index))
    case PartnerAddressListId(index) => NavigateTo.save(routes.PartnerAddressController.onPageLoad(mode, index))
    case PartnerAddressId(index) => NavigateTo.save(routes.PartnerAddressYearsController.onPageLoad(mode, index))
    case PartnerAddressYearsId(index) => partnerAddressYearsRoutes(index, from.userAnswers, mode)
    case PartnerPreviousAddressPostCodeLookupId(index) => NavigateTo.dontSave(routes.PartnerPreviousAddressListController.onPageLoad(mode, index))
    case PartnerPreviousAddressListId(index) => NavigateTo.save(routes.PartnerPreviousAddressController.onPageLoad(mode, index))
    case PartnerPreviousAddressId(index) => previousAddressRoutes(index, from.userAnswers, mode)
    case PartnerContactDetailsId(index) => contactDetailsRoutes(index, from.userAnswers, mode)
    case CheckYourAnswersId => NavigateTo.save(controllers.register.partnership.routes.AddPartnerController.onPageLoad(mode))
    case _ => sessionExpired
  }

  private def confirmPreviousAddressRoutes(index: Int, answers: UserAnswers): Option[NavigateTo] =
    answers.get(PartnerConfirmPreviousAddressId(index)) match {
      case Some(true) => anyMoreChanges
      case Some(false) => NavigateTo.save(routes.PartnerPreviousAddressController.onPageLoad(UpdateMode, index))
      case _ => sessionExpired
    }

  private def previousAddressRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    mode match {
      case NormalMode =>
        NavigateTo.save(routes.PartnerContactDetailsController.onPageLoad(mode, index))
      case UpdateMode =>
        redirectBasedOnIsNew(
          answers,
          index,
          routes.PartnerContactDetailsController.onPageLoad(mode, index),
          anyMoreChangesPage
        )
      case _ => sessionExpired
    }
  }

  private def ninoRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    mode match {
      case NormalMode => NavigateTo.save(routes.PartnerUniqueTaxReferenceController.onPageLoad(mode, index))
      case UpdateMode => redirectBasedOnIsNew(answers, index,
        routes.PartnerUniqueTaxReferenceController.onPageLoad(mode, index), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def utrRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    mode match {
      case NormalMode => NavigateTo.save(routes.PartnerAddressPostCodeLookupController.onPageLoad(mode, index))
      case UpdateMode => redirectBasedOnIsNew(answers, index,
        routes.PartnerAddressPostCodeLookupController.onPageLoad(mode, index), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  private def contactDetailsRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    mode match {
      case NormalMode => checkYourAnswers(index, mode)
      case UpdateMode => redirectBasedOnIsNew(answers, index, checkYourAnswersPage(index, mode), anyMoreChangesPage)
      case _ => sessionExpired
    }
  }

  //noinspection ScalaStyle
  override protected def editRouteMap(from: NavigateFrom, mode: Mode): Option[NavigateTo] = from.id match {
    case PartnerNameId(index) => checkYourAnswers(index, journeyMode(mode))
    case PartnerDOBId(index) => checkYourAnswers(index, journeyMode(mode))
    case PartnerNinoId(index) => checkYourAnswers(index, journeyMode(mode))
    case PartnerUniqueTaxReferenceId(index) => checkYourAnswers(index, journeyMode(mode))
    case PartnerAddressId(index) => checkYourAnswers(index, journeyMode(mode))
    case PartnerAddressYearsId(index) => partnerAddressYearsCheckRoutes(index, from.userAnswers, mode)
    case PartnerPreviousAddressId(index) => checkYourAnswers(index, journeyMode(mode))
    case PartnerContactDetailsId(index) => checkYourAnswers(index, journeyMode(mode))
    case _ => commonRouteMap(from, mode)
  }

  private def partnerAddressYearsRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    (answers.get(PartnerAddressYearsId(index)), mode) match {
      case (Some(AddressYears.UnderAYear), NormalMode) =>
        NavigateTo.save(routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, index))
      case (Some(AddressYears.UnderAYear), UpdateMode) =>
        redirectBasedOnIsNew(answers, index,
            routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, index),
            routes.PartnerConfirmPreviousAddressController.onPageLoad(index)
          )
      case (Some(AddressYears.OverAYear), NormalMode) =>
        NavigateTo.save(routes.PartnerContactDetailsController.onPageLoad(mode, index))
      case (Some(AddressYears.OverAYear), UpdateMode) =>
        redirectBasedOnIsNew(answers, index,
          routes.PartnerContactDetailsController.onPageLoad(mode, index),
          anyMoreChangesPage
        )
      case _ => sessionExpired
    }
  }

  private def redirectBasedOnIsNew(answers: UserAnswers, index: Int, ifNewRoute: Call, ifNotNew: Call): Option[NavigateTo] = {
    answers.get(PartnerNameId(index)).map { person =>
      if (person.isNew) {
        NavigateTo.save(ifNewRoute)
      } else {
        NavigateTo.save(ifNotNew)
      }
    }.getOrElse(sessionExpired)
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
          NavigateTo.save(controllers.register.partnership.partners.routes.PartnerNameController.onPageLoad(mode, answers.partnersCount))
        }
    }
  }

  private def partnerAddressYearsCheckRoutes(index: Int, answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    answers.get(PartnerAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, index))
      case Some(AddressYears.OverAYear) =>
        checkYourAnswers(index, journeyMode(mode))
      case None => sessionExpired
    }
  }
}
