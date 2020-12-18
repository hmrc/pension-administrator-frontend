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
import controllers.register.partnership.routes
import identifiers._
import identifiers.register.partnership._
import identifiers.register.partnership.partners.PartnerNameId
import identifiers.register.{BusinessNameId, BusinessUTRId, EnterVATId, HasVATId, IsRegisteredNameId, _}
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor3
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Navigator, NavigatorBehaviour, UserAnswers}

class PartnershipNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import PartnershipNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[PartnershipNavigator]

  "PartnershipNavigator in NormalMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (BusinessUTRId, emptyAnswers, partnershipNamePage),
      (BusinessNameId, uk, partnershipIsRegisteredNamePage),
      (BusinessNameId, nonUk, nonUkAddress),
      (IsRegisteredNameId, isRegisteredNameTrue, confirmPartnershipDetailsPage),
      (IsRegisteredNameId, isRegisteredNameFalse, companyUpdate),
      (ConfirmPartnershipDetailsId, confirmPartnershipDetailsTrue, hasPayePage),

      (HasPAYEId, hasPAYEYes, payePage(NormalMode)),
      (HasPAYEId, hasPAYENo, hasVatPage),
      (EnterPAYEId, emptyAnswers, hasVatPage),
      (HasVATId, hasVatYes, enterVatPage(NormalMode)),
      (HasVATId, hasVatNo, sameContactAddressPage),
      (EnterVATId, emptyAnswers, sameContactAddressPage),

      (PartnershipSameContactAddressId, isSameContactAddress, addressYearsPage(NormalMode)),
      (PartnershipSameContactAddressId, notSameContactAddressUk, contactPostcodePage(NormalMode)),
      (PartnershipSameContactAddressId, notSameContactAddressNonUk, contactAddressPage(NormalMode)),

      (PartnershipContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(NormalMode)),
      (PartnershipContactAddressId, emptyAnswers, addressYearsPage(NormalMode)),

      (PartnershipAddressYearsId, addressYearsOverAYear, emailPage),
      (PartnershipAddressYearsId, addressYearsUnderAYear, tradingOverAYearPage(NormalMode)),

      (PartnershipTradingOverAYearId, tradingOverAYearUk, contactPreviousPostCodePage(NormalMode)),
      (PartnershipTradingOverAYearId, tradingOverAYearNonUk, contactPreviousAddressPage(NormalMode)),
      (PartnershipTradingOverAYearId, tradingUnderAYear, emailPage),

      (PartnershipPreviousAddressPostCodeLookupId, emptyAnswers, contactPreviousAddressListPage(NormalMode)),
      (PartnershipPreviousAddressId, emptyAnswers, emailPage),

      (PartnershipEmailId, emptyAnswers, phonePage),
      (PartnershipPhoneId, emptyAnswers, checkYourAnswersPage),

      (CheckYourAnswersId, emptyAnswers, wynPage),
      (CheckYourAnswersId, hasPartner, addPartnersPage()),
      (PartnershipReviewId, emptyAnswers, declarationWorkingKnowledgePage(NormalMode)),

      (PartnershipRegisteredAddressId, nonUkEuAddress, sameContactAddressPage),
      (PartnershipRegisteredAddressId, uKAddress, reconsiderAreYouInUk),
      (PartnershipRegisteredAddressId, nonUkNonEuAddress, outsideEuEea)
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
      (PartnershipTradingOverAYearId, tradingOverAYearNonUk, contactPreviousAddressPage(CheckMode)),
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

      (PartnershipEmailId, uk, anyMoreChangesPage),
      (PartnershipEmailId, nonUk, anyMoreChangesPage),
      (PartnershipEmailId, emptyAnswers, anyMoreChangesPage),

      (PartnershipPhoneId, uk, anyMoreChangesPage),
      (PartnershipPhoneId, nonUk, anyMoreChangesPage),
      (PartnershipPhoneId, emptyAnswers, anyMoreChangesPage),

      (PartnershipPreviousAddressPostCodeLookupId, emptyAnswers, contactPreviousAddressListPage(UpdateMode)),
      (PartnershipPreviousAddressId, rLSFlag, stillUsePage),

      (PartnershipConfirmPreviousAddressId, emptyAnswers, sessionExpiredPage),
      (PartnershipConfirmPreviousAddressId, varianceConfirmPreviousAddressYes, anyMoreChangesPage),
      (PartnershipConfirmPreviousAddressId, samePreviousAddressUpdateContactAddress, stillUsePage),
      (PartnershipConfirmPreviousAddressId, varianceConfirmPreviousAddressNo, contactPreviousPostCodePage(UpdateMode))
    )
    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, UpdateMode)
  }

}

object PartnershipNavigatorSpec extends OptionValues {

  private lazy val rLSFlag = UserAnswers(Json.obj()).set(UpdateContactAddressId)(true).asOpt.value

  private def samePreviousAddressUpdateContactAddress = UserAnswers(Json.obj())
    .set(PartnershipConfirmPreviousAddressId)(true).asOpt.value
    .set(UpdateContactAddressId)(true).asOpt.value

  private val stillUsePage = controllers.register.routes.StillUseAdviserController.onPageLoad()

  private lazy val sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad()

  private lazy val anyMoreChangesPage: Call = controllers.register.routes.AnyMoreChangesController.onPageLoad()

  private lazy val confirmPartnershipDetailsPage: Call = routes.ConfirmPartnershipDetailsController.onPageLoad()

  private lazy val partnershipNamePage = routes.PartnershipNameController.onPageLoad()

  private lazy val partnershipIsRegisteredNamePage = routes.PartnershipIsRegisteredNameController.onPageLoad()

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

  private def contactPreviousAddressPage(mode: Mode): Call = routes.PartnershipPreviousAddressController.onPageLoad(mode)

  private def declarationWorkingKnowledgePage(mode: Mode): Call = controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(mode)

  private lazy val nonUkAddress: Call = routes.PartnershipRegisteredAddressController.onPageLoad()

  private lazy val hasPayePage: Call = routes.HasPartnershipPAYEController.onPageLoad(NormalMode)

  private def payePage(mode: Mode): Call = routes.PartnershipEnterPAYEController.onPageLoad(mode)

  private lazy val reconsiderAreYouInUk: Call = controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(CheckMode)

  private lazy val outsideEuEea: Call = routes.OutsideEuEeaController.onPageLoad()

  private lazy val companyUpdate = controllers.register.company.routes.CompanyUpdateDetailsController.onPageLoad()

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

  private val nonUkEuAddress = UserAnswers().nonUkPartnershipAddress(address("AT"))
  private val uKAddress = UserAnswers().nonUkPartnershipAddress(address("GB"))
  private val nonUkNonEuAddress = UserAnswers().nonUkPartnershipAddress(address("AF"))

  private val notSameContactAddressUk = UserAnswers().areYouInUk(true).partnershipSameContactAddress(false)
  private val notSameContactAddressNonUk = UserAnswers().areYouInUk(false).partnershipSameContactAddress(false)
  private val isSameContactAddress = UserAnswers().partnershipSameContactAddress(true)

  private val addressYearsUnderAYear = UserAnswers().partnershipAddressYears(AddressYears.UnderAYear)
  private val tradingUnderAYear = UserAnswers().set(PartnershipTradingOverAYearId)(false).asOpt.value
  private val tradingOverAYearUk = UserAnswers(Json.obj()).areYouInUk(true).set(PartnershipTradingOverAYearId)(true).asOpt.value
  private val tradingOverAYearNonUk = UserAnswers(Json.obj()).areYouInUk(false).set(PartnershipTradingOverAYearId)(true).asOpt.value
  private val addressYearsOverAYear = UserAnswers().partnershipAddressYears(AddressYears.OverAYear)
  val hasPartner: UserAnswers = UserAnswers(Json.obj())
    .set(PartnerNameId(0))(PersonName("first", "last")).asOpt.value

  private def address(countryCode: String) = Address("addressLine1", "addressLine2", Some("addressLine3"), Some("addressLine4"), Some("NE11AA"), countryCode)

  private val confirmPartnershipDetailsTrue = UserAnswers(Json.obj()).set(ConfirmPartnershipDetailsId)(true).asOpt.value
}
