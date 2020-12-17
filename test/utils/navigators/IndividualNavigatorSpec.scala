/*
 * Copyright 2020 HM Revenue & Customs
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
import identifiers.{Identifier, RLSFlagId}
import identifiers.register.AreYouInUKId
import identifiers.register.individual._
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.{TableFor3, TableFor4}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, Navigator, NavigatorBehaviour, UserAnswers}

class IndividualNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import IndividualNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[IndividualNavigator]

  "IndividualNavigator in NormalMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (AreYouInUKId, emptyAnswers, sessionExpiredPage),
      (AreYouInUKId, uk, ukIndividualDetailsPage),
      (AreYouInUKId, nonUk, nonUkIndividualNamePage),
      (AreYouInUKId, nonUkNoIndividualDetails, nonUkIndividualNamePage),

      (IndividualDetailsCorrectId, detailsCorrect, individualDateOfBirthPage),
      (IndividualDetailsCorrectId, detailsIncorrect, youWillNeedToUpdatePage),
      (IndividualDetailsCorrectId, emptyAnswers, sessionExpiredPage),

      (IndividualDetailsId, emptyAnswers, nonUkIndividualAddressPage),

      (IndividualAddressId, nonUkEuAddress, individualDateOfBirthPage),
      (IndividualAddressId, nonUkButUKAddress, reconsiderAreYouInUk(CheckMode)),
      (IndividualAddressId, nonUkNonEuAddress, outsideEuEea),

      (WhatYouWillNeedId, emptyAnswers, reconsiderAreYouInUk(NormalMode)),

      (IndividualDateOfBirthId, emptyAnswers, sessionExpiredPage),
      (IndividualDateOfBirthId, uk, sameContactAddressPage(NormalMode)),
      (IndividualDateOfBirthId, nonUk, sameContactAddressPage(NormalMode)),
      (IndividualSameContactAddressId, sameContactAddressUk, addressYearsPage(NormalMode)),

      (IndividualSameContactAddressId, sameContactAddressNonUk, addressYearsPage(NormalMode)),
      (IndividualSameContactAddressId, ukDifferentContactAddress, contactPostCodeLookupPage(NormalMode)),
      (IndividualSameContactAddressId, nonUkDifferentContactAddress, contactAddressPage(NormalMode)),
      (IndividualSameContactAddressId, sameContactAddressIncompleteUk, contactAddressPage(NormalMode)),
      (IndividualSameContactAddressId, sameContactAddressIncompleteNonUk, contactAddressPage(NormalMode)),

      (IndividualContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(NormalMode)),
      (IndividualContactAddressId, emptyAnswers, addressYearsPage(NormalMode)),

      (IndividualAddressYearsId, ukAddressYearsOverAYear, emailPage(NormalMode)),
      (IndividualAddressYearsId, nonUkAddressYearsOverAYear, emailPage(NormalMode)),
      (IndividualAddressYearsId, ukAddressYearsUnderAYear, previousPostCodeLookupPage(NormalMode)),
      (IndividualAddressYearsId, nonUkAddressYearsUnderAYear, previousAddressPage(NormalMode)),
      (IndividualAddressYearsId, emptyAnswers, sessionExpiredPage),

      (IndividualPreviousAddressPostCodeLookupId, emptyAnswers, previousAddressListPage(NormalMode)),
      (IndividualPreviousAddressId, emptyAnswers, emailPage(NormalMode)),

      (IndividualEmailId, emptyAnswers, phonePage(NormalMode)),
      (IndividualPhoneId, nonUk, checkYourAnswersPage),
      (IndividualPhoneId, uk, individualDateOfBirthPage),

      (CheckYourAnswersId, emptyAnswers, declarationWorkingKnowledgePage)
    )
      behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, NormalMode)
  }

  "IndividualNavigator in CheckMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (AreYouInUKId, emptyAnswers, sessionExpiredPage),
      (AreYouInUKId, nonUk, nonUkIndividualAddressPage),
      (AreYouInUKId, nonUkNoIndividualDetails, nonUkIndividualNamePage),

      (IndividualDateOfBirthId, emptyAnswers, checkYourAnswersPage),
      (IndividualDateOfBirthId, nonUk, checkYourAnswersPage),
      (IndividualSameContactAddressId, sameContactAddressUk, addressYearsPage(CheckMode)),

      (IndividualSameContactAddressId, sameContactAddressNonUk, addressYearsPage(CheckMode)),
      (IndividualSameContactAddressId, ukDifferentContactAddress, contactPostCodeLookupPage(CheckMode)),
      (IndividualSameContactAddressId, nonUkDifferentContactAddress, contactAddressPage(CheckMode)),
      (IndividualSameContactAddressId, sameContactAddressIncompleteUk, contactAddressPage(CheckMode)),
      (IndividualSameContactAddressId, sameContactAddressIncompleteNonUk, contactAddressPage(CheckMode)),

      (IndividualContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(CheckMode)),
      (IndividualContactAddressId, emptyAnswers, addressYearsPage(CheckMode)),

      (IndividualAddressYearsId, ukAddressYearsOverAYear, checkYourAnswersPage),
      (IndividualAddressYearsId, nonUkAddressYearsOverAYear, checkYourAnswersPage),
      (IndividualAddressYearsId, ukAddressYearsUnderAYear, previousPostCodeLookupPage(CheckMode)),
      (IndividualAddressYearsId, nonUkAddressYearsUnderAYear, previousAddressPage(CheckMode)),
      (IndividualAddressYearsId, emptyAnswers, sessionExpiredPage),

      (IndividualPreviousAddressPostCodeLookupId, emptyAnswers, previousAddressListPage(CheckMode)),
      (IndividualPreviousAddressId, emptyAnswers, checkYourAnswersPage),

      (IndividualEmailId, emptyAnswers, checkYourAnswersPage),
      (IndividualPhoneId, nonUk, checkYourAnswersPage),
      (IndividualPhoneId, uk, checkYourAnswersPage)
    )
    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, CheckMode)
  }

  "IndividualNavigator in UpdateMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (IndividualContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(UpdateMode)),
      (IndividualContactAddressId, emptyAnswers, confirmPreviousAddress),
      (IndividualAddressYearsId, ukAddressYearsOverAYear, anyMoreChanges),
      (IndividualAddressYearsId, ukAddressYearsUnderAYear, confirmPreviousAddress),
      (IndividualAddressYearsId, emptyAnswers, sessionExpiredPage),
      (IndividualConfirmPreviousAddressId, emptyAnswers, sessionExpiredPage),
      (IndividualConfirmPreviousAddressId, samePreviousAddress, anyMoreChanges),
      (IndividualConfirmPreviousAddressId, samePreviousAddressRLSFlag, variationsDeclarationPage),
      (IndividualConfirmPreviousAddressId, notSamePreviousAddress, previousPostCodeLookupPage(UpdateMode)),
      (IndividualPreviousAddressPostCodeLookupId, emptyAnswers, previousAddressListPage(UpdateMode)),
      (IndividualPreviousAddressId, emptyAnswers, anyMoreChanges),
      (IndividualPreviousAddressId, rLSFlag, variationsDeclarationPage),
      (IndividualEmailId, emptyAnswers, anyMoreChanges),
      (IndividualPhoneId, uk, anyMoreChanges),
      (IndividualPhoneId, nonUk, anyMoreChanges)
    )
    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, UpdateMode)
  }

}

object IndividualNavigatorSpec extends OptionValues {
  private def emailPage(mode: Mode): Call = routes.IndividualEmailController.onPageLoad(mode)
  private def phonePage(mode: Mode): Call = routes.IndividualPhoneController.onPageLoad(mode)
  private lazy val invalidIdForNavigator = AreYouInUKId

  lazy private val variationsDeclarationPage = controllers.register.routes.VariationDeclarationController.onPageLoad()
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

  private val rLSFlag = UserAnswers(Json.obj())
    .set(RLSFlagId)(true).asOpt.value

  private val nonUkEuAddress = UserAnswers().individualAddress(address("AT"))
  private val nonUkButUKAddress = UserAnswers().individualAddress(address("GB"))
  private val nonUkNonEuAddress = UserAnswers().individualAddress(address("AF"))

  private def address(countryCode: String) = Address("addressLine1", "addressLine2", Some("addressLine3"), Some("addressLine4"), Some("NE11AA"), countryCode)

  private def samePreviousAddress = UserAnswers(Json.obj())
    .set(IndividualConfirmPreviousAddressId)(true).asOpt.value

  private def samePreviousAddressRLSFlag = UserAnswers(Json.obj())
    .set(IndividualConfirmPreviousAddressId)(true).asOpt.value
    .set(RLSFlagId)(true).asOpt.value

  private def notSamePreviousAddress = UserAnswers(Json.obj())
    .set(IndividualConfirmPreviousAddressId)(false).asOpt.value

}
