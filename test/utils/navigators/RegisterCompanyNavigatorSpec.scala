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
import identifiers.register.company.{PhoneId, _}
import identifiers.register.partnership.ConfirmPartnershipDetailsId
import identifiers.register._
import identifiers.{Identifier, LastPageId}
import models._
import models.register.BusinessType
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, NavigatorBehaviour, UserAnswers}

class RegisterCompanyNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import RegisterCompanyNavigatorSpec._

  def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val navigator = new RegisterCompanyNavigator(FakeUserAnswersCacheConnector, countryOptions, frontendAppConfig)

  private def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),
    (BusinessUTRId, emptyAnswers, companyNamePage, false, None, false),
    (BusinessNameId, uk, companyIsRegisteredNamePage, false, None, false),
    (BusinessNameId, nonUk, nonUkAddress, false, None, false),
    (IsRegisteredNameId, isRegisteredNameTrue, confirmCompanyDetailsPage, false, None, false),
    (IsRegisteredNameId, isRegisteredNameFalse, companyUpdate, false, None, false),

    (ConfirmCompanyAddressId, confirmAddressTrueLimitedCompany, companyRegistrationNumberPage(NormalMode), false, None, false),
    (ConfirmCompanyAddressId, confirmAddressTrueUnlimitedCompany, hasCRNPage(NormalMode), false, None, false),

    (HasCompanyCRNId, hasCRN(true), companyRegistrationNumberPage(NormalMode), false, Some(companyRegistrationNumberPage(CheckMode)), false),
    (HasCompanyCRNId, hasCRN(false), hasPayePage, false, Some(checkYourAnswersPage), false),
    (CompanyRegistrationNumberId, emptyAnswers, hasPayePage, true, Some(checkYourAnswersPage), true),

    (HasPAYEId, hasPAYEYes, payePage(), true, Some(payePage(CheckMode)), true),
    (HasPAYEId, hasPAYENo, hasVatPage, true, Some(checkYourAnswersPage), true),
    (EnterPAYEId, emptyAnswers, hasVatPage, true, Some(checkYourAnswersPage), true),

    (HasVATId, hasVATYes, vatPage(), true, Some(vatPage(CheckMode)), true),
    (HasVATId, hasVATNo, sameContactAddress(NormalMode), true, Some(checkYourAnswersPage), true),

    (EnterVATId, emptyAnswers, sameContactAddress(NormalMode), true, Some(checkYourAnswersPage), true),

    (CompanySameContactAddressId, isSameContactAddress, companyAddressYearsPage(NormalMode), true, Some(companyAddressYearsPage(CheckMode)), true),
    (CompanySameContactAddressId, notSameContactAddressUk, contactAddressPostCode(NormalMode), true, Some(contactAddressPostCode(CheckMode)), true),
    (CompanySameContactAddressId, notSameContactAddressNonUk, contactAddress(NormalMode), true, Some(contactAddress(CheckMode)), true),
    (CompanySameContactAddressId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),

    (CompanyContactAddressPostCodeLookupId, emptyAnswers, contactAddressList(NormalMode), true, Some(contactAddressList(CheckMode)), true),
    (CompanyContactAddressListId, emptyAnswers, contactAddress(NormalMode), true, Some(contactAddress(CheckMode)), true),
    (CompanyContactAddressId, emptyAnswers, companyAddressYearsPage(NormalMode), true, Some(companyAddressYearsPage(CheckMode)), true),

    (CompanyAddressYearsId, addressYearsOverAYear, contactDetailsPage(NormalMode), true, Some(checkYourAnswersPage), true),
    (CompanyAddressYearsId, addressYearsUnderAYearUk, paPostCodePage(NormalMode), true, Some(paPostCodePage(CheckMode)), true),
    (CompanyAddressYearsId, addressYearsUnderAYearNonUk, previousAddressPage(NormalMode), true, Some(previousAddressPage(CheckMode)), true),
    (CompanyAddressYearsId, emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),

    (CompanyPreviousAddressPostCodeLookupId, emptyAnswers, paAddressListPage(NormalMode), true, Some(paAddressListPage(CheckMode)), true),
    (CompanyAddressListId, emptyAnswers, previousAddressPage(NormalMode), true, Some(previousAddressPage(CheckMode)), true),
    (CompanyPreviousAddressId, emptyAnswers, emailPage(NormalMode), true, Some(checkYourAnswersPage), true),

    (EmailId, emptyAnswers, phonePage(NormalMode), true, Some(checkYourAnswersPage), true),
    (PhoneId, uk, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),
    (PhoneId, nonUk, checkYourAnswersPage, true, Some(checkYourAnswersPage), true),

    (CheckYourAnswersId, emptyAnswers, addCompanyDirectors(NormalMode), true, None, false),

    (CompanyReviewId, emptyAnswers, declarationPage, true, None, false),

    (CompanyAddressId, nonUkEuAddress, whatYouWillNeedPage, false, None, false),
    (CompanyAddressId, nonUkButUKAddress, reconsiderAreYouInUk, false, None, false),
    (CompanyAddressId, nonUkNonEuAddress, outsideEuEea, false, None, false),

    (WhatYouWillNeedId, emptyAnswers, sameContactAddress(NormalMode), true, None, false)
  )

  private def updateRoutes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save (NM)", "Next Page (Check Mode)", "Save (CM)"),

    (CompanyContactAddressPostCodeLookupId, emptyAnswers, contactAddressList(UpdateMode), true, None, true),
    (CompanyContactAddressListId, emptyAnswers, contactAddress(UpdateMode), true, None, true),
    (CompanyContactAddressId, emptyAnswers, companyAddressYearsPage(UpdateMode), true, None, true),

    (CompanyAddressYearsId, addressYearsOverAYear, anyMoreChanges, false, None, true),
    (CompanyAddressYearsId, addressYearsUnderAYearUk, confirmPreviousAddressPage, true, None, true),
    (CompanyAddressYearsId, emptyAnswers, sessionExpiredPage, false, None, false),

    (CompanyPreviousAddressPostCodeLookupId, emptyAnswers, paAddressListPage(UpdateMode), true, None, true),
    (CompanyAddressListId, emptyAnswers, previousAddressPage(UpdateMode), true, None, true),
    (CompanyPreviousAddressId, emptyAnswers, anyMoreChanges, false, None, true),

    (EmailId, emptyAnswers, anyMoreChanges, false, None, true),
    (PhoneId, uk, anyMoreChanges, false, None, true),
    (PhoneId, nonUk, anyMoreChanges, false, None, true)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes(), dataDescriber, UpdateMode)
  }
}

object RegisterCompanyNavigatorSpec extends OptionValues {
  private def emailPage(mode: Mode): Call = routes.EmailController.onPageLoad(mode)
  private def phonePage(mode: Mode): Call = routes.PhoneController.onPageLoad(mode)
  private def sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  private def anyMoreChanges = controllers.register.routes.AnyMoreChangesController.onPageLoad()
  private def confirmPreviousAddressPage = routes.CompanyConfirmPreviousAddressController.onPageLoad()

  private def checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad()

  private def companyNamePage = routes.CompanyNameController.onPageLoad()
  private def companyUTRPage = routes.CompanyUTRController.onPageLoad()
  private def companyIsRegisteredNamePage = routes.CompanyIsRegisteredNameController.onPageLoad()

  private def confirmCompanyDetailsPage = routes.ConfirmCompanyDetailsController.onPageLoad()

  private def whatYouWillNeedPage = routes.WhatYouWillNeedController.onPageLoad()

  private def hasCRNPage(mode: Mode) = routes.HasCompanyCRNController.onPageLoad(mode)

  private def companyRegistrationNumberPage(mode: Mode) = routes.CompanyRegistrationNumberController.onPageLoad(mode)
  private def hasPayePage = routes.HasCompanyPAYEController.onPageLoad(NormalMode)

  private def hasVatPage = routes.HasCompanyVATController.onPageLoad(NormalMode)

  private def payePage(mode: Mode = NormalMode) = routes.CompanyEnterPAYEController.onPageLoad(mode)

  private def vatPage(mode: Mode = NormalMode) = routes.CompanyEnterVATController.onPageLoad(mode)

  private def companyRegistrationNumberPage = routes.CompanyRegistrationNumberController.onPageLoad(NormalMode)

  private def companyAddressYearsPage(mode: Mode) = routes.CompanyAddressYearsController.onPageLoad(mode)

  private def contactDetailsPage(mode: Mode) = routes.ContactDetailsController.onPageLoad(mode)

  private def paPostCodePage(mode: Mode) = routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(mode)

  private def paAddressListPage(mode: Mode) = routes.CompanyAddressListController.onPageLoad(mode)

  private def previousAddressPage(mode: Mode) = routes.CompanyPreviousAddressController.onPageLoad(mode)

  private def declarationPage = controllers.register.routes.DeclarationController.onPageLoad()

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
  private val addressYearsUnderAYearUk = UserAnswers(Json.obj()).areYouInUk(true)
    .set(CompanyAddressYearsId)(AddressYears.UnderAYear).asOpt.value
  private val addressYearsUnderAYearNonUk = UserAnswers(Json.obj()).areYouInUk(false)
    .set(CompanyAddressYearsId)(AddressYears.UnderAYear).asOpt.value
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
  def hasCRN(b:Boolean): UserAnswers = UserAnswers(Json.obj())
    .set(HasCompanyCRNId)(b).asOpt.value

  private val confirmAddressTrueLimitedCompany = UserAnswers(Json.obj()).set(BusinessTypeId)(BusinessType.LimitedCompany).flatMap(
    _.set(ConfirmPartnershipDetailsId)(true)).asOpt.value
  private val confirmAddressTrueUnlimitedCompany = UserAnswers(Json.obj()).set(BusinessTypeId)(BusinessType.UnlimitedCompany).flatMap(
    _.set(ConfirmPartnershipDetailsId)(true)).asOpt.value

}
