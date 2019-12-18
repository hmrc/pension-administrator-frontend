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
import connectors.cache.FakeUserAnswersCacheConnector
import controllers.register.partnership.routes
import identifiers._
import identifiers.register.partnership._
import identifiers.register.partnership.partners.PartnerNameId
import identifiers.register.{BusinessNameId, BusinessUTRId, EnterVATId, HasVATId, IsRegisteredNameId, _}
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor4
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, NavigatorBehaviour, UserAnswers}

class PartnershipNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import PartnershipNavigatorSpec._

  def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val navigator = new PartnershipNavigator(FakeUserAnswersCacheConnector, countryOptions, frontendAppConfig)

  //scalastyle:off line.size.limit
  private def routes(): TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Next Page (Check Mode)"),

    (BusinessUTRId, emptyAnswers, partnershipNamePage, None),
    (BusinessNameId, uk, partnershipIsRegisteredNamePage, None),
    (BusinessNameId, nonUk, nonUkAddress, None),
    (IsRegisteredNameId, isRegisteredNameTrue, confirmPartnershipDetailsPage, None),
    (IsRegisteredNameId, isRegisteredNameFalse, companyUpdate, None),
    (ConfirmPartnershipDetailsId, confirmPartnershipDetailsTrue, hasPayePage, None),

    (HasPAYEId, hasPAYEYes, payePage(NormalMode), Some(payePage(CheckMode))),
    (HasPAYEId, hasPAYENo, hasVatPage, Some(checkYourAnswersPage)),
    (EnterPAYEId, emptyAnswers, hasVatPage, Some(checkYourAnswersPage)),
    (HasVATId, hasVatYes, enterVatPage(NormalMode), Some(enterVatPage(CheckMode))),
    (HasVATId, hasVatNo, sameContactAddressPage, Some(checkYourAnswersPage)),
    (EnterVATId, emptyAnswers, sameContactAddressPage, Some(checkYourAnswersPage)),

    (PartnershipSameContactAddressId, isSameContactAddress, addressYearsPage(NormalMode), Some(addressYearsPage(CheckMode))),
    (PartnershipSameContactAddressId, notSameContactAddressUk, contactPostcodePage(NormalMode), Some(contactPostcodePage(CheckMode))),
    (PartnershipSameContactAddressId, notSameContactAddressNonUk, contactAddressPage(NormalMode), Some(contactAddressPage(CheckMode))),
    (PartnershipSameContactAddressId, emptyAnswers, sessionExpiredPage, Some(sessionExpiredPage)),

    (PartnershipContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(NormalMode), Some(contactAddressListPage(CheckMode))),
    (PartnershipContactAddressId, emptyAnswers, addressYearsPage(NormalMode), Some(checkYourAnswersPage)),

    (PartnershipAddressYearsId, addressYearsOverAYear, emailPage, Some(checkYourAnswersPage)),
    (PartnershipAddressYearsId, addressYearsUnderAYear, tradingOverAYearPage(NormalMode), Some(tradingOverAYearPage(CheckMode))),
    (PartnershipAddressYearsId, emptyAnswers, sessionExpiredPage, Some(sessionExpiredPage)),

    (PartnershipTradingOverAYearId, tradingOverAYearUk, contactPreviousPostCodePage(NormalMode), Some(contactPreviousPostCodePage(CheckMode))),
    (PartnershipTradingOverAYearId, tradingOverAYearNonUk, contactPreviousAddressPage(NormalMode), Some(contactPreviousAddressPage(CheckMode))),
    (PartnershipTradingOverAYearId, tradingUnderAYear, emailPage, Some(checkYourAnswersPage)),

    (PartnershipPreviousAddressPostCodeLookupId, emptyAnswers, contactPreviousAddressListPage(NormalMode), Some(contactPreviousAddressListPage(CheckMode))),
    (PartnershipPreviousAddressId, emptyAnswers, emailPage, Some(checkYourAnswersPage)),

    (PartnershipEmailId, emptyAnswers, phonePage, Some(checkYourAnswersPage)),
    (PartnershipPhoneId, emptyAnswers, checkYourAnswersPage, Some(checkYourAnswersPage)),

    (CheckYourAnswersId, emptyAnswers, wynPage, None),
    (CheckYourAnswersId, hasPartner, addPartnersPage(), None),
    (PartnershipReviewId, emptyAnswers, declarationWorkingKnowledgePage(NormalMode), None),

    (PartnershipRegisteredAddressId, nonUkEuAddress, sameContactAddressPage, None),
    (PartnershipRegisteredAddressId, uKAddress, reconsiderAreYouInUk, None),
    (PartnershipRegisteredAddressId, nonUkNonEuAddress, outsideEuEea, None)
  )

  private def updateRoutes(): TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Next Page (Check Mode)"),
    (PartnershipContactAddressPostCodeLookupId, emptyAnswers, contactAddressListPage(UpdateMode), None),
    (PartnershipContactAddressId, emptyAnswers, confirmPreviousAddressPage, None),
    (PartnershipAddressYearsId, addressYearsOverAYear, anyMoreChangesPage, None),
    (PartnershipAddressYearsId, addressYearsUnderAYear, tradingOverAYearPage(UpdateMode), None),
    (PartnershipAddressYearsId, emptyAnswers, sessionExpiredPage, Some(sessionExpiredPage)),

    (PartnershipEmailId, uk, anyMoreChangesPage, None),
    (PartnershipEmailId, nonUk, anyMoreChangesPage, None),
    (PartnershipEmailId, emptyAnswers, anyMoreChangesPage, None),

    (PartnershipPhoneId, uk, anyMoreChangesPage, None),
    (PartnershipPhoneId, nonUk, anyMoreChangesPage, None),
    (PartnershipPhoneId, emptyAnswers, anyMoreChangesPage, None),

    (PartnershipPreviousAddressPostCodeLookupId, emptyAnswers, contactPreviousAddressListPage(UpdateMode), None),
    (PartnershipPreviousAddressId, emptyAnswers, anyMoreChangesPage, None),
    (PartnershipConfirmPreviousAddressId, emptyAnswers, sessionExpiredPage, None),
    (PartnershipConfirmPreviousAddressId, varianceConfirmPreviousAddressYes, anyMoreChangesPage, None),
    (PartnershipConfirmPreviousAddressId, varianceConfirmPreviousAddressNo, contactPreviousPostCodePage(UpdateMode), None)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, routes(), dataDescriber)
    behave like navigatorWithRoutes(navigator, updateRoutes(), dataDescriber, UpdateMode)
  }
}

object PartnershipNavigatorSpec extends OptionValues {

  private lazy val sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad()

  private lazy val anyMoreChangesPage: Call = controllers.register.routes.AnyMoreChangesController.onPageLoad()

  private lazy val confirmPartnershipDetailsPage: Call = routes.ConfirmPartnershipDetailsController.onPageLoad()

  private lazy val whatYouWillNeedPage: Call = routes.WhatYouWillNeedController.onPageLoad()

  private lazy val partnershipNamePage = routes.PartnershipNameController.onPageLoad()

  private lazy val partnershipIsRegisteredNamePage = routes.PartnershipIsRegisteredNameController.onPageLoad()

  private lazy val sameContactAddressPage: Call = routes.PartnershipSameContactAddressController.onPageLoad(NormalMode)

  private lazy val checkYourAnswersPage: Call = routes.CheckYourAnswersController.onPageLoad()

  private lazy val emailPage: Call = routes.PartnershipEmailController.onPageLoad(NormalMode)

  private lazy val phonePage: Call = routes.PartnershipPhoneController.onPageLoad(NormalMode)

  private lazy val hasVatPage: Call = routes.HasPartnershipVATController.onPageLoad(NormalMode)

  private def enterVatPage(mode: Mode): Call = routes.PartnershipEnterVATController.onPageLoad(mode)

  private lazy val payeNumberPage: Call = routes.HasPartnershipPAYEController.onPageLoad(NormalMode)

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
