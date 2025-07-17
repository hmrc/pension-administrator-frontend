/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.register.partnership.routes
import identifiers._
import identifiers.register._
import identifiers.register.partnership._
import identifiers.register.partnership.partners.PartnerNameId
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor3
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Navigator, NavigatorBehaviour, UserAnswerOps, UserAnswers}

class PartnershipNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import PartnershipNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[PartnershipNavigator]

  "PartnershipNavigator in NormalMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (BusinessUTRId, uk, partnershipNamePage),
      (BusinessNameId, uk, partnershipIsRegisteredNamePage),
      (IsRegisteredNameId, isRegisteredNameTrue.areYouInUk(true), confirmPartnershipDetailsPage),
      (IsRegisteredNameId, isRegisteredNameFalse.areYouInUk(true), updateDetails),
      (ConfirmPartnershipDetailsId, confirmPartnershipDetailsTrue.areYouInUk(true), hasPayePage),

      (HasPAYEId, hasPAYEYes.areYouInUk(true), payePage(NormalMode)),
      (HasPAYEId, hasPAYENo.areYouInUk(true), hasVatPage),
      (EnterPAYEId, uk, hasVatPage),
      (HasVATId, hasVatYes.areYouInUk(true), enterVatPage(NormalMode)),
      (HasVATId, hasVatNo.areYouInUk(true), sameContactAddressPage),
      (EnterVATId, uk, sameContactAddressPage),

      (PartnershipSameContactAddressId, isSameContactAddress, addressYearsPage(NormalMode)),
      (PartnershipSameContactAddressId, notSameContactAddressUk, contactPostcodePage(NormalMode)),

      (PartnershipContactAddressPostCodeLookupId, uk, contactAddressListPage(NormalMode)),
      (PartnershipContactAddressId, uk, addressYearsPage(NormalMode)),

      (PartnershipAddressYearsId, addressYearsOverAYear, emailPage),
      (PartnershipAddressYearsId, addressYearsUnderAYear, tradingOverAYearPage(NormalMode)),

      (PartnershipTradingOverAYearId, tradingOverAYearUk, contactPreviousPostCodePage(NormalMode)),
      (PartnershipTradingOverAYearId, tradingUnderAYear, emailPage),

      (PartnershipPreviousAddressPostCodeLookupId, uk, contactPreviousAddressListPage(NormalMode)),
      (PartnershipPreviousAddressId, uk, emailPage),

      (PartnershipEmailId, uk, phonePage),
      (PartnershipPhoneId, uk, checkYourAnswersPage),

      (CheckYourAnswersId, uk, wynPage),
      (CheckYourAnswersId, hasPartner, addPartnersPage()),
      (PartnershipReviewId, uk, declarationWorkingKnowledgePage(NormalMode)),

      (PartnershipRegisteredAddressId, uKAddress, reconsiderAreYouInUk),
    )
    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, NormalMode)
  }

  "PartnershipNavigator in CheckMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (HasPAYEId, hasPAYEYes, payePage(CheckMode)),
      (HasPAYEId, hasPAYENo, checkYourAnswersPage),
      (EnterPAYEId, emptyAnswers, checkYourAnswersPage),
      (HasVATId, hasVatYes, enterVatPage(CheckMode)),
      (HasVATId, hasVatNo, checkYourAnswersPage),
      (EnterVATId, emptyAnswers, checkYourAnswersPage),

      (PartnershipSameContactAddressId, isSameContactAddress, addressYearsPage(CheckMode)),
      (PartnershipSameContactAddressId, notSameContactAddressUk, contactPostcodePage(CheckMode)),
      (PartnershipSameContactAddressId, notSameContactAddressNonUk, contactAddressPage(CheckMode)),
      (PartnershipSameContactAddressId, emptyAnswers, sessionExpiredPage),

      (PartnershipContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(CheckMode)),
      (PartnershipContactAddressId, emptyAnswers, checkYourAnswersPage),

      (PartnershipAddressYearsId, addressYearsOverAYear, checkYourAnswersPage),
      (PartnershipAddressYearsId, addressYearsUnderAYear, tradingOverAYearPage(CheckMode)),

      (PartnershipTradingOverAYearId, tradingOverAYearUk, contactPreviousPostCodePage(CheckMode)),
      (PartnershipTradingOverAYearId, tradingUnderAYear, checkYourAnswersPage),

      (PartnershipPreviousAddressPostCodeLookupId, emptyAnswers, contactPreviousAddressListPage(CheckMode)),
      (PartnershipPreviousAddressId, emptyAnswers, checkYourAnswersPage),

      (PartnershipEmailId, emptyAnswers, checkYourAnswersPage),
      (PartnershipPhoneId, emptyAnswers, checkYourAnswersPage)

    )
    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, CheckMode)
  }

  "PartnershipNavigator in UpdateMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (PartnershipContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(UpdateMode)),
      (PartnershipContactAddressId, emptyAnswers, confirmPreviousAddressPage),
      (PartnershipAddressYearsId, addressYearsOverAYear, anyMoreChangesPage),
      (PartnershipAddressYearsId, addressYearsUnderAYear, tradingOverAYearPage(UpdateMode)),
      (PartnershipAddressYearsId, emptyAnswers, sessionExpiredPage),

      (PartnershipEmailId, emptyAnswers, anyMoreChangesPage),
      (PartnershipEmailId, updatingContactAddressForRLS, updateContactAddressCYAPage),

      (PartnershipPhoneId, emptyAnswers, anyMoreChangesPage),
      (PartnershipPhoneId, updatingContactAddressForRLS, updateContactAddressCYAPage),

      (PartnershipPreviousAddressPostCodeLookupId, emptyAnswers, contactPreviousAddressListPage(UpdateMode)),
      (PartnershipPreviousAddressId, updatingContactAddressForRLS, updateContactAddressCYAPage),

      (PartnershipConfirmPreviousAddressId, emptyAnswers, sessionExpiredPage),
      (PartnershipConfirmPreviousAddressId, varianceConfirmPreviousAddressYes, anyMoreChangesPage),
      (PartnershipConfirmPreviousAddressId, samePreviousAddressUpdateContactAddress, updateContactAddressCYAPage),
      (PartnershipConfirmPreviousAddressId, varianceConfirmPreviousAddressNo, contactPreviousPostCodePage(UpdateMode))
    )
    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, UpdateMode)
  }

}

object PartnershipNavigatorSpec extends OptionValues {

  private lazy val updatingContactAddressForRLS = UserAnswers(Json.obj()).set(UpdateContactAddressId)(true).asOpt.value

  private def samePreviousAddressUpdateContactAddress = UserAnswers(Json.obj())
    .set(PartnershipConfirmPreviousAddressId)(true).asOpt.value
    .set(UpdateContactAddressId)(true).asOpt.value

  private lazy val updateContactAddressCYAPage:Call = controllers.routes.UpdateContactAddressCYAController.onPageLoad()
  private lazy val sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad

  private lazy val anyMoreChangesPage: Call = controllers.register.routes.AnyMoreChangesController.onPageLoad()

  private lazy val confirmPartnershipDetailsPage: Call = routes.ConfirmPartnershipDetailsController.onPageLoad()

  private lazy val partnershipNamePage = routes.PartnershipNameController.onPageLoad

  private lazy val partnershipIsRegisteredNamePage = routes.PartnershipIsRegisteredNameController.onPageLoad

  private lazy val sameContactAddressPage: Call = routes.PartnershipSameContactAddressController.onPageLoad(NormalMode)

  private lazy val checkYourAnswersPage: Call = routes.CheckYourAnswersController.onPageLoad()

  private lazy val emailPage: Call = routes.PartnershipEmailController.onPageLoad(NormalMode)

  private lazy val phonePage: Call = routes.PartnershipPhoneController.onPageLoad(NormalMode)

  private lazy val hasVatPage: Call = routes.HasPartnershipVATController.onPageLoad(NormalMode)

  private def enterVatPage(mode: Mode): Call = routes.PartnershipEnterVATController.onPageLoad(mode)

  private def tradingOverAYearPage(mode: Mode): Call = routes.PartnershipTradingOverAYearController.onPageLoad(mode)

  private lazy val wynPage: Call = controllers.register.partnership.partners.routes.WhatYouWillNeedController.onPageLoad()

  private def addPartnersPage(): Call = routes.AddPartnerController.onPageLoad(NormalMode)

  private def addressYearsPage(mode: Mode): Call = routes.PartnershipAddressYearsController.onPageLoad(mode)

  private def contactPostcodePage(mode: Mode): Call = routes.PartnershipContactAddressPostCodeLookupController.onPageLoad(mode)

  private def contactAddressListPage(mode: Mode): Call = routes.PartnershipContactAddressListController.onPageLoad(mode)

  private def contactAddressPage(mode: Mode): Call = routes.PartnershipContactAddressController.onPageLoad(mode)

  private def contactPreviousPostCodePage(mode: Mode): Call = routes.PartnershipPreviousAddressPostCodeLookupController.onPageLoad(mode)

  private def contactPreviousAddressListPage(mode: Mode): Call = routes.PartnershipPreviousAddressListController.onPageLoad(mode)

  private lazy val confirmPreviousAddressPage: Call = routes.PartnershipConfirmPreviousAddressController.onPageLoad()

  private def declarationWorkingKnowledgePage(mode: Mode): Call = controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(mode)


  private lazy val hasPayePage: Call = routes.HasPartnershipPAYEController.onPageLoad(NormalMode)

  private def payePage(mode: Mode): Call = routes.PartnershipEnterPAYEController.onPageLoad(mode)

  private lazy val reconsiderAreYouInUk: Call = controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(CheckMode)


  private lazy val updateDetails = controllers.register.partnership.routes.PartnershipUpdateDetailsController.onPageLoad()

  protected val uk: UserAnswers = UserAnswers().areYouInUk(true)
  protected val nonUk: UserAnswers = UserAnswers().areYouInUk(false)
  private val hasPAYEYes = UserAnswers().set(HasPAYEId)(value = true).asOpt.value
  private val hasPAYENo = UserAnswers().set(HasPAYEId)(value = false).asOpt.value

  protected val hasVatYes: UserAnswers = UserAnswers().hasVat(true)
  protected val hasVatNo: UserAnswers = UserAnswers().hasVat(false)

  protected val isRegisteredNameTrue: UserAnswers = UserAnswers().isRegisteredName(true)
  protected val isRegisteredNameFalse: UserAnswers = UserAnswers().isRegisteredName(false)

  private val varianceConfirmPreviousAddressYes = UserAnswers().set(PartnershipConfirmPreviousAddressId)(true).asOpt.get
  private val varianceConfirmPreviousAddressNo = UserAnswers().set(PartnershipConfirmPreviousAddressId)(false).asOpt.get

  private val uKAddress = uk.nonUkPartnershipAddress(address("GB"))

  private val notSameContactAddressUk = UserAnswers().areYouInUk(true).partnershipSameContactAddress(false)
  private val notSameContactAddressNonUk = UserAnswers().areYouInUk(false).partnershipSameContactAddress(false)
  private val isSameContactAddress = uk.partnershipSameContactAddress(true)

  private val addressYearsUnderAYear = uk.partnershipAddressYears(AddressYears.UnderAYear)
  private val tradingUnderAYear = uk.set(PartnershipTradingOverAYearId)(false).asOpt.value
  private val tradingOverAYearUk = UserAnswers(Json.obj()).areYouInUk(true).set(PartnershipTradingOverAYearId)(true).asOpt.value
  private val addressYearsOverAYear = uk.partnershipAddressYears(AddressYears.OverAYear)
  val hasPartner: UserAnswers = uk
    .set(PartnerNameId(0))(PersonName("first", "last")).asOpt.value

  private def address(countryCode: String) = Address("addressLine1", "addressLine2", Some("addressLine3"), Some("addressLine4"), Some("NE11AA"), countryCode)

  private val confirmPartnershipDetailsTrue = UserAnswers(Json.obj()).set(ConfirmPartnershipDetailsId)(true).asOpt.value
}
