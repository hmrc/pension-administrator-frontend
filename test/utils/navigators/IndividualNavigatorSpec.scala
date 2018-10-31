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
import connectors.FakeUserAnswersCacheConnector
import controllers.register.individual.routes
import identifiers.Identifier
import identifiers.register.AreYouInUKId
import identifiers.register.individual._
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, NavigatorBehaviour, UserAnswers}

class IndividualNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import IndividualNavigatorSpec._

  //noinspection ScalaStyle
  def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save(NormalMode)", "Next Page (CheckMode)", "Save(CheckMode"),
    (IndividualDetailsCorrectId, detailsCorrect, whatYouWillNeedPage, false, None, false),
    (IndividualDetailsCorrectId, detailsIncorrect, youWillNeedToUpdatePage, false, None, false),
    (IndividualDetailsCorrectId, lastPage, whatYouWillNeedPage, false, None, false),
    (IndividualDetailsCorrectId, emptyAnswers, sessionExpiredPage, false, None, false),
    (WhatYouWillNeedId, emptyAnswers, sameContactAddressPage(NormalMode), true, None, false),
    (IndividualSameContactAddressId, sameContactAddress, addressYearsPage(NormalMode), true, Some(addressYearsPage(CheckMode)), true),
    (IndividualSameContactAddressId, differentContactAddress, contactPostCodeLookupPage(NormalMode), true, Some(contactPostCodeLookupPage(CheckMode)), true),
    (IndividualSameContactAddressId, sameContactAddressIncomplete, contactAddressPage(NormalMode), true, Some(contactAddressPage(CheckMode)), true),
    (IndividualContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(NormalMode), false, Some(contactAddressListPage(CheckMode)), false),
    (IndividualContactAddressListId, emptyAnswers, contactAddressPage(NormalMode), true, Some(contactAddressPage(CheckMode)), true),
    (IndividualContactAddressId, emptyAnswers, addressYearsPage(NormalMode), true, Some(addressYearsPage(CheckMode)), true),
    (IndividualAddressYearsId, addressYearsOverAYear, contactDetailsPage, true, Some(checkYourAnswersPage), true),
    (IndividualAddressYearsId, addressYearsUnderAYear, previousPostCodeLookupPage(NormalMode), true, Some(previousPostCodeLookupPage(CheckMode)), true),
    (IndividualAddressYearsId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),
    (IndividualPreviousAddressPostCodeLookupId, emptyAnswers, previousAddressListPage(NormalMode), false, Some(previousAddressListPage(CheckMode)), false),
    (IndividualPreviousAddressListId, emptyAnswers, previousAddressPage(NormalMode), true, Some(previousAddressPage(CheckMode)), true),
    (IndividualPreviousAddressId, emptyAnswers, contactDetailsPage, true, Some(checkYourAnswersPage), true),
    (IndividualContactDetailsId, emptyAnswers, individualDateOfBirthPage, false, Some(checkYourAnswersPage), true),
    (IndividualDateOfBirthId, emptyAnswers, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),
    (CheckYourAnswersId, emptyAnswers, declarationPage, true, None, false)
  )

  def nonUkroutes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save(NormalMode)", "Next Page (CheckMode)", "Save(CheckMode"),

    (AreYouInUKId, uk, ukIndividualDetailsPage, false, None, false),
    (AreYouInUKId, nonUk, nonUkIndividualNamePage, false, Some(nonUkIndividualAddressPage), false),
    (IndividualDetailsId, emptyAnswers, individualDateOfBirthPage, false, None, false),
    (IndividualDateOfBirthId, nonUk, nonUkIndividualAddressPage, false, Some(checkYourAnswersPage), true),
    (IndividualAddressId, nonUkEuAddress, whatYouWillNeedPage, false, None, false),
    (IndividualAddressId, nonUkButUKAddress, reconsiderAreYouInUk, false, None, false),
    (IndividualAddressId, nonUkNonEuAddress, outsideEuEea, false, None, false),
    (IndividualSameContactAddressId, sameContactAddressUk, addressYearsPage(NormalMode), true, Some(addressYearsPage(CheckMode)), true),
    (IndividualSameContactAddressId, sameContactAddressNonUk, addressYearsPage(NormalMode), true, Some(addressYearsPage(CheckMode)), true),
    (IndividualSameContactAddressId, ukDifferentContactAddress, contactPostCodeLookupPage(NormalMode), true, Some(contactPostCodeLookupPage(CheckMode)), true),
    (IndividualSameContactAddressId, nonUkDifferentContactAddress, contactAddressPage(NormalMode), true, Some(contactAddressPage(CheckMode)), true),
    (IndividualSameContactAddressId, sameContactAddressIncompleteUk, contactAddressPage(NormalMode), true, Some(contactAddressPage(CheckMode)), true),
    (IndividualSameContactAddressId, sameContactAddressIncompleteNonUk, contactAddressPage(NormalMode), true, Some(contactAddressPage(CheckMode)), true),
    (IndividualAddressYearsId, ukAddressYearsOverAYear, contactDetailsPage, true, Some(checkYourAnswersPage), true),
    (IndividualAddressYearsId, nonUkAddressYearsOverAYear, contactDetailsPage, true, Some(checkYourAnswersPage), true),
    (IndividualAddressYearsId, ukAddressYearsUnderAYear, previousPostCodeLookupPage(NormalMode), true, Some(previousPostCodeLookupPage(CheckMode)), true),
    (IndividualAddressYearsId, nonUkAddressYearsUnderAYear, previousAddressPage(NormalMode), true, Some(previousAddressPage(CheckMode)), true),
    (IndividualAddressYearsId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),
    (IndividualContactDetailsId, nonUk, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),
    (CheckYourAnswersId, emptyAnswers, declarationPage, true, None, false)
  )

  def countryOptions: CountryOptions = new FakeCountryOptions(environment, appConfig())
  val navigatorUk = new IndividualNavigator(FakeUserAnswersCacheConnector, appConfig(false), countryOptions)
  val navigatorNonUk = new IndividualNavigator(FakeUserAnswersCacheConnector, appConfig(true), countryOptions)

  navigatorUk.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigatorUk)
    behave like navigatorWithRoutes(navigatorUk, FakeUserAnswersCacheConnector, routes(), dataDescriber)
  }

  s"NonUK ${navigatorNonUk.getClass.getSimpleName}" must {
    appRunning()
    behave like navigatorWithRoutes(navigatorNonUk, FakeUserAnswersCacheConnector, nonUkroutes(), dataDescriber)
  }
}

object IndividualNavigatorSpec extends OptionValues {
  private def appConfig(nonUk: Boolean = false) = new GuiceApplicationBuilder().configure(
    "features.non-uk-journeys" -> nonUk
  ).build().injector.instanceOf[FrontendAppConfig]

  lazy val lastPageCall: Call = Call("GET", "http://www.test.com")

  lazy private val whatYouWillNeedPage = routes.WhatYouWillNeedController.onPageLoad()
  lazy private val youWillNeedToUpdatePage = routes.YouWillNeedToUpdateController.onPageLoad()
  lazy private val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  lazy private val individualDateOfBirthPage = routes.IndividualDateOfBirthController.onPageLoad(NormalMode)
  lazy private val checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad()
  lazy private val declarationPage = controllers.register.routes.DeclarationController.onPageLoad()
  lazy private val contactDetailsPage = routes.IndividualContactDetailsController.onPageLoad(NormalMode)
  lazy private val ukIndividualDetailsPage = routes.IndividualDetailsCorrectController.onPageLoad(NormalMode)
  lazy private val nonUkIndividualNamePage = routes.IndividualNameController.onPageLoad(NormalMode)
  lazy private val nonUkIndividualAddressPage = routes.IndividualRegisteredAddressController.onPageLoad()
  lazy private val reconsiderAreYouInUk = routes.IndividualAreYouInUKController.onPageLoad(CheckMode)
  lazy private val outsideEuEea = routes.OutsideEuEeaController.onPageLoad()

  private def addressYearsPage(mode: Mode) = routes.IndividualAddressYearsController.onPageLoad(mode)

  private def contactAddressPage(mode: Mode) = routes.IndividualContactAddressController.onPageLoad(mode)

  private def contactAddressListPage(mode: Mode) = routes.IndividualContactAddressListController.onPageLoad(mode)

  private def contactPostCodeLookupPage(mode: Mode) = routes.IndividualContactAddressPostCodeLookupController.onPageLoad(mode)

  private def sameContactAddressPage(mode: Mode) = routes.IndividualSameContactAddressController.onPageLoad(mode)

  private def previousPostCodeLookupPage(mode: Mode) = routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(mode)

  private def previousAddressListPage(mode: Mode) = routes.IndividualPreviousAddressListController.onPageLoad(mode)

  private def previousAddressPage(mode: Mode) = routes.IndividualPreviousAddressController.onPageLoad(mode)

  val emptyAnswers = UserAnswers(Json.obj())
  private val detailsCorrect = UserAnswers(Json.obj()).set(
    IndividualDetailsCorrectId)(true).asOpt.value
  private val detailsIncorrect = UserAnswers(Json.obj()).set(
    IndividualDetailsCorrectId)(false).asOpt.value
  private lazy val lastPage =
    detailsCorrect.lastPage(LastPage(lastPageCall.method, lastPageCall.url))

  private val sameContactAddress = UserAnswers(Json.obj()).set(
    IndividualSameContactAddressId)(true)
    .flatMap(_.set(IndividualContactAddressId)(Address("foo", "bar", None, None, None, "GB")))
    .asOpt.value

  private val sameContactAddressUk = sameContactAddress.areYouInUk(true)
  private val sameContactAddressNonUk = sameContactAddress.areYouInUk(false)

  private val sameContactAddressIncomplete = UserAnswers(Json.obj()).set(
    IndividualSameContactAddressId)(true)
    .flatMap(_.set(IndividualContactAddressListId)(TolerantAddress(Some("foo"), None, None, None, None, Some("GB"))))
    .asOpt.value

  private val sameContactAddressIncompleteUk = sameContactAddressIncomplete.areYouInUk(true)
  private val sameContactAddressIncompleteNonUk = sameContactAddressIncomplete.areYouInUk(false)

  private val differentContactAddress = UserAnswers(Json.obj()).set(
    IndividualSameContactAddressId)(false).asOpt.value
  private val ukDifferentContactAddress = UserAnswers(Json.obj()).set(
    IndividualSameContactAddressId)(false).asOpt.value.areYouInUk(true)
  private val nonUkDifferentContactAddress = UserAnswers(Json.obj()).set(
    IndividualSameContactAddressId)(false).asOpt.value.areYouInUk(false)
  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(IndividualAddressYearsId)(AddressYears.OverAYear).asOpt.value
  private val nonUkAddressYearsOverAYear = addressYearsOverAYear.areYouInUk(false)
  private val ukAddressYearsOverAYear = addressYearsOverAYear.areYouInUk(true)
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(IndividualAddressYearsId)(AddressYears.UnderAYear).asOpt.value
  private val ukAddressYearsUnderAYear = UserAnswers(Json.obj())
    .set(IndividualAddressYearsId)(AddressYears.UnderAYear).asOpt.value.areYouInUk(true)
  private val nonUkAddressYearsUnderAYear = UserAnswers(Json.obj())
    .set(IndividualAddressYearsId)(AddressYears.UnderAYear).asOpt.value.areYouInUk(false)

  private val uk = UserAnswers(Json.obj())
    .set(AreYouInUKId)(true).asOpt.value
  private val nonUk = UserAnswers(Json.obj())
    .set(AreYouInUKId)(false).asOpt.value

  private val nonUkEuAddress = UserAnswers().nonUkIndividualAddress(address("AT"))
  private val nonUkButUKAddress = UserAnswers().nonUkIndividualAddress(address("GB"))
  private val nonUkNonEuAddress = UserAnswers().nonUkIndividualAddress(address("AF"))

  private def address(countryCode: String) =Address("addressLine1","addressLine2", Some("addressLine3"), Some("addressLine4"), Some("NE11AA"), countryCode)


}
