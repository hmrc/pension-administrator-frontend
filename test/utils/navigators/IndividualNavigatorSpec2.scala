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
import connectors.FakeDataCacheConnector
import controllers.register.individual.routes
import identifiers.Identifier
import identifiers.register.individual._
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{NavigatorBehaviour2, UserAnswers}

class IndividualNavigatorSpec2 extends SpecBase with NavigatorBehaviour2 {
  import IndividualNavigatorSpec2._

  //Remove the routes and the corresponding tests once the toggle is removed
  def toggledRoute(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",              "User Answers", "Next Page (Normal Mode)",    "Save(NormalMode)", "Next Page (CheckMode)",  "Save(CheckMode"),
    (WhatYouWillNeedId, emptyAnswers,   addressYearsPage(NormalMode), true,               None,                     false)
  )

  //noinspection ScalaStyle
  def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                      "User Answers",               "Next Page (Normal Mode)",              "Save(NormalMode)", "Next Page (CheckMode)",                     "Save(CheckMode"),
    (IndividualDetailsCorrectId,                detailsCorrect,               whatYouWillNeedPage,                    true,               None,                                        false),
    (IndividualDetailsCorrectId,                detailsIncorrect,             youWillNeedToUpdatePage,                false,               None,                                        false),
    (IndividualDetailsCorrectId,                emptyAnswers,                 sessionExpiredPage,                     false,              None,                                        false),
    (WhatYouWillNeedId,                         emptyAnswers,                 sameContactAddressPage(NormalMode),     true,               None,                                        false),
    (IndividualSameContactAddressId,            sameContactAddress,           addressYearsPage(NormalMode),           true,               Some(addressYearsPage(CheckMode)),           true),
    (IndividualSameContactAddressId,            differentContactAddress,      contactPostCodeLookupPage(NormalMode),  true,               Some(contactPostCodeLookupPage(CheckMode)),  true),
    (IndividualSameContactAddressId,            sameContactAddressIncomplete, contactAddressPage(NormalMode),         true,               Some(contactAddressPage(CheckMode)),         true),
    (IndividualContactAddressPostCodeLookupId,  emptyAnswers,                 contactAddressListPage(NormalMode),     false,              Some(contactAddressListPage(CheckMode)),     false),
    (IndividualContactAddressListId,            emptyAnswers,                 contactAddressPage(NormalMode),         true,               Some(contactAddressPage(CheckMode)),         true),
    (IndividualContactAddressId,                emptyAnswers,                 addressYearsPage(NormalMode),           true,               Some(addressYearsPage(CheckMode)),           true),
    (IndividualAddressYearsId,                  addressYearsOverAYear,        contactDetailsPage,                     true,               Some(checkYourAnswersPage),                  true),
    (IndividualAddressYearsId,                  addressYearsUnderAYear,       previousPostCodeLookupPage(NormalMode), true,               Some(previousPostCodeLookupPage(CheckMode)), true),
    (IndividualAddressYearsId,                  emptyAnswers,                 sessionExpiredPage,                     false,              Some(sessionExpiredPage),                    false),
    (IndividualPreviousAddressPostCodeLookupId, emptyAnswers,                 previousAddressListPage(NormalMode),    false,              Some(previousAddressListPage(CheckMode)),    false),
    (IndividualPreviousAddressListId,           emptyAnswers,                 previousAddressPage(NormalMode),        true,               Some(previousAddressPage(CheckMode)),        true),
    (IndividualPreviousAddressId,               emptyAnswers,                 contactDetailsPage,                     true,               Some(checkYourAnswersPage),                  true),
    (IndividualContactDetailsId,                emptyAnswers,                 individualDateOfBirthPage,              true,               Some(checkYourAnswersPage),                  true),
    (IndividualDateOfBirthId,                   emptyAnswers,                 checkYourAnswersPage,                   true,               Some(checkYourAnswersPage),                  true),
    (CheckYourAnswersId,                        emptyAnswers,                 declarationPage,                        true,               None,                                        false)
  )

  val navigatorToggled = new IndividualNavigator2(FakeDataCacheConnector, appConfig())
  s"When contact address journey is toggled off ${navigatorToggled.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigatorToggled, FakeDataCacheConnector, toggledRoute())
  }

  val navigator = new IndividualNavigator2(FakeDataCacheConnector, appConfig(true))
  s"When contact address journey is toggled on ${navigator.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routes())
  }

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routes())
  }
}

object IndividualNavigatorSpec2 extends OptionValues {
  private def appConfig(isContactAddressEnabled: Boolean = false) = {
    val application = new GuiceApplicationBuilder()
      .configure(Configuration("microservice.services.features.contact-address" -> isContactAddressEnabled)).build()
    application.injector.instanceOf[FrontendAppConfig]
  }

  lazy val lastPage: Call = Call("GET", "http://www.test.com")

  lazy private val whatYouWillNeedPage = routes.WhatYouWillNeedController.onPageLoad()
  lazy private val youWillNeedToUpdatePage = routes.YouWillNeedToUpdateController.onPageLoad()
  lazy private val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  lazy private val individualDateOfBirthPage = routes.IndividualDateOfBirthController.onPageLoad(NormalMode)
  lazy private val checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad()
  lazy private val declarationPage = controllers.register.routes.DeclarationController.onPageLoad()
  lazy private val contactDetailsPage = routes.IndividualContactDetailsController.onPageLoad(NormalMode)

  private def addressYearsPage(mode: Mode) = routes.IndividualAddressYearsController.onPageLoad(mode)

  private def contactAddressPage(mode: Mode) = routes.IndividualContactAddressController.onPageLoad(mode)

  private def contactAddressListPage(mode: Mode) = routes.IndividualContactAddressListController.onPageLoad(mode)

  private def contactPostCodeLookupPage(mode: Mode) = routes.IndividualContactAddressPostCodeLookupController.onPageLoad(mode)

  private def sameContactAddressPage(mode: Mode) = routes.IndividualSameContactAddressController.onPageLoad(mode)

  private def previousPostCodeLookupPage(mode: Mode) = routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(mode)

  private def previousAddressListPage(mode: Mode) = routes.IndividualPreviousAddressListController.onPageLoad(mode)

  private def previousAddressPage(mode: Mode) = routes.IndividualPreviousAddressController.onPageLoad(mode)

  val emptyAnswers = UserAnswers(Json.obj())
  private val detailsCorrectNoLastPage = UserAnswers(Json.obj()).set(
    IndividualDetailsCorrectId)(true).asOpt.value
  private val detailsCorrectLastPage =
    UserAnswers()
      .lastPage(LastPage(lastPage.method, lastPage.url))
      .set(IndividualDetailsCorrectId)(true).asOpt.value
  private val detailsCorrect = UserAnswers(Json.obj()).set(
    IndividualDetailsCorrectId)(true).asOpt.value
  private val detailsIncorrect = UserAnswers(Json.obj()).set(
    IndividualDetailsCorrectId)(false).asOpt.value
  private val sameContactAddress = UserAnswers(Json.obj()).set(
    IndividualSameContactAddressId)(true)
    .flatMap(_.set(IndividualContactAddressId)(Address("foo", "bar", None, None, None, "GB")))
    .asOpt.value
  private val sameContactAddressIncomplete = UserAnswers(Json.obj()).set(
    IndividualSameContactAddressId)(true)
    .flatMap(_.set(IndividualContactAddressListId)(TolerantAddress(Some("foo"), None, None, None, None, Some("GB"))))
    .asOpt.value
  private val differentContactAddress = UserAnswers(Json.obj()).set(
    IndividualSameContactAddressId)(false).asOpt.value
  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(IndividualAddressYearsId)(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(IndividualAddressYearsId)(AddressYears.UnderAYear).asOpt.value
}
