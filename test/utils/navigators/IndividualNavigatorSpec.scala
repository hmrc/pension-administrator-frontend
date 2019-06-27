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

import base.SpecBase
import connectors.FakeUserAnswersCacheConnector
import controllers.register.individual.routes
import identifiers.Identifier
import identifiers.register.AreYouInUKId
import identifiers.register.individual._
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, NavigatorBehaviour, UserAnswers}

class IndividualNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import IndividualNavigatorSpec._

  //noinspection ScalaStyle
  def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save(NormalMode)", "Next Page (CheckMode)", "Save(CheckMode"),

    (AreYouInUKId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),
    (AreYouInUKId, uk, ukIndividualDetailsPage, false, None, false),
    (AreYouInUKId, nonUk, nonUkIndividualNamePage, false, Some(nonUkIndividualAddressPage), false),
    (AreYouInUKId, nonUkNoIndividualDetails, nonUkIndividualNamePage, false, Some(nonUkIndividualNamePage), false),

    (IndividualDetailsCorrectId, detailsCorrect, whatYouWillNeedPage, false, None, false),
    (IndividualDetailsCorrectId, detailsIncorrect, youWillNeedToUpdatePage, false, None, false),
    (IndividualDetailsCorrectId, lastPage, whatYouWillNeedPage, false, None, false),
    (IndividualDetailsCorrectId, emptyAnswers, sessionExpiredPage, false, None, false),

    (IndividualDetailsId, emptyAnswers, nonUkIndividualAddressPage, false, None, false),

    (IndividualAddressId, nonUkEuAddress, individualDateOfBirthPage, false, None, false),
    (IndividualAddressId, nonUkButUKAddress, reconsiderAreYouInUk, false, None, false),
    (IndividualAddressId, nonUkNonEuAddress, outsideEuEea, false, None, false),

    (WhatYouWillNeedId, emptyAnswers, sameContactAddressPage(NormalMode), true, None, false),

    (IndividualDateOfBirthId, emptyAnswers, sessionExpiredPage, false, Some(checkYourAnswersPage), true),
    (IndividualDateOfBirthId, uk, checkYourAnswersPage, false, None, false),
    (IndividualDateOfBirthId, nonUk, whatYouWillNeedPage, false, Some(checkYourAnswersPage), true),
    (IndividualSameContactAddressId, sameContactAddressUk, addressYearsPage(NormalMode), true, Some(addressYearsPage(CheckMode)), true),

    (IndividualSameContactAddressId, sameContactAddressNonUk, addressYearsPage(NormalMode), true, Some(addressYearsPage(CheckMode)), true),
    (IndividualSameContactAddressId, ukDifferentContactAddress, contactPostCodeLookupPage(NormalMode), true, Some(contactPostCodeLookupPage(CheckMode)), true),
    (IndividualSameContactAddressId, nonUkDifferentContactAddress, contactAddressPage(NormalMode), true, Some(contactAddressPage(CheckMode)), true),
    (IndividualSameContactAddressId, sameContactAddressIncompleteUk, contactAddressPage(NormalMode), true, Some(contactAddressPage(CheckMode)), true),
    (IndividualSameContactAddressId, sameContactAddressIncompleteNonUk, contactAddressPage(NormalMode), true, Some(contactAddressPage(CheckMode)), true),

    (IndividualContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(NormalMode), false, Some(contactAddressListPage(CheckMode)), false),
    (IndividualContactAddressListId, emptyAnswers, contactAddressPage(NormalMode), true, Some(contactAddressPage(CheckMode)), true),
    (IndividualContactAddressId, emptyAnswers, addressYearsPage(NormalMode), true, Some(addressYearsPage(CheckMode)), true),

    (IndividualAddressYearsId, ukAddressYearsOverAYear, contactDetailsPage, true, Some(checkYourAnswersPage), true),
    (IndividualAddressYearsId, nonUkAddressYearsOverAYear, contactDetailsPage, true, Some(checkYourAnswersPage), true),
    (IndividualAddressYearsId, ukAddressYearsUnderAYear, previousPostCodeLookupPage(NormalMode), true, Some(previousPostCodeLookupPage(CheckMode)), true),
    (IndividualAddressYearsId, nonUkAddressYearsUnderAYear, previousAddressPage(NormalMode), true, Some(previousAddressPage(CheckMode)), true),
    (IndividualAddressYearsId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),

    (IndividualPreviousAddressPostCodeLookupId, emptyAnswers, previousAddressListPage(NormalMode), false, Some(previousAddressListPage(CheckMode)), false),
    (IndividualPreviousAddressListId, emptyAnswers, previousAddressPage(NormalMode), true, Some(previousAddressPage(CheckMode)), true),
    (IndividualPreviousAddressId, emptyAnswers, contactDetailsPage, true, Some(checkYourAnswersPage), true),

    (IndividualContactDetailsId, nonUk, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),
    (CheckYourAnswersId, emptyAnswers, declarationPage, true, None, false)
  )

  def updateRoutes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save(NormalMode)", "Next Page (CheckMode)", "Save(CheckMode"),
    (IndividualContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(UpdateMode), false, None, false),
    (IndividualContactAddressListId, emptyAnswers, contactAddressPage(UpdateMode), true, None, true),
    (IndividualContactAddressId, emptyAnswers, addressYearsPage(UpdateMode), true, None, true),
    (IndividualAddressYearsId, ukAddressYearsOverAYear, anyMoreChanges, false, None, false),
    (IndividualAddressYearsId, ukAddressYearsUnderAYear, confirmPreviousAddress, true, None, true),
    (IndividualAddressYearsId, emptyAnswers, sessionExpiredPage, false, None, false),
    (IndividualConfirmPreviousAddressId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),
    (IndividualConfirmPreviousAddressId, samePreviousAddress, anyMoreChanges, false, None, false),
    (IndividualConfirmPreviousAddressId, notSamePreviousAddress, previousAddressPage(UpdateMode), false, None, false),
    (IndividualPreviousAddressPostCodeLookupId, emptyAnswers, previousAddressListPage(UpdateMode), false, None, false),
    (IndividualPreviousAddressListId, emptyAnswers, previousAddressPage(UpdateMode), true, None, true),
    (IndividualPreviousAddressId, emptyAnswers, anyMoreChanges, false, None, true),
    (IndividualContactDetailsId, nonUk, anyMoreChanges, false, None, true),
    (invalidIdForNavigator, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), true)
  )


  def countryOptions: CountryOptions = new FakeCountryOptions(environment, appConfig(isHubEnabled = false))

  val navigator = new IndividualNavigator(FakeUserAnswersCacheConnector, appConfig(isHubEnabled = false), countryOptions)

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes(), dataDescriber, UpdateMode)
  }
}

object IndividualNavigatorSpec extends OptionValues {

  private lazy val invalidIdForNavigator = AreYouInUKId

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
  lazy private val nonUkIndividualAddressPage = routes.IndividualRegisteredAddressController.onPageLoad(NormalMode)
  lazy private val reconsiderAreYouInUk = routes.IndividualAreYouInUKController.onPageLoad(CheckMode)
  lazy private val outsideEuEea = routes.OutsideEuEeaController.onPageLoad()
  lazy private val anyMoreChanges = controllers.register.routes.AnyMoreChangesController.onPageLoad()
  lazy private val confirmPreviousAddress = routes.IndividualConfirmPreviousAddressController.onPageLoad()

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

  private def sameContactAddress(areYouInUk: Boolean) = UserAnswers(Json.obj()).areYouInUk(areYouInUk).set(
    IndividualSameContactAddressId)(true)
    .flatMap(_.set(IndividualContactAddressId)(Address("foo", "bar", None, None, None, "GB")))
    .asOpt.value

  private val sameContactAddressUk = sameContactAddress(areYouInUk = true)
  private val sameContactAddressNonUk = sameContactAddress(areYouInUk = false)

  private def sameContactAddressIncomplete(areYouInUk: Boolean) = UserAnswers(Json.obj()).areYouInUk(areYouInUk).
    set(
      IndividualSameContactAddressId)(true)
    .flatMap(_.set(IndividualContactAddressListId)(TolerantAddress(Some("foo"), None, None, None, None, Some("GB"))))
    .asOpt.value

  private val sameContactAddressIncompleteUk = sameContactAddressIncomplete(areYouInUk = true)
  private val sameContactAddressIncompleteNonUk = sameContactAddressIncomplete(areYouInUk = false)

  private val ukDifferentContactAddress = UserAnswers(Json.obj()).areYouInUk(true).set(
    IndividualSameContactAddressId)(false).asOpt.value
  private val nonUkDifferentContactAddress = UserAnswers(Json.obj()).areYouInUk(false).set(
    IndividualSameContactAddressId)(false).asOpt.value

  private val nonUkAddressYearsOverAYear = UserAnswers(Json.obj()).areYouInUk(false)
    .set(IndividualAddressYearsId)(AddressYears.OverAYear).asOpt.value
  private val ukAddressYearsOverAYear = UserAnswers(Json.obj()).areYouInUk(true)
    .set(IndividualAddressYearsId)(AddressYears.OverAYear).asOpt.value

  private val ukAddressYearsUnderAYear = UserAnswers(Json.obj()).areYouInUk(true)
    .set(IndividualAddressYearsId)(AddressYears.UnderAYear).asOpt.value
  private val nonUkAddressYearsUnderAYear = UserAnswers(Json.obj()).areYouInUk(false)
    .set(IndividualAddressYearsId)(AddressYears.UnderAYear).asOpt.value

  private val uk = UserAnswers(Json.obj())
    .set(AreYouInUKId)(true).asOpt.value
  private val nonUk = UserAnswers(Json.obj())
    .set(AreYouInUKId)(false).asOpt.value
    .set(IndividualDetailsId)(TolerantIndividual(Some("first"), None, Some("last"))).asOpt.value

  private val nonUkNoIndividualDetails = UserAnswers(Json.obj())
    .set(AreYouInUKId)(false).asOpt.value

  private val nonUkEuAddress = UserAnswers().nonUkIndividualAddress(address("AT"))
  private val nonUkButUKAddress = UserAnswers().nonUkIndividualAddress(address("GB"))
  private val nonUkNonEuAddress = UserAnswers().nonUkIndividualAddress(address("AF"))

  private def address(countryCode: String) = Address("addressLine1", "addressLine2", Some("addressLine3"), Some("addressLine4"), Some("NE11AA"), countryCode)

  private def samePreviousAddress = UserAnswers(Json.obj())
    .set(IndividualConfirmPreviousAddressId)(true).asOpt.value

  private def notSamePreviousAddress = UserAnswers(Json.obj())
    .set(IndividualConfirmPreviousAddressId)(false).asOpt.value

}
