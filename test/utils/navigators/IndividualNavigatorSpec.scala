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

import base.SpecBase
import controllers.register.individual.routes
import identifiers.Identifier
import identifiers.register.individual._
import models.{AddressYears, CheckMode, Mode, NormalMode}
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor4
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class IndividualNavigatorSpec extends SpecBase with NavigatorBehaviour {
  import IndividualNavigatorSpec._

  val navigator = new IndividualNavigator()

  private val routes: TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id",                                    "User Answers",                 "Next Page (Normal Mode)",            "Next Page (Check Mode)"),
    (IndividualDetailsCorrectId,                individualDetailsCorrect,       whatYouWillNeedPage,                  None),
    (IndividualDetailsCorrectId,                individualDetailsInCorrect,     youWillNeedToUpdatePage,              None),
    (IndividualDetailsCorrectId,                emptyAnswers,                   sessionExpiredPage,                   None),
    (WhatYouWillNeedId,                         emptyAnswers,                   individualAddressYearsPage,           None),
    (IndividualAddressYearsId,                  addressYearsOverAYear,          contactDetailsPage,                   Some(checkYourAnswersPage)),
    (IndividualAddressYearsId,                  addressYearsUnderAYear,         paPostCodeLookupPage(NormalMode),     Some(paPostCodeLookupPage(CheckMode))),
    (IndividualAddressYearsId,                  emptyAnswers,                   sessionExpiredPage,                   Some(sessionExpiredPage)),
    (IndividualPreviousAddressPostCodeLookupId, emptyAnswers,                   paAddressListPage(NormalMode),        Some(paAddressListPage(CheckMode))),
    (IndividualPreviousAddressListId,           emptyAnswers,                   paAddressPage(NormalMode),            Some(paAddressPage(CheckMode))),
    (IndividualPreviousAddressId,               emptyAnswers,                   contactDetailsPage,                   Some(checkYourAnswersPage)),
    (IndividualContactDetailsId,                emptyAnswers,                   checkYourAnswersPage,                 Some(checkYourAnswersPage)),
    (CheckYourAnswersId,                        emptyAnswers,                   declarationPage,                      None)
  )

  navigator.getClass.getSimpleName must {
    behave like navigatorWithRoutes(navigator, routes)
  }
}

object IndividualNavigatorSpec extends OptionValues {
  val whatYouWillNeedPage = routes.WhatYouWillNeedController.onPageLoad()
  val youWillNeedToUpdatePage = routes.YouWillNeedToUpdateController.onPageLoad()
  val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  val individualAddressYearsPage = routes.IndividualAddressYearsController.onPageLoad(NormalMode)
  def paPostCodeLookupPage(mode: Mode) = routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(mode)
  val contactDetailsPage = routes.IndividualContactDetailsController.onPageLoad(NormalMode)
  def paAddressListPage(mode: Mode) = routes.IndividualPreviousAddressListController.onPageLoad(mode)
  def paAddressPage(mode: Mode) = routes.IndividualPreviousAddressController.onPageLoad(mode)
  val checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad()
  val declarationPage = controllers.register.routes.DeclarationController.onPageLoad()

  val emptyAnswers = new UserAnswers(Json.obj())
  val individualDetailsCorrect = UserAnswers(Json.obj()).set(
    IndividualDetailsCorrectId)(true).asOpt.value
  val individualDetailsInCorrect = UserAnswers(Json.obj()).set(
    IndividualDetailsCorrectId)(false).asOpt.value

  val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(IndividualAddressYearsId)(AddressYears.OverAYear).asOpt.value
  val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(IndividualAddressYearsId)(AddressYears.UnderAYear).asOpt.value
}
