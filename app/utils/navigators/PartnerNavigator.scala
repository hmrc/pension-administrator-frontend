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
import utils.{Navigator, UserAnswers}

@Singleton
class PartnerNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector, config: FrontendAppConfig) extends Navigator {

  private def checkYourAnswers(index: Int): Option[NavigateTo] =
    NavigateTo.save(routes.CheckYourAnswersController.onPageLoad(index, NormalMode))

  //noinspection ScalaStyle
  override protected def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case AddPartnersId => addPartnerRoutes(from.userAnswers, NormalMode)
    case MoreThanTenPartnersId => NavigateTo.save(controllers.register.partnership.routes.PartnershipReviewController.onPageLoad())
    case PartnerDetailsId(index) => NavigateTo.save(routes.PartnerNinoController.onPageLoad(NormalMode, index))
    case PartnerNinoId(index) => NavigateTo.save(routes.PartnerUniqueTaxReferenceController.onPageLoad(NormalMode, index))
    case PartnerUniqueTaxReferenceId(index) => NavigateTo.save(routes.PartnerAddressPostCodeLookupController.onPageLoad(NormalMode, index))
    case PartnerAddressPostCodeLookupId(index) => NavigateTo.dontSave(routes.PartnerAddressListController.onPageLoad(NormalMode, index))
    case PartnerAddressListId(index) => NavigateTo.save(routes.PartnerAddressController.onPageLoad(NormalMode, index))
    case PartnerAddressId(index) => NavigateTo.save(routes.PartnerAddressYearsController.onPageLoad(NormalMode, index))
    case PartnerAddressYearsId(index) => partnerAddressYearsRoutes(index, from.userAnswers)
    case PartnerPreviousAddressPostCodeLookupId(index) => NavigateTo.dontSave(routes.PartnerPreviousAddressListController.onPageLoad(NormalMode, index))
    case PartnerPreviousAddressListId(index) => NavigateTo.save(routes.PartnerPreviousAddressController.onPageLoad(NormalMode, index))
    case PartnerPreviousAddressId(index) => NavigateTo.save(routes.PartnerContactDetailsController.onPageLoad(NormalMode, index))
    case PartnerContactDetailsId(index) => checkYourAnswers(index)
    case CheckYourAnswersId => NavigateTo.save(controllers.register.partnership.routes.AddPartnerController.onPageLoad(NormalMode))
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  //noinspection ScalaStyle
  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case AddPartnersId => addPartnerRoutes(from.userAnswers, CheckMode)
    case PartnerDetailsId(index) => checkYourAnswers(index)
    case PartnerNinoId(index) => checkYourAnswers(index)
    case PartnerUniqueTaxReferenceId(index) => checkYourAnswers(index)
    case PartnerAddressPostCodeLookupId(index) => NavigateTo.dontSave(routes.PartnerAddressListController.onPageLoad(CheckMode, index))
    case PartnerAddressListId(index) => NavigateTo.save(routes.PartnerAddressController.onPageLoad(CheckMode, index))
    case PartnerAddressId(index) => checkYourAnswers(index)
    case PartnerAddressYearsId(index) => partnerAddressYearsCheckRoutes(index, from.userAnswers)
    case PartnerPreviousAddressPostCodeLookupId(index) => NavigateTo.dontSave(routes.PartnerPreviousAddressListController.onPageLoad(CheckMode, index))
    case PartnerPreviousAddressListId(index) => NavigateTo.save(routes.PartnerPreviousAddressController.onPageLoad(CheckMode, index))
    case PartnerPreviousAddressId(index) => checkYourAnswers(index)
    case PartnerContactDetailsId(index) => checkYourAnswers(index)
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  override protected def updateRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case AddPartnersId => addPartnerRoutes(from.userAnswers, UpdateMode)
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  private def partnerAddressYearsRoutes(index: Int, answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnerAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) => NavigateTo.save(routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(NormalMode, index))
      case Some(AddressYears.OverAYear) => NavigateTo.save(routes.PartnerContactDetailsController.onPageLoad(NormalMode, index))
      case None => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  private def addPartnerRoutes(answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    answers.get(AddPartnersId) match {
      case Some(false) => NavigateTo.save(controllers.register.partnership.routes.PartnershipReviewController.onPageLoad())
      case _ =>
        val index = answers.allPartnersAfterDelete(mode).length
        if (index >= config.maxPartners) {
          NavigateTo.save(controllers.register.partnership.routes.MoreThanTenPartnersController.onPageLoad(NormalMode))
        } else {
          NavigateTo.save(controllers.register.partnership.partners.routes.PartnerDetailsController.onPageLoad(mode, answers.partnersCount))
        }
    }
  }

  private def partnerAddressYearsCheckRoutes(index: Int, answers: UserAnswers): Option[NavigateTo] = {
    answers.get(PartnerAddressYearsId(index)) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(CheckMode, index))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(routes.CheckYourAnswersController.onPageLoad(index, NormalMode))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }
}
