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
import controllers.register.company.routes
import identifiers.register._
import identifiers.register.company.directors.DirectorNameId
import identifiers.register.company.{CompanyPhoneId, _}
import identifiers.register.partnership.ConfirmPartnershipDetailsId
import identifiers.{Identifier, LastPageId}
import models._
import models.register.BusinessType
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor4
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, NavigatorBehaviour, UserAnswers}

class RegisterCompanyNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import RegisterCompanyNavigatorSpec._

  def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val navigator = new RegisterCompanyNavigator(countryOptions, frontendAppConfig)

  private def routes(): TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Next Page (Check Mode)"),
    (BusinessUTRId, emptyAnswers, companyNamePage, None),
    (BusinessNameId, uk, companyIsRegisteredNamePage, None),
    (BusinessNameId, nonUk, nonUkAddress, None),
    (IsRegisteredNameId, isRegisteredNameTrue, confirmCompanyDetailsPage, None),
    (IsRegisteredNameId, isRegisteredNameFalse, companyUpdate, None),

    (ConfirmCompanyAddressId, confirmAddressTrueLimitedCompany, companyRegistrationNumberPage(NormalMode), None),
    (ConfirmCompanyAddressId, confirmAddressTrueUnlimitedCompany, hasCRNPage(NormalMode), None),

    (HasCompanyCRNId, hasCRN(true), companyRegistrationNumberPage(NormalMode), Some(companyRegistrationNumberPage(CheckMode))),
    (HasCompanyCRNId, hasCRN(false), hasPayePage, Some(checkYourAnswersPage)),
    (CompanyRegistrationNumberId, emptyAnswers, hasPayePage, Some(checkYourAnswersPage)),

    (HasPAYEId, hasPAYEYes, payePage(), Some(payePage(CheckMode))),
    (HasPAYEId, hasPAYENo, hasVatPage, Some(checkYourAnswersPage)),
    (EnterPAYEId, emptyAnswers, hasVatPage, Some(checkYourAnswersPage)),

    (HasVATId, hasVATYes, vatPage(), Some(vatPage(CheckMode))),
    (HasVATId, hasVATNo, sameContactAddress(NormalMode), Some(checkYourAnswersPage)),

    (EnterVATId, emptyAnswers, sameContactAddress(NormalMode), Some(checkYourAnswersPage)),

    (CompanySameContactAddressId, isSameContactAddress, companyAddressYearsPage(NormalMode), Some(companyAddressYearsPage(CheckMode))),
    (CompanySameContactAddressId, notSameContactAddressUk, contactAddressPostCode(NormalMode), Some(contactAddressPostCode(CheckMode))),
    (CompanySameContactAddressId, notSameContactAddressNonUk, contactAddress(NormalMode), Some(contactAddress(CheckMode))),
    (CompanySameContactAddressId, emptyAnswers, sessionExpiredPage, Some(sessionExpiredPage)),

    (CompanyContactAddressPostCodeLookupId, emptyAnswers, contactAddressList(NormalMode), Some(contactAddressList(CheckMode))),
    (CompanyContactAddressListId, emptyAnswers, contactAddress(NormalMode), Some(contactAddress(CheckMode))),
    (CompanyContactAddressId, emptyAnswers, companyAddressYearsPage(NormalMode), Some(checkYourAnswersPage)),

    (CompanyAddressYearsId, addressYearsOverAYear, emailPage(NormalMode), Some(checkYourAnswersPage)),
    (CompanyAddressYearsId, addressYearsUnderAYear, hasBeenTradingPage(NormalMode), Some(hasBeenTradingPage(CheckMode))),
    (CompanyAddressYearsId, emptyAnswers, sessionExpiredPage, Some(sessionExpiredPage)),

    (CompanyTradingOverAYearId, tradingOverAYearUk, paPostCodePage(NormalMode), Some(paPostCodePage(CheckMode))),
    (CompanyTradingOverAYearId, tradingOverAYearNonUk, previousAddressPage(NormalMode), Some(previousAddressPage(CheckMode))),
    (CompanyTradingOverAYearId, tradingUnderAYear, emailPage(NormalMode), Some(checkYourAnswersPage)),

    (CompanyPreviousAddressPostCodeLookupId, emptyAnswers, paAddressListPage(NormalMode), Some(paAddressListPage(CheckMode))),
    (CompanyAddressListId, emptyAnswers, previousAddressPage(NormalMode), Some(previousAddressPage(CheckMode))),
    (CompanyPreviousAddressId, emptyAnswers, emailPage(NormalMode), Some(checkYourAnswersPage)),

    (CompanyEmailId, emptyAnswers, phonePage(NormalMode), Some(checkYourAnswersPage)),
    (CompanyPhoneId, uk, checkYourAnswersPage, Some(checkYourAnswersPage)),
    (CompanyPhoneId, nonUk, checkYourAnswersPage, Some(checkYourAnswersPage)),

    (CheckYourAnswersId, emptyAnswers, whatYouWillNeedDirectorPage, None),
    (CheckYourAnswersId, hasDirector, addCompanyDirectors(NormalMode), None),

    (CompanyReviewId, emptyAnswers, declarationWorkingKnowledgePage(NormalMode), None),

    (CompanyAddressId, nonUkEuAddress, whatYouWillNeedPage, None),
    (CompanyAddressId, nonUkButUKAddress, reconsiderAreYouInUk, None),
    (CompanyAddressId, nonUkNonEuAddress, outsideEuEea, None),

    (WhatYouWillNeedId, emptyAnswers, sameContactAddress(NormalMode), None)
  )

  private def updateRoutes(): TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Next Page (Check Mode)"),

    (CompanyContactAddressPostCodeLookupId, emptyAnswers, contactAddressList(UpdateMode), None),
    (CompanyContactAddressListId, emptyAnswers, contactAddress(UpdateMode), None),
    (CompanyContactAddressId, emptyAnswers, companyAddressYearsPage(UpdateMode), None),

    (CompanyAddressYearsId, addressYearsOverAYear, anyMoreChanges, None),
    (CompanyAddressYearsId, addressYearsUnderAYear, confirmPreviousAddressPage, None),
    (CompanyAddressYearsId, emptyAnswers, sessionExpiredPage, None),

    (CompanyPreviousAddressPostCodeLookupId, emptyAnswers, paAddressListPage(UpdateMode), None),
    (CompanyAddressListId, emptyAnswers, previousAddressPage(UpdateMode), None),
    (CompanyPreviousAddressId, emptyAnswers, anyMoreChanges, None),

    (CompanyEmailId, emptyAnswers, anyMoreChanges, None),
    (CompanyPhoneId, uk, anyMoreChanges, None),
    (CompanyPhoneId, nonUk, anyMoreChanges, None)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, routes(), dataDescriber)
    behave like navigatorWithRoutes(navigator, updateRoutes(), dataDescriber, UpdateMode)
  }
}

object RegisterCompanyNavigatorSpec extends OptionValues {
  private def emailPage(mode: Mode): Call = routes.CompanyEmailController.onPageLoad(mode)
  private def phonePage(mode: Mode): Call = routes.CompanyPhoneController.onPageLoad(mode)
  private def sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  private def anyMoreChanges = controllers.register.routes.AnyMoreChangesController.onPageLoad()
  private def confirmPreviousAddressPage = routes.CompanyConfirmPreviousAddressController.onPageLoad()

  private def checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad()

  private def companyNamePage = routes.CompanyNameController.onPageLoad()
  private def companyIsRegisteredNamePage = routes.CompanyIsRegisteredNameController.onPageLoad()

  private def confirmCompanyDetailsPage = routes.ConfirmCompanyDetailsController.onPageLoad()

  private def whatYouWillNeedPage = routes.WhatYouWillNeedController.onPageLoad()

  private def whatYouWillNeedDirectorPage = controllers.register.company.directors.routes.WhatYouWillNeedController.onPageLoad()

  private def hasCRNPage(mode: Mode) = routes.HasCompanyCRNController.onPageLoad(mode)

  private def companyRegistrationNumberPage(mode: Mode) = routes.CompanyRegistrationNumberController.onPageLoad(mode)
  private def hasPayePage = routes.HasCompanyPAYEController.onPageLoad(NormalMode)

  private def hasVatPage = routes.HasCompanyVATController.onPageLoad(NormalMode)

  private def payePage(mode: Mode = NormalMode) = routes.CompanyEnterPAYEController.onPageLoad(mode)

  private def vatPage(mode: Mode = NormalMode) = routes.CompanyEnterVATController.onPageLoad(mode)

  private def companyRegistrationNumberPage = routes.CompanyRegistrationNumberController.onPageLoad(NormalMode)

  private def companyAddressYearsPage(mode: Mode) = routes.CompanyAddressYearsController.onPageLoad(mode)

  private def paPostCodePage(mode: Mode) = routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(mode)

  private def paAddressListPage(mode: Mode) = routes.CompanyAddressListController.onPageLoad(mode)

  private def hasBeenTradingPage(mode: Mode): Call = routes.CompanyTradingOverAYearController.onPageLoad(mode)

  private def previousAddressPage(mode: Mode) = routes.CompanyPreviousAddressController.onPageLoad(mode)

  private def declarationWorkingKnowledgePage(mode: Mode) = controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(mode)

  private def sameContactAddress(mode: Mode) = routes.CompanySameContactAddressController.onPageLoad(mode)

  private def contactAddressPostCode(mode: Mode) = routes.CompanyContactAddressPostCodeLookupController.onPageLoad(mode)

  private def contactAddressList(mode: Mode) = routes.CompanyContactAddressListController.onPageLoad(mode)

  private def contactAddress(mode: Mode) = routes.CompanyContactAddressController.onPageLoad(mode)

  private def addCompanyDirectors(mode: Mode) = routes.AddCompanyDirectorsController.onPageLoad(mode)

  private def nonUkAddress = routes.CompanyRegisteredAddressController.onPageLoad()

  private def reconsiderAreYouInUk = controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(CheckMode)

  private def outsideEuEea = routes.OutsideEuEeaController.onPageLoad()

  private def companyUpdate = routes.CompanyUpdateDetailsController.onPageLoad()

  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(CompanyAddressYearsId)(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers().set(CompanyAddressYearsId)(AddressYears.UnderAYear).asOpt.value
  private val tradingOverAYearUk = UserAnswers(Json.obj()).areYouInUk(true)
    .set(CompanyTradingOverAYearId)(true).asOpt.value
  private val tradingOverAYearNonUk = UserAnswers(Json.obj()).areYouInUk(false)
    .set(CompanyTradingOverAYearId)(true).asOpt.value
  private val tradingUnderAYear = UserAnswers().set(CompanyTradingOverAYearId)(false).asOpt.value
  private val isSameContactAddress = UserAnswers().companySameContactAddress(true)
  private val notSameContactAddressUk = UserAnswers().areYouInUk(true).companySameContactAddress(false)
  private val notSameContactAddressNonUk = UserAnswers().areYouInUk(false).companySameContactAddress(false)
  private lazy val lastPage = UserAnswers(Json.obj())
    .set(LastPageId)(LastPage("GET", "www.test.com")).asOpt.value

  private val hasPAYEYes = UserAnswers().set(HasPAYEId)(value = true).asOpt.value
  private val hasPAYENo = UserAnswers().set(HasPAYEId)(value = false).asOpt.value

  private val hasVATYes = UserAnswers().set(HasVATId)(value = true).asOpt.value
  private val hasVATNo = UserAnswers().set(HasVATId)(value = false).asOpt.value

  private val nonUkEuAddress = UserAnswers().nonUkCompanyAddress(address("AT"))
  private val nonUkButUKAddress = UserAnswers().nonUkCompanyAddress(address("GB"))
  private val nonUkNonEuAddress = UserAnswers().nonUkCompanyAddress(address("AF"))

  protected val uk: UserAnswers = UserAnswers().areYouInUk(true)
  protected val nonUk: UserAnswers = UserAnswers().areYouInUk(false)
  protected val isRegisteredNameTrue: UserAnswers = UserAnswers().isRegisteredName(true)
  protected val isRegisteredNameFalse: UserAnswers = UserAnswers().isRegisteredName(false)

  private def address(countryCode: String) = Address("addressLine1", "addressLine2", Some("addressLine3"), Some("addressLine4"), Some("NE11AA"), countryCode)

  val unlimitedCompany: UserAnswers = UserAnswers(Json.obj())
    .set(BusinessTypeId)(BusinessType.UnlimitedCompany).asOpt.value
  val limitedCompany: UserAnswers = UserAnswers(Json.obj())
    .set(BusinessTypeId)(BusinessType.LimitedCompany).asOpt.value
  val hasDirector: UserAnswers = UserAnswers(Json.obj())
    .set(DirectorNameId(0))(PersonName("first", "last")).asOpt.value
  def hasCRN(b:Boolean): UserAnswers = UserAnswers(Json.obj())
    .set(HasCompanyCRNId)(b).asOpt.value

  private val confirmAddressTrueLimitedCompany = UserAnswers(Json.obj()).set(BusinessTypeId)(BusinessType.LimitedCompany).flatMap(
    _.set(ConfirmPartnershipDetailsId)(true)).asOpt.value
  private val confirmAddressTrueUnlimitedCompany = UserAnswers(Json.obj()).set(BusinessTypeId)(BusinessType.UnlimitedCompany).flatMap(
    _.set(ConfirmPartnershipDetailsId)(true)).asOpt.value

}
