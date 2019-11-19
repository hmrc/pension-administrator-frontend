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
import controllers.register.individual.routes
import identifiers.Identifier
import identifiers.register.AreYouInUKId
import identifiers.register.individual._
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor4
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, NavigatorBehaviour, UserAnswers}

class IndividualNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import IndividualNavigatorSpec._

  //noinspection ScalaStyle
  def routes(): TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Next Page (CheckMode)"),

    (AreYouInUKId, emptyAnswers, sessionExpiredPage, Some(sessionExpiredPage)),
    (AreYouInUKId, uk, ukIndividualDetailsPage, None),
    (AreYouInUKId, nonUk, nonUkIndividualNamePage, Some(nonUkIndividualAddressPage)),
    (AreYouInUKId, nonUkNoIndividualDetails, nonUkIndividualNamePage, Some(nonUkIndividualNamePage)),

    (IndividualDetailsCorrectId, detailsCorrect, individualDateOfBirthPage, None),
    (IndividualDetailsCorrectId, detailsIncorrect, youWillNeedToUpdatePage, None),
    (IndividualDetailsCorrectId, emptyAnswers, sessionExpiredPage, None),

    (IndividualDetailsId, emptyAnswers, nonUkIndividualAddressPage, None),

    (IndividualAddressId, nonUkEuAddress, individualDateOfBirthPage, None),
    (IndividualAddressId, nonUkButUKAddress, reconsiderAreYouInUk(CheckMode), None),
    (IndividualAddressId, nonUkNonEuAddress, outsideEuEea, None),

    (WhatYouWillNeedId, emptyAnswers, reconsiderAreYouInUk(NormalMode), None),

    (IndividualDateOfBirthId, emptyAnswers, sessionExpiredPage, Some(checkYourAnswersPage)),
    (IndividualDateOfBirthId, uk, sameContactAddressPage(NormalMode), None),
    (IndividualDateOfBirthId, nonUk, sameContactAddressPage(NormalMode), Some(checkYourAnswersPage)),
    (IndividualSameContactAddressId, sameContactAddressUk, addressYearsPage(NormalMode), Some(addressYearsPage(CheckMode))),

    (IndividualSameContactAddressId, sameContactAddressNonUk, addressYearsPage(NormalMode), Some(addressYearsPage(CheckMode))),
    (IndividualSameContactAddressId, ukDifferentContactAddress, contactPostCodeLookupPage(NormalMode), Some(contactPostCodeLookupPage(CheckMode))),
    (IndividualSameContactAddressId, nonUkDifferentContactAddress, contactAddressPage(NormalMode), Some(contactAddressPage(CheckMode))),
    (IndividualSameContactAddressId, sameContactAddressIncompleteUk, contactAddressPage(NormalMode), Some(contactAddressPage(CheckMode))),
    (IndividualSameContactAddressId, sameContactAddressIncompleteNonUk, contactAddressPage(NormalMode), Some(contactAddressPage(CheckMode))),

    (IndividualContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(NormalMode), Some(contactAddressListPage(CheckMode))),
    (IndividualContactAddressListId, emptyAnswers, contactAddressPage(NormalMode), Some(contactAddressPage(CheckMode))),
    (IndividualContactAddressId, emptyAnswers, addressYearsPage(NormalMode), Some(addressYearsPage(CheckMode))),

    (IndividualAddressYearsId, ukAddressYearsOverAYear, emailPage(NormalMode), Some(checkYourAnswersPage)),
    (IndividualAddressYearsId, nonUkAddressYearsOverAYear, emailPage(NormalMode), Some(checkYourAnswersPage)),
    (IndividualAddressYearsId, ukAddressYearsUnderAYear, previousPostCodeLookupPage(NormalMode), Some(previousPostCodeLookupPage(CheckMode))),
    (IndividualAddressYearsId, nonUkAddressYearsUnderAYear, previousAddressPage(NormalMode), Some(previousAddressPage(CheckMode))),
    (IndividualAddressYearsId, emptyAnswers, sessionExpiredPage, Some(sessionExpiredPage)),

    (IndividualPreviousAddressPostCodeLookupId, emptyAnswers, previousAddressListPage(NormalMode), Some(previousAddressListPage(CheckMode))),
    (IndividualPreviousAddressListId, emptyAnswers, previousAddressPage(NormalMode), Some(previousAddressPage(CheckMode))),
    (IndividualPreviousAddressId, emptyAnswers, emailPage(NormalMode), Some(checkYourAnswersPage)),

    (IndividualEmailId, emptyAnswers, phonePage(NormalMode), Some(checkYourAnswersPage)),
    (IndividualPhoneId, nonUk, checkYourAnswersPage, Some(checkYourAnswersPage)),
    (IndividualPhoneId, uk, individualDateOfBirthPage, Some(checkYourAnswersPage)),

    (CheckYourAnswersId, emptyAnswers, declarationWorkingKnowledgePage, None)
  )

  def updateRoutes(): TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Next Page (CheckMode)"),
    (IndividualContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(UpdateMode), None),
    (IndividualContactAddressListId, emptyAnswers, contactAddressPage(UpdateMode), None),
    (IndividualContactAddressId, emptyAnswers, addressYearsPage(UpdateMode), None),
    (IndividualAddressYearsId, ukAddressYearsOverAYear, anyMoreChanges, None),
    (IndividualAddressYearsId, ukAddressYearsUnderAYear, confirmPreviousAddress, None),
    (IndividualAddressYearsId, emptyAnswers, sessionExpiredPage, None),
    (IndividualConfirmPreviousAddressId, emptyAnswers, sessionExpiredPage, Some(sessionExpiredPage)),
    (IndividualConfirmPreviousAddressId, samePreviousAddress, anyMoreChanges, None),
    (IndividualConfirmPreviousAddressId, notSamePreviousAddress, previousAddressPage(UpdateMode), None),
    (IndividualPreviousAddressPostCodeLookupId, emptyAnswers, previousAddressListPage(UpdateMode), None),
    (IndividualPreviousAddressListId, emptyAnswers, previousAddressPage(UpdateMode), None),
    (IndividualPreviousAddressId, emptyAnswers, anyMoreChanges, None),
    (IndividualEmailId, emptyAnswers, anyMoreChanges, None),
    (IndividualPhoneId, uk, anyMoreChanges, None),
    (IndividualPhoneId, nonUk, anyMoreChanges, None),
    (invalidIdForNavigator, emptyAnswers, sessionExpiredPage, Some(sessionExpiredPage))
  )


  def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val navigator = new IndividualNavigator(frontendAppConfig, countryOptions)

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, routes(), dataDescriber)
    behave like navigatorWithRoutes(navigator, updateRoutes(), dataDescriber, UpdateMode)
  }
}

object IndividualNavigatorSpec extends OptionValues {
  private def emailPage(mode: Mode): Call = routes.IndividualEmailController.onPageLoad(mode)
  private def phonePage(mode: Mode): Call = routes.IndividualPhoneController.onPageLoad(mode)
  private lazy val invalidIdForNavigator = AreYouInUKId

  lazy private val youWillNeedToUpdatePage = routes.YouWillNeedToUpdateController.onPageLoad()
  lazy private val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  lazy private val individualDateOfBirthPage = routes.IndividualDateOfBirthController.onPageLoad(NormalMode)
  lazy private val checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad()
  lazy private val declarationWorkingKnowledgePage = controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(NormalMode)
  lazy private val ukIndividualDetailsPage = routes.IndividualDetailsCorrectController.onPageLoad(NormalMode)
  lazy private val nonUkIndividualNamePage = routes.IndividualNameController.onPageLoad(NormalMode)
  lazy private val nonUkIndividualAddressPage = routes.IndividualRegisteredAddressController.onPageLoad(NormalMode)
  private def reconsiderAreYouInUk(mode: Mode): Call = routes.IndividualAreYouInUKController.onPageLoad(mode)
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
