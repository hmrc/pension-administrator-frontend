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
import connectors.UserAnswersCacheConnector
import controllers.register.individual.routes
import identifiers.register.AreYouInUKId
import identifiers.register.individual._
import models.InternationalRegion._
import models.{AddressYears, CheckMode, Mode, NormalMode}
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}

@Singleton
class IndividualNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector,
                                    config: FrontendAppConfig,
                                    countryOptions: CountryOptions) extends Navigator {

  private def checkYourAnswers(): Option[NavigateTo] =
    NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())

  //noinspection ScalaStyle
  override def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case AreYouInUKId => countryOfRegistrationRoutes(from.userAnswers)
    case IndividualDetailsCorrectId => detailsCorrect(from.userAnswers)
    case IndividualDetailsId => NavigateTo.dontSave(routes.IndividualDateOfBirthController.onPageLoad(NormalMode))
    case IndividualAddressId => regionBasedNavigation(from.userAnswers)
    case WhatYouWillNeedId => NavigateTo.save(routes.IndividualSameContactAddressController.onPageLoad(NormalMode))
    case IndividualSameContactAddressId => contactAddressRoutes(from.userAnswers, NormalMode)
    case IndividualContactAddressPostCodeLookupId => NavigateTo.dontSave(routes.IndividualContactAddressListController.onPageLoad(NormalMode))
    case IndividualContactAddressListId => NavigateTo.save(routes.IndividualContactAddressController.onPageLoad(NormalMode))
    case IndividualContactAddressId => NavigateTo.save(routes.IndividualAddressYearsController.onPageLoad(NormalMode))
    case IndividualAddressYearsId => addressYearsRoutes(from.userAnswers)
    case IndividualPreviousAddressPostCodeLookupId => NavigateTo.dontSave(routes.IndividualPreviousAddressListController.onPageLoad(NormalMode))
    case IndividualPreviousAddressListId => NavigateTo.save(routes.IndividualPreviousAddressController.onPageLoad(NormalMode))
    case IndividualPreviousAddressId => NavigateTo.save(routes.IndividualContactDetailsController.onPageLoad(NormalMode))
    case IndividualContactDetailsId => countryBasedContactDetailsNavigation(from.userAnswers)
    case IndividualDateOfBirthId => countryBasedDobNavigation(from.userAnswers)
    case CheckYourAnswersId => NavigateTo.save(controllers.register.routes.DeclarationController.onPageLoad())
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  //noinspection ScalaStyle
  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case AreYouInUKId => countryOfRegistrationEditRoutes(from.userAnswers)
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
        NavigateTo.dontSave(routes.WhatYouWillNeedController.onPageLoad())
      case Some(false) =>
        NavigateTo.dontSave(routes.YouWillNeedToUpdateController.onPageLoad())
      case None =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  def addressYearsRoutes(answers: UserAnswers): Option[NavigateTo] = {
    (answers.get(IndividualAddressYearsId), answers.get(AreYouInUKId)) match {
      case (Some(AddressYears.UnderAYear), Some(false)) =>
        NavigateTo.save(routes.IndividualPreviousAddressController.onPageLoad(NormalMode))
      case (Some(AddressYears.UnderAYear), _) =>
        NavigateTo.save(routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(NormalMode))
      case (Some(AddressYears.OverAYear), _) =>
        NavigateTo.save(routes.IndividualContactDetailsController.onPageLoad(NormalMode))
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  def addressYearsRouteCheckMode(answers: UserAnswers): Option[NavigateTo] = {
    (answers.get(IndividualAddressYearsId), answers.get(AreYouInUKId)) match {
      case (Some(AddressYears.UnderAYear), Some(false)) =>
        NavigateTo.save(routes.IndividualPreviousAddressController.onPageLoad(CheckMode))
      case (Some(AddressYears.UnderAYear), _) =>
        NavigateTo.save(routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(CheckMode))
      case (Some(AddressYears.OverAYear), _) =>
        NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
      case _ =>
        NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  def contactAddressRoutes(answers: UserAnswers, mode: Mode): Option[NavigateTo] =
    if(config.nonUkJourneys) {
      (answers.get(IndividualSameContactAddressId), answers.get(AreYouInUKId)) match {
        case (_, None) => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
        case (Some(false), Some(false)) => NavigateTo.save(routes.IndividualContactAddressController.onPageLoad(mode))
        case (Some(false), Some(true)) => NavigateTo.save(routes.IndividualContactAddressPostCodeLookupController.onPageLoad(mode))
        case (Some(true), _) =>  contactAddressCompletionBasedNav(answers, mode)
        case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
      }
    } else {
      answers.get(IndividualSameContactAddressId) match {
        case Some(false) => NavigateTo.save(routes.IndividualContactAddressPostCodeLookupController.onPageLoad(mode))
        case Some(true) => contactAddressCompletionBasedNav(answers, mode)
        case None => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
      }
    }

  private def contactAddressCompletionBasedNav(answers: UserAnswers, mode: Mode): Option[NavigateTo] =
    answers.get(IndividualContactAddressId) match {
    case None =>
      NavigateTo.save(routes.IndividualContactAddressController.onPageLoad(mode))
    case Some(_) =>
      NavigateTo.save(routes.IndividualAddressYearsController.onPageLoad(mode))
  }


  def countryOfRegistrationRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(AreYouInUKId) match {
      case Some(false) => NavigateTo.dontSave(routes.IndividualNameController.onPageLoad(NormalMode))
      case Some(true) => NavigateTo.dontSave(routes.IndividualDetailsCorrectController.onPageLoad(NormalMode))
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  def countryOfRegistrationEditRoutes(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(AreYouInUKId) match {
      case Some(false) => NavigateTo.dontSave(routes.IndividualRegisteredAddressController.onPageLoad())
      case Some(true) => NavigateTo.dontSave(routes.IndividualDetailsCorrectController.onPageLoad(NormalMode))
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  }

  def countryBasedDobNavigation(answers: UserAnswers): Option[NavigateTo] =
    if(config.nonUkJourneys) {
      answers.get(AreYouInUKId) match {
        case Some(false) => NavigateTo.dontSave(routes.IndividualRegisteredAddressController.onPageLoad())
        case Some(true) => checkYourAnswers()
        case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
      }
    } else {
      checkYourAnswers()
    }

  private def regionBasedNavigation(answers: UserAnswers): Option[NavigateTo] = {
    answers.get(IndividualAddressId) flatMap { address =>
      countryOptions.regions(address.country.getOrElse("")) match {
        case UK => NavigateTo.dontSave(routes.IndividualAreYouInUKController.onPageLoad(CheckMode))
        case EuEea => NavigateTo.dontSave(routes.WhatYouWillNeedController.onPageLoad())
        case RestOfTheWorld => NavigateTo.dontSave(routes.OutsideEuEeaController.onPageLoad())
        case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
      }
    }
  }

  def countryBasedContactDetailsNavigation(answers: UserAnswers): Option[NavigateTo] =
    if(config.nonUkJourneys) {
      answers.get(AreYouInUKId) match {
      case Some(false) => checkYourAnswers()
      case Some(true) => NavigateTo.dontSave(routes.IndividualDateOfBirthController.onPageLoad(NormalMode))
      case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
    }
  } else {
      NavigateTo.dontSave(routes.IndividualDateOfBirthController.onPageLoad(NormalMode))
  }


}
