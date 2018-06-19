/*
 * Copyright 2018 HM Revenue & Customs
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
import connectors.DataCacheConnector
import controllers.register.individual.routes
import identifiers.register.individual.WhatYouWillNeedId
import identifiers.register.individual._
import identifiers.{Identifier, LastPageId}
import models.{AddressYears, CheckMode, Mode, NormalMode}
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

@Singleton
class IndividualNavigator @Inject()(val dataCacheConnector: DataCacheConnector, config: FrontendAppConfig) extends Navigator {

  private def checkYourAnswers(): Option[NavigateTo] =
    NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())

  //noinspection ScalaStyle
  override def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case IndividualDetailsCorrectId => detailsCorrect(from.userAnswers)
    case WhatYouWillNeedId => contactAddressToggle(from.userAnswers)
    case IndividualSameContactAddressId => contactAddressRoutes(from.userAnswers, NormalMode)
    case IndividualContactAddressPostCodeLookupId => NavigateTo.dontSave(routes.IndividualContactAddressListController.onPageLoad(NormalMode))
    case IndividualContactAddressListId => NavigateTo.save(routes.IndividualContactAddressController.onPageLoad(NormalMode))
    case IndividualContactAddressId => NavigateTo.save(routes.IndividualAddressYearsController.onPageLoad(NormalMode))
    case IndividualAddressYearsId => addressYearsRoutes(from.userAnswers)
    case IndividualPreviousAddressPostCodeLookupId => NavigateTo.dontSave(routes.IndividualPreviousAddressListController.onPageLoad(NormalMode))
    case IndividualPreviousAddressListId => NavigateTo.save(routes.IndividualPreviousAddressController.onPageLoad(NormalMode))
    case IndividualPreviousAddressId => NavigateTo.save(routes.IndividualContactDetailsController.onPageLoad(NormalMode))
    case IndividualContactDetailsId => NavigateTo.save(routes.IndividualDateOfBirthController.onPageLoad(NormalMode))
    case IndividualDateOfBirthId => checkYourAnswers()
    case CheckYourAnswersId => NavigateTo.save(controllers.register.routes.DeclarationController.onPageLoad())
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  //noinspection ScalaStyle
  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case IndividualDateOfBirthId => checkYourAnswers()
    case IndividualSameContactAddressId => contactAddressRoutes(from.userAnswers, CheckMode)
    case IndividualContactAddressPostCodeLookupId => NavigateTo.dontSave(routes.IndividualContactAddressListController.onPageLoad(CheckMode))
    case IndividualContactAddressListId => NavigateTo.save(routes.IndividualContactAddressController.onPageLoad(CheckMode))
    case IndividualContactAddressId => NavigateTo.save(routes.IndividualAddressYearsController.onPageLoad(CheckMode))
    case IndividualAddressYearsId => addressYearsRouteCheckMode(from.userAnswers)
    case IndividualPreviousAddressPostCodeLookupId => NavigateTo.dontSave(routes.IndividualPreviousAddressListController.onPageLoad(CheckMode))
    case IndividualPreviousAddressListId => NavigateTo.save(routes.IndividualPreviousAddressController.onPageLoad(CheckMode))
    case IndividualPreviousAddressId => checkYourAnswers()
    case IndividualContactDetailsId => checkYourAnswers()
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  def detailsCorrect(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(IndividualDetailsCorrectId) match {
      case Some(true) =>
        answers.get(LastPageId) match {
          case Some(lastPage) => NavigateTo.dontSave(Call(lastPage.method, lastPage.url))
          case _ => NavigateTo.save(routes.WhatYouWillNeedController.onPageLoad())
        }
      case Some(false) =>
        NavigateTo.dontSave(routes.YouWillNeedToUpdateController.onPageLoad())
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  def addressYearsRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(IndividualAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(NormalMode))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(routes.IndividualContactDetailsController.onPageLoad(NormalMode))
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  def addressYearsRouteCheckMode(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(IndividualAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        NavigateTo.save(routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(CheckMode))
      case Some(AddressYears.OverAYear) =>
        NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  def contactAddressRoutes(answers: UserAnswers, mode: Mode): Option[NavigateTo] = {
    answers.get(IndividualSameContactAddressId) match {
      case Some(false) =>
        NavigateTo.save(routes.IndividualContactAddressPostCodeLookupController.onPageLoad(mode))
      case Some(true) =>
        answers.get(IndividualContactAddressId) match {
          case None =>
            NavigateTo.save(routes.IndividualContactAddressController.onPageLoad(mode))
          case Some(_) =>
            NavigateTo.save(routes.IndividualAddressYearsController.onPageLoad(mode))
        }
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  def contactAddressToggle(answers: UserAnswers): Option[NavigateTo] = {
    if (config.contactAddressEnabled) {
      NavigateTo.save(routes.IndividualSameContactAddressController.onPageLoad(NormalMode))
    } else {
      NavigateTo.save(routes.IndividualAddressYearsController.onPageLoad(NormalMode))
    }
  }

}
