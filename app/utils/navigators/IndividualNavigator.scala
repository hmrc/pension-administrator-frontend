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
import identifiers.Identifier
import identifiers.register.individual._
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}
import controllers.register.individual.routes
import models.{AddressYears, CheckMode, NormalMode}

@Singleton
class IndividualNavigator @Inject()(config: FrontendAppConfig) extends Navigator {

  private def checkYourAnswers()(answers: UserAnswers): Call =
    routes.CheckYourAnswersController.onPageLoad()

  override protected def routeMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case IndividualDetailsCorrectId => detailsCorrect
    case IndividualSameContactAddressId => contactAddressRoutes
    case IndividualContactAddressPostCodeLookupId => _ => routes.IndividualContactAddressListController.onPageLoad(NormalMode)
    case IndividualContactAddressListId => _ => routes.IndividualContactAddressController.onPageLoad(NormalMode)
    case IndividualContactAddressId => _ => routes.IndividualAddressYearsController.onPageLoad(NormalMode)
    case IndividualAddressYearsId => addressYearsRoutes
    case IndividualPreviousAddressPostCodeLookupId => _ => routes.IndividualPreviousAddressListController.onPageLoad(NormalMode)
    case IndividualPreviousAddressListId => _ => routes.IndividualPreviousAddressController.onPageLoad(NormalMode)
    case IndividualPreviousAddressId => _ => routes.IndividualContactDetailsController.onPageLoad(NormalMode)
    case IndividualContactDetailsId => _ => routes.IndividualDateOfBirthController.onPageLoad(NormalMode)
    case IndividualDateOfBirthId => checkYourAnswers()
    case CheckYourAnswersId => _ => controllers.register.routes.DeclarationController.onPageLoad()
  }

  override protected def editRouteMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case IndividualDateOfBirthId => checkYourAnswers()
    case IndividualSameContactAddressId => contactAddressRoutesCheckMode
    case IndividualContactAddressPostCodeLookupId => _ => routes.IndividualContactAddressListController.onPageLoad(CheckMode)
    case IndividualContactAddressListId => _ => routes.IndividualContactAddressController.onPageLoad(CheckMode)
    case IndividualContactAddressId => _ => routes.IndividualAddressYearsController.onPageLoad(CheckMode)
    case IndividualAddressYearsId => addressYearsRouteCheckMode
    case IndividualPreviousAddressPostCodeLookupId => _ => routes.IndividualPreviousAddressListController.onPageLoad(CheckMode)
    case IndividualPreviousAddressListId => _ => routes.IndividualPreviousAddressController.onPageLoad(CheckMode)
    case IndividualPreviousAddressId => checkYourAnswers()
    case IndividualContactDetailsId => checkYourAnswers()
  }

  def detailsCorrect(answers: UserAnswers): Call = {
    answers.get(IndividualDetailsCorrectId) match {
      case Some(true) =>
        if (config.contactAddressEnabled) {
          routes.IndividualSameContactAddressController.onPageLoad(NormalMode)
        } else {
          routes.IndividualAddressYearsController.onPageLoad(NormalMode)
        }
      case Some(false) =>
        routes.YouWillNeedToUpdateController.onPageLoad()
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  def addressYearsRoutes(answers: UserAnswers): Call = {
    answers.get(IndividualAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(NormalMode)
      case Some(AddressYears.OverAYear) =>
        routes.IndividualContactDetailsController.onPageLoad(NormalMode)
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  def addressYearsRouteCheckMode(answers: UserAnswers): Call = {
    answers.get(IndividualAddressYearsId) match {
      case Some(AddressYears.UnderAYear) =>
        routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(CheckMode)
      case Some(AddressYears.OverAYear) =>
        routes.CheckYourAnswersController.onPageLoad()
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  def contactAddressRoutes(answers: UserAnswers): Call = {
    answers.get(IndividualSameContactAddressId) match {
      case Some(false) =>
        routes.IndividualContactAddressPostCodeLookupController.onPageLoad(NormalMode)
      case Some(true) =>
        answers.get(IndividualContactAddressId) match {
          case None =>
            routes.IndividualContactAddressController.onPageLoad(NormalMode)
          case Some(_) =>
            routes.IndividualAddressYearsController.onPageLoad(NormalMode)
        }
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

  def contactAddressRoutesCheckMode(answers: UserAnswers): Call = {
    answers.get(IndividualSameContactAddressId) match {
      case Some(false) =>
        routes.IndividualContactAddressPostCodeLookupController.onPageLoad(CheckMode)
      case Some(true) =>
        answers.get(IndividualContactAddressId) match {
          case None =>
            routes.IndividualContactAddressController.onPageLoad(CheckMode)
          case Some(_) =>
            routes.IndividualAddressYearsController.onPageLoad(CheckMode)
        }
      case None =>
        controllers.routes.SessionExpiredController.onPageLoad()
    }
  }

}
