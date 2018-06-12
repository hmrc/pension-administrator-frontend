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
import config.FrontendAppConfig
import controllers.register.individual.routes
import identifiers.Identifier
import identifiers.register.individual._
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor4
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class IndividualNavigatorSpec extends SpecBase with NavigatorBehaviour {
  import IndividualNavigatorSpec._

  private def appConfig(isContactAddressEnabled: Boolean = false) = {
    val application = new GuiceApplicationBuilder()
      .configure(Configuration("microservice.services.features.contact-address" -> isContactAddressEnabled))
    application.injector.instanceOf[FrontendAppConfig]
  }
  
  val navigator = new IndividualNavigator(appConfig(false))
  val navigatorToggled = new IndividualNavigator(appConfig(true))
  val navigatorx = new IndividualNavigator(frontendAppConfig)

  private def routes(): TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id",                                    "User Answers",                 "Next Page (Normal Mode)",            "Next Page (Check Mode)"),
    (IndividualDetailsCorrectId,                detailsCorrectNoLastPage,       whatYouWillNeedPage,                  None),
    (IndividualDetailsCorrectId,                detailsCorrectLastPage,         lastPage,                             None),
    (IndividualDetailsCorrectId,                individualDetailsInCorrect,     youWillNeedToUpdatePage,              None),
    (IndividualDetailsCorrectId,                emptyAnswers,                   sessionExpiredPage,                   None),
    (IndividualAddressYearsId,                  addressYearsOverAYear,          contactDetailsPage,                   Some(checkYourAnswersPage)),
    (IndividualAddressYearsId,                  addressYearsUnderAYear,         paPostCodeLookupPage(NormalMode),     Some(paPostCodeLookupPage(CheckMode))),
    (IndividualAddressYearsId,                  emptyAnswers,                   sessionExpiredPage,                   Some(sessionExpiredPage)),
    (IndividualPreviousAddressPostCodeLookupId, emptyAnswers,                   paAddressListPage(NormalMode),        Some(paAddressListPage(CheckMode))),
    (IndividualPreviousAddressListId,           emptyAnswers,                   paAddressPage(NormalMode),            Some(paAddressPage(CheckMode))),
    (IndividualPreviousAddressId,               emptyAnswers,                   contactDetailsPage,                   Some(checkYourAnswersPage)),
    (IndividualContactDetailsId,                emptyAnswers,                   individualDateOfBirthPage,            Some(checkYourAnswersPage)),
    (IndividualDateOfBirthId,                   emptyAnswers,                   checkYourAnswersPage,                 Some(checkYourAnswersPage)),
    (CheckYourAnswersId,                        emptyAnswers,                   declarationPage,                      None)
  )

  private val toggledRoutes: TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id",                                    "User Answers",                 "Next Page (Normal Mode)",            "Next Page (Check Mode)"),
    (IndividualDetailsCorrectId,                individualDetailsCorrect,       individualSameContactAddressPage(NormalMode),           None),
    (IndividualDetailsCorrectId,                individualDetailsInCorrect,     youWillNeedToUpdatePage,              None),
    (IndividualDetailsCorrectId,                emptyAnswers,                   sessionExpiredPage,                   None),
    (IndividualSameContactAddressId,            individualSameContactAddress,   individualAddressYearsPage(NormalMode), Some(individualAddressYearsPage(CheckMode))),
    (IndividualSameContactAddressId,            individualDiffContactAddress,   individualCAPostCodeLookupPage(NormalMode), Some(individualCAPostCodeLookupPage(CheckMode))),
    (IndividualContactAddressPostCodeLookupId,  emptyAnswers,                   individualContactAddressListPage(NormalMode), Some(individualContactAddressListPage(CheckMode))),
    (IndividualContactAddressListId,            emptyAnswers,                   individualContactAddressPage(NormalMode), Some(individualContactAddressPage(CheckMode))),
    (IndividualContactAddressId,                emptyAnswers,                   individualAddressYearsPage(NormalMode), Some(individualAddressYearsPage(CheckMode))),
    (IndividualAddressYearsId,                  addressYearsOverAYear,          contactDetailsPage,                   Some(checkYourAnswersPage)),
    (IndividualAddressYearsId,                  addressYearsUnderAYear,         paPostCodeLookupPage(NormalMode),     Some(paPostCodeLookupPage(CheckMode))),
    (IndividualAddressYearsId,                  emptyAnswers,                   sessionExpiredPage,                   Some(sessionExpiredPage)),
    (IndividualPreviousAddressPostCodeLookupId, emptyAnswers,                   paAddressListPage(NormalMode),        Some(paAddressListPage(CheckMode))),
    (IndividualPreviousAddressListId,           emptyAnswers,                   paAddressPage(NormalMode),            Some(paAddressPage(CheckMode))),
    (IndividualPreviousAddressId,               emptyAnswers,                   contactDetailsPage,                   Some(checkYourAnswersPage)),
    (IndividualContactDetailsId,                emptyAnswers,                   individualDateOfBirthPage,            Some(checkYourAnswersPage)),
    (IndividualDateOfBirthId,                   emptyAnswers,                   checkYourAnswersPage,                 Some(checkYourAnswersPage)),
    (CheckYourAnswersId,                        emptyAnswers,                   declarationPage,                      None)
  )


  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, routes())
  }
}

object IndividualNavigatorSpec extends OptionValues {

  lazy val lastPage: Call = Call("GET", "http://www.test.com")

  lazy val whatYouWillNeedPage = routes.WhatYouWillNeedController.onPageLoad()
  lazy val youWillNeedToUpdatePage = routes.YouWillNeedToUpdateController.onPageLoad()
  lazy val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  lazy val individualDateOfBirthPage = routes.IndividualDateOfBirthController.onPageLoad(NormalMode)
  lazy val individualAddressYearsPage = routes.IndividualAddressYearsController.onPageLoad(NormalMode)
  def individualContactAddressPage(mode: Mode) = routes.IndividualContactAddressController.onPageLoad(mode)
  def individualContactAddressListPage(mode: Mode) = routes.IndividualContactAddressListController.onPageLoad(mode)
  def individualCAPostCodeLookupPage(mode: Mode) = routes.IndividualContactAddressPostCodeLookupController.onPageLoad(mode)
  def individualSameContactAddressPage(mode: Mode) = routes.IndividualSameContactAddressController.onPageLoad(mode)
  def paPostCodeLookupPage(mode: Mode) = routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(mode)
  lazy val contactDetailsPage = routes.IndividualContactDetailsController.onPageLoad(NormalMode)
  def paAddressListPage(mode: Mode) = routes.IndividualPreviousAddressListController.onPageLoad(mode)
  def paAddressPage(mode: Mode) = routes.IndividualPreviousAddressController.onPageLoad(mode)
  lazy val checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad()
  lazy val declarationPage = controllers.register.routes.DeclarationController.onPageLoad()

  val emptyAnswers = new UserAnswers(Json.obj())
  val detailsCorrectNoLastPage = UserAnswers(Json.obj()).set(
    IndividualDetailsCorrectId)(true).asOpt.value
  val detailsCorrectLastPage =
    UserAnswers()
    .lastPage(LastPage(lastPage.method, lastPage.url))
    .set(IndividualDetailsCorrectId)(true).asOpt.value
  val individualDetailsInCorrect = UserAnswers(Json.obj()).set(
    IndividualDetailsCorrectId)(false).asOpt.value
  val individualSameContactAddress = UserAnswers(Json.obj()).set(
    IndividualSameContactAddressId)(true)
    .flatMap(_.set(IndividualContactAddressId)(Address("foo", "bar", None, None, None, "GB")))
    .asOpt.value

  val individualDiffContactAddress = UserAnswers(Json.obj()).set(
    IndividualSameContactAddressId)(false).asOpt.value
  val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(IndividualAddressYearsId)(AddressYears.OverAYear).asOpt.value
  val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(IndividualAddressYearsId)(AddressYears.UnderAYear).asOpt.value
}
