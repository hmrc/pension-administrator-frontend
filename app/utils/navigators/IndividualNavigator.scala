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
import controllers.register.individual.routes
import identifiers.Identifier
import identifiers.register.AreYouInUKId
import identifiers.register.individual._
import models.InternationalRegion._
import models._
import play.api.mvc.Call
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}

@Singleton
class IndividualNavigator @Inject()(config: FrontendAppConfig,
                                    countryOptions: CountryOptions) extends Navigator {

  //noinspection ScalaStyle
  override def routeMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    case AreYouInUKId => countryOfRegistrationRoutes(ua)
    case IndividualDetailsCorrectId => detailsCorrect(ua)
    case IndividualDetailsId => routes.IndividualRegisteredAddressController.onPageLoad(NormalMode)
    case IndividualAddressId => regionBasedNavigation(ua)
    case WhatYouWillNeedId => routes.IndividualAreYouInUKController.onPageLoad(NormalMode)
    case IndividualSameContactAddressId => contactAddressRoutes(ua, NormalMode)
    case IndividualContactAddressPostCodeLookupId => routes.IndividualContactAddressListController.onPageLoad(NormalMode)
    case IndividualContactAddressListId => routes.IndividualContactAddressController.onPageLoad(NormalMode)
    case IndividualContactAddressId => routes.IndividualAddressYearsController.onPageLoad(NormalMode)
    case IndividualAddressYearsId => addressYearsRoutes(ua)
    case IndividualPreviousAddressPostCodeLookupId => routes.IndividualPreviousAddressListController.onPageLoad(NormalMode)
    case IndividualPreviousAddressListId => routes.IndividualPreviousAddressController.onPageLoad(NormalMode)
    case IndividualPreviousAddressId => routes.IndividualEmailController.onPageLoad(NormalMode)
    case IndividualEmailId =>
      routes.IndividualPhoneController.onPageLoad(NormalMode)
    case IndividualPhoneId => countryBasedContactDetailsNavigation(ua)
    case IndividualDateOfBirthId => countryBasedDobNavigation(ua)
    case CheckYourAnswersId => controllers.register.routes.DeclarationController.onPageLoad()
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  //noinspection ScalaStyle
  override protected def editRouteMap(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case AreYouInUKId => countryOfRegistrationEditRoutes(ua)
    case IndividualDateOfBirthId => checkYourAnswers
    case IndividualSameContactAddressId => contactAddressRoutes(ua, CheckMode)
    case IndividualContactAddressPostCodeLookupId => routes.IndividualContactAddressListController.onPageLoad(CheckMode)
    case IndividualContactAddressListId => routes.IndividualContactAddressController.onPageLoad(CheckMode)
    case IndividualContactAddressId => routes.IndividualAddressYearsController.onPageLoad(CheckMode)
    case IndividualAddressYearsId => addressYearsRouteCheckMode(ua)
    case IndividualPreviousAddressPostCodeLookupId => routes.IndividualPreviousAddressListController.onPageLoad(CheckMode)
    case IndividualPreviousAddressListId => routes.IndividualPreviousAddressController.onPageLoad(CheckMode)
    case IndividualPreviousAddressId => checkYourAnswers
    case IndividualEmailId => checkYourAnswers
    case IndividualPhoneId => checkYourAnswers

    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  //noinspection ScalaStyle
  override protected def updateRouteMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    case IndividualContactAddressPostCodeLookupId => routes.IndividualContactAddressListController.onPageLoad(UpdateMode)
    case IndividualContactAddressListId => routes.IndividualContactAddressController.onPageLoad(UpdateMode)
    case IndividualContactAddressId => routes.IndividualAddressYearsController.onPageLoad(UpdateMode)
    case IndividualAddressYearsId => addressYearsRoutesUpdateMode(ua)
    case IndividualConfirmPreviousAddressId => confirmPreviousAddressRoutes(ua)
    case IndividualPreviousAddressPostCodeLookupId => routes.IndividualPreviousAddressListController.onPageLoad(UpdateMode)
    case IndividualPreviousAddressListId => routes.IndividualPreviousAddressController.onPageLoad(UpdateMode)
    case IndividualPreviousAddressId => anyMoreChanges
    case IndividualEmailId =>
      anyMoreChanges
    case IndividualPhoneId =>
      anyMoreChanges
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def checkYourAnswers: Call =
    routes.CheckYourAnswersController.onPageLoad()

  private def anyMoreChanges: Call =
    controllers.register.routes.AnyMoreChangesController.onPageLoad()


  def detailsCorrect(answers: UserAnswers): Call = {
    answers.get(IndividualDetailsCorrectId) match {
      case Some(true) =>
        routes.IndividualDateOfBirthController.onPageLoad(NormalMode)
      case Some(false) =>
        routes.YouWillNeedToUpdateController.onPageLoad()
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  def addressYearsRoutes(answers: UserAnswers): Call =
    (answers.get(IndividualAddressYearsId), answers.get(AreYouInUKId)) match {
      case (_, None) => controllers.routes.SessionExpiredController.onPageLoad()
      case (Some(AddressYears.UnderAYear), Some(false)) =>
        routes.IndividualPreviousAddressController.onPageLoad(NormalMode)
      case (Some(AddressYears.UnderAYear), Some(true)) =>
        routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(NormalMode)
      case (Some(AddressYears.OverAYear), _) =>
        routes.IndividualEmailController.onPageLoad(NormalMode)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }


  def addressYearsRouteCheckMode(answers: UserAnswers): Call =
    (answers.get(IndividualAddressYearsId), answers.get(AreYouInUKId)) match {
      case (_, None) => controllers.routes.SessionExpiredController.onPageLoad()
      case (Some(AddressYears.UnderAYear), Some(false)) =>
        routes.IndividualPreviousAddressController.onPageLoad(CheckMode)
      case (Some(AddressYears.UnderAYear), Some(true)) =>
        routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(CheckMode)
      case (Some(AddressYears.OverAYear), _) =>
        routes.CheckYourAnswersController.onPageLoad()
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }

  def addressYearsRoutesUpdateMode(answers: UserAnswers): Call =
    answers.get(IndividualAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        routes.IndividualConfirmPreviousAddressController.onPageLoad()
      case Some(AddressYears.OverAYear) =>
        anyMoreChanges
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }

  private def confirmPreviousAddressRoutes(answers: UserAnswers):  Call =
    answers.get(IndividualConfirmPreviousAddressId) match {
      case Some(true) => anyMoreChanges
      case Some(false) => routes.IndividualPreviousAddressController.onPageLoad(UpdateMode)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }

  def contactAddressRoutes(answers: UserAnswers, mode: Mode): Call =
    (answers.get(IndividualSameContactAddressId), answers.get(AreYouInUKId)) match {
      case (_, None) => controllers.routes.SessionExpiredController.onPageLoad()
      case (Some(false), Some(false)) => routes.IndividualContactAddressController.onPageLoad(mode)
      case (Some(false), Some(true)) => routes.IndividualContactAddressPostCodeLookupController.onPageLoad(mode)
      case (Some(true), _) => contactAddressCompletionBasedNav(answers, mode)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }


  private def contactAddressCompletionBasedNav(answers: UserAnswers, mode: Mode): Call =
    answers.get(IndividualContactAddressId) match {
      case None =>
        routes.IndividualContactAddressController.onPageLoad(mode)
      case Some(_) =>
        routes.IndividualAddressYearsController.onPageLoad(mode)
    }


  def countryOfRegistrationRoutes(answers: UserAnswers): Call = {
    answers.get(AreYouInUKId) match {
      case Some(false) => routes.IndividualNameController.onPageLoad(NormalMode)
      case Some(true) => routes.IndividualDetailsCorrectController.onPageLoad(NormalMode)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  def countryOfRegistrationEditRoutes(answers: UserAnswers): Call = {
    answers.get(AreYouInUKId) match {
      case Some(false) => answers.get(IndividualDetailsId) match {
        case None => routes.IndividualNameController.onPageLoad(NormalMode)
        case _ => routes.IndividualRegisteredAddressController.onPageLoad(NormalMode)
      }
      case Some(true) => routes.IndividualDetailsCorrectController.onPageLoad(NormalMode)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  def countryBasedDobNavigation(answers: UserAnswers): Call =
    answers.get(AreYouInUKId) match {
      case Some(_) => routes.IndividualSameContactAddressController.onPageLoad(NormalMode)
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }


  private def regionBasedNavigation(answers: UserAnswers): Call = {
    answers.get(IndividualAddressId).fold(controllers.routes.SessionExpiredController.onPageLoad())(address =>
      countryOptions.regions(address.country.getOrElse("")) match {
        case UK => routes.IndividualAreYouInUKController.onPageLoad(CheckMode)
        case EuEea => routes.IndividualDateOfBirthController.onPageLoad(NormalMode)
        case RestOfTheWorld => routes.OutsideEuEeaController.onPageLoad()
        case _ => controllers.routes.SessionExpiredController.onPageLoad()
      }
    )
  }

  def countryBasedContactDetailsNavigation(answers: UserAnswers): Call =
    answers.get(AreYouInUKId) match {
      case Some(false) => checkYourAnswers
      case Some(true) => answers.get(IndividualDateOfBirthId).fold(
        routes.IndividualDateOfBirthController.onPageLoad(NormalMode))(_ =>
        routes.CheckYourAnswersController.onPageLoad())
      case _ => controllers.routes.SessionExpiredController.onPageLoad()
    }

}
