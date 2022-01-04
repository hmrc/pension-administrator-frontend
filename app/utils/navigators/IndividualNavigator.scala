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
import config.FrontendAppConfig
import controllers.register.individual.routes._
import identifiers.register.AreYouInUKId
import identifiers.register.individual._
import models.InternationalRegion._
import models._
import play.api.mvc.Call
import utils.countryOptions.CountryOptions
import utils.{Navigator, UserAnswers}
import controllers.routes.SessionExpiredController
import identifiers.{Identifier, UpdateContactAddressId}

@Singleton
class IndividualNavigator @Inject()(config: FrontendAppConfig,
                                    countryOptions: CountryOptions) extends Navigator {

  //noinspection ScalaStyle
  override def routeMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    case AreYouInUKId => countryOfRegistrationRoutes(ua)

    case IndividualDetailsCorrectId => detailsCorrect(ua)
    case IndividualDetailsId => IndividualRegisteredAddressController.onPageLoad(NormalMode)

    case IndividualAddressId => regionBasedNavigation(ua)
    case WhatYouWillNeedId => IndividualAreYouInUKController.onPageLoad(NormalMode)
    case IndividualSameContactAddressId => contactAddressRoutes(ua, NormalMode)

    case IndividualContactAddressPostCodeLookupId => IndividualContactAddressListController.onPageLoad(NormalMode)
    case IndividualContactAddressId => IndividualAddressYearsController.onPageLoad(NormalMode)
    case IndividualAddressYearsId => addressYearsRoutes(ua)
    case IndividualPreviousAddressPostCodeLookupId => IndividualPreviousAddressListController.onPageLoad(NormalMode)
    case IndividualPreviousAddressId => IndividualEmailController.onPageLoad(NormalMode)

    case IndividualEmailId => IndividualPhoneController.onPageLoad(NormalMode)
    case IndividualPhoneId => countryBasedContactDetailsNavigation(ua)
    case IndividualDateOfBirthId => countryBasedDobNavigation(ua)

    case CheckYourAnswersId => controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(NormalMode)
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  //noinspection ScalaStyle
  override protected def editRouteMap(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case AreYouInUKId => countryOfRegistrationEditRoutes(ua)
    case IndividualDateOfBirthId => checkYourAnswers
    case IndividualSameContactAddressId => contactAddressRoutes(ua, CheckMode)
    case IndividualContactAddressPostCodeLookupId => IndividualContactAddressListController.onPageLoad(CheckMode)
    case IndividualContactAddressId => IndividualAddressYearsController.onPageLoad(CheckMode)
    case IndividualAddressYearsId => addressYearsRouteCheckMode(ua)
    case IndividualPreviousAddressPostCodeLookupId => IndividualPreviousAddressListController.onPageLoad(CheckMode)
    case IndividualPreviousAddressId => checkYourAnswers
    case IndividualEmailId => checkYourAnswers
    case IndividualPhoneId => checkYourAnswers
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  //noinspection ScalaStyle
  override protected def updateRouteMap(ua: UserAnswers): PartialFunction[Identifier, Call] = {
    case IndividualContactAddressPostCodeLookupId => IndividualContactAddressListController.onPageLoad(UpdateMode)
    case IndividualContactAddressId => IndividualConfirmPreviousAddressController.onPageLoad()
    case IndividualAddressYearsId => addressYearsRoutesUpdateMode(ua)
    case IndividualConfirmPreviousAddressId => confirmPreviousAddressRoutes(ua)
    case IndividualPreviousAddressPostCodeLookupId => IndividualPreviousAddressListController.onPageLoad(UpdateMode)
    case IndividualPreviousAddressId => finishAmendmentNavigation(ua)
    case IndividualEmailId => finishAmendmentNavigation(ua)
    case IndividualPhoneId => finishAmendmentNavigation(ua)
    case _ => controllers.routes.SessionExpiredController.onPageLoad()
  }

  private def checkYourAnswers: Call = CheckYourAnswersController.onPageLoad()

  private def anyMoreChanges: Call = controllers.register.routes.AnyMoreChangesController.onPageLoad()

  private def finishAmendmentNavigation(answers: UserAnswers): Call = {
    answers.get(UpdateContactAddressId) match {
      case Some(_) => updateContactAddressCYAPage()
      case _ => anyMoreChanges
    }
  }

  private def updateContactAddressCYAPage():Call = controllers.routes.UpdateContactAddressCYAController.onPageLoad()

  def detailsCorrect(answers: UserAnswers): Call = {
    answers.get(IndividualDetailsCorrectId) match {
      case Some(true) => IndividualDateOfBirthController.onPageLoad(NormalMode)
      case Some(false) => YouWillNeedToUpdateController.onPageLoad()
      case None => SessionExpiredController.onPageLoad()
    }
  }

  def addressYearsRoutes(answers: UserAnswers): Call =
    (answers.get(IndividualAddressYearsId), answers.get(AreYouInUKId)) match {
      case (_, None) => SessionExpiredController.onPageLoad()
      case (Some(AddressYears.UnderAYear), Some(false)) => IndividualPreviousAddressController.onPageLoad(NormalMode)
      case (Some(AddressYears.UnderAYear), Some(true)) => IndividualPreviousAddressPostCodeLookupController.onPageLoad(NormalMode)
      case (Some(AddressYears.OverAYear), _) => IndividualEmailController.onPageLoad(NormalMode)
      case _ => SessionExpiredController.onPageLoad()
    }


  def addressYearsRouteCheckMode(answers: UserAnswers): Call =
    (answers.get(IndividualAddressYearsId), answers.get(AreYouInUKId)) match {
      case (_, None) => SessionExpiredController.onPageLoad()
      case (Some(AddressYears.UnderAYear), Some(false)) => IndividualPreviousAddressController.onPageLoad(CheckMode)
      case (Some(AddressYears.UnderAYear), Some(true)) => IndividualPreviousAddressPostCodeLookupController.onPageLoad(CheckMode)
      case (Some(AddressYears.OverAYear), _) => CheckYourAnswersController.onPageLoad()
      case _ => SessionExpiredController.onPageLoad()
    }

  def addressYearsRoutesUpdateMode(answers: UserAnswers): Call =
    answers.get(IndividualAddressYearsId) match {
      case Some(AddressYears.UnderAYear) => IndividualConfirmPreviousAddressController.onPageLoad()
      case Some(AddressYears.OverAYear) => anyMoreChanges
      case _ => SessionExpiredController.onPageLoad()
    }

  private def confirmPreviousAddressRoutes(answers: UserAnswers): Call =
    (answers.get(IndividualConfirmPreviousAddressId), answers.get(UpdateContactAddressId)) match {
      case (Some(true),None) => anyMoreChanges
      case (Some(true), Some(_)) => updateContactAddressCYAPage()
      case (Some(false), _) => IndividualPreviousAddressPostCodeLookupController.onPageLoad(UpdateMode)
      case _ => SessionExpiredController.onPageLoad()
    }

  def contactAddressRoutes(answers: UserAnswers, mode: Mode): Call =
    (answers.get(IndividualSameContactAddressId), answers.get(AreYouInUKId)) match {
      case (_, None) => SessionExpiredController.onPageLoad()
      case (Some(false), Some(false)) => IndividualContactAddressController.onPageLoad(mode)
      case (Some(false), Some(true)) => IndividualContactAddressPostCodeLookupController.onPageLoad(mode)
      case (Some(true), _) => contactAddressCompletionBasedNav(answers, mode)
      case _ => SessionExpiredController.onPageLoad()
    }


  private def contactAddressCompletionBasedNav(answers: UserAnswers, mode: Mode): Call =
    answers.get(IndividualContactAddressId) match {
      case None => IndividualContactAddressController.onPageLoad(mode)
      case Some(_) => IndividualAddressYearsController.onPageLoad(mode)
    }


  def countryOfRegistrationRoutes(answers: UserAnswers): Call = {
    answers.get(AreYouInUKId) match {
      case Some(false) => IndividualNameController.onPageLoad(NormalMode)
      case Some(true) => IndividualDetailsCorrectController.onPageLoad(NormalMode)
      case _ => SessionExpiredController.onPageLoad()
    }
  }

  def countryOfRegistrationEditRoutes(answers: UserAnswers): Call = {
    answers.get(AreYouInUKId) match {
      case Some(false) => answers.get(IndividualDetailsId) match {
        case None => IndividualNameController.onPageLoad(NormalMode)
        case _ => IndividualRegisteredAddressController.onPageLoad(NormalMode)
      }
      case Some(true) => IndividualDetailsCorrectController.onPageLoad(NormalMode)
      case _ => SessionExpiredController.onPageLoad()
    }
  }

  def countryBasedDobNavigation(answers: UserAnswers): Call =
    answers.get(AreYouInUKId) match {
      case Some(_) => IndividualSameContactAddressController.onPageLoad(NormalMode)
      case _ => SessionExpiredController.onPageLoad()
    }


  private def regionBasedNavigation(answers: UserAnswers): Call = {
    answers.get(IndividualAddressId).fold(SessionExpiredController.onPageLoad())(address =>
      countryOptions.regions(address.countryOpt.getOrElse("")) match {
        case UK => IndividualAreYouInUKController.onPageLoad(CheckMode)
        case EuEea => IndividualDateOfBirthController.onPageLoad(NormalMode)
        case RestOfTheWorld => OutsideEuEeaController.onPageLoad()
        case _ => SessionExpiredController.onPageLoad()
      }
    )
  }

  def countryBasedContactDetailsNavigation(answers: UserAnswers): Call =
    answers.get(AreYouInUKId) match {
      case Some(false) => checkYourAnswers
      case Some(true) => answers.get(IndividualDateOfBirthId).fold(
        IndividualDateOfBirthController.onPageLoad(NormalMode))(_ =>
        CheckYourAnswersController.onPageLoad())
      case _ => SessionExpiredController.onPageLoad()
    }

}
