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
import controllers.register.company.routes
import identifiers.register._
import identifiers.register.company._
import identifiers.register.company.directors.DirectorNameId
import identifiers.register.partnership.ConfirmPartnershipDetailsId
import identifiers.{Identifier, UpdateContactAddressId}
import models._
import models.register.BusinessType
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor3
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Navigator, NavigatorBehaviour, UserAnswers}

class RegisterCompanyNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import RegisterCompanyNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[RegisterCompanyNavigator]

  "RegisterCompanyNavigator in NormalMode" must {
    // scalastyle:off method.length
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (BusinessUTRId, uk, companyNamePage),
      (BusinessNameId, uk, companyIsRegisteredNamePage),
      (IsRegisteredNameId, isRegisteredNameTrue.areYouInUk(true), confirmCompanyDetailsPage),
      (IsRegisteredNameId, isRegisteredNameFalse.areYouInUk(true), companyUpdate),

      (ConfirmCompanyAddressId, confirmAddressTrueLimitedCompany, companyRegistrationNumberPage(NormalMode)),
      (ConfirmCompanyAddressId, confirmAddressTrueUnlimitedCompany.areYouInUk(true), hasCRNPage(NormalMode)),

      (HasCompanyCRNId, hasCRN(true).areYouInUk(true), companyRegistrationNumberPage(NormalMode)),
      (HasCompanyCRNId, hasCRN(false).areYouInUk(true), hasPayePage),
      (CompanyRegistrationNumberId, uk, hasPayePage),

      (HasPAYEId, hasPAYEYes.areYouInUk(true), payePage()),
      (HasPAYEId, hasPAYENo.areYouInUk(true), hasVatPage),
      (EnterPAYEId, uk, hasVatPage),

      (HasVATId, hasVATYes.areYouInUk(true), vatPage()),
      (HasVATId, hasVATNo.areYouInUk(true), sameContactAddress(NormalMode)),

      (EnterVATId, uk, sameContactAddress(NormalMode)),

      (CompanySameContactAddressId, isSameContactAddress, companyAddressYearsPage(NormalMode)),
      (CompanySameContactAddressId, notSameContactAddressUk.areYouInUk(true), contactAddressPostCode(NormalMode)),
      (CompanySameContactAddressId, uk, sessionExpiredPage),

      (CompanyContactAddressPostCodeLookupId, uk, contactAddressList(NormalMode)),
      (CompanyContactAddressId, uk, companyAddressYearsPage(NormalMode)),

      (CompanyAddressYearsId, addressYearsOverAYear, emailPage(NormalMode)),
      (CompanyAddressYearsId, addressYearsUnderAYear.areYouInUk(true), hasBeenTradingPage(NormalMode)),
      (CompanyAddressYearsId, uk, sessionExpiredPage),

      (CompanyTradingOverAYearId, tradingOverAYearUk.areYouInUk(true), paPostCodePage(NormalMode)),
      (CompanyTradingOverAYearId, tradingUnderAYear.areYouInUk(true), emailPage(NormalMode)),

      (CompanyPreviousAddressPostCodeLookupId, uk, paAddressListPage(NormalMode)),
      (CompanyPreviousAddressId, uk, emailPage(NormalMode)),

      (CompanyEmailId, uk, phonePage(NormalMode)),
      (CompanyPhoneId, uk, checkYourAnswersPage),

      (CheckYourAnswersId, uk, whatYouWillNeedDirectorPage),
      (CheckYourAnswersId, hasDirector, addCompanyDirectors(NormalMode)),

      (CompanyReviewId, uk, declarationWorkingKnowledgePage(NormalMode)),

      (WhatYouWillNeedId, uk, sameContactAddress(NormalMode))
    )
    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, NormalMode)
  }

  "RegisterCompanyNavigator in CheckMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),

      (HasCompanyCRNId, hasCRN(true), companyRegistrationNumberPage(CheckMode)),
      (HasCompanyCRNId, hasCRN(false), checkYourAnswersPage),
      (CompanyRegistrationNumberId, emptyAnswers, checkYourAnswersPage),

      (HasPAYEId, hasPAYEYes,payePage(CheckMode)),
      (HasPAYEId, hasPAYENo, checkYourAnswersPage),
      (EnterPAYEId, emptyAnswers, checkYourAnswersPage),

      (HasVATId, hasVATYes, vatPage(CheckMode)),
      (HasVATId, hasVATNo, checkYourAnswersPage),

      (EnterVATId, emptyAnswers, checkYourAnswersPage),

      (CompanySameContactAddressId, isSameContactAddress, companyAddressYearsPage(CheckMode)),
      (CompanySameContactAddressId, notSameContactAddressUk, contactAddressPostCode(CheckMode)),
      (CompanySameContactAddressId, notSameContactAddressNonUk, contactAddress(CheckMode)),

      (CompanyContactAddressPostCodeLookupId, emptyAnswers, contactAddressList(CheckMode)),
      (CompanyContactAddressId, emptyAnswers, checkYourAnswersPage),

      (CompanyAddressYearsId, addressYearsOverAYear, checkYourAnswersPage),
      (CompanyAddressYearsId, addressYearsUnderAYear, hasBeenTradingPage(CheckMode)),

      (CompanyTradingOverAYearId, tradingOverAYearUk, paPostCodePage(CheckMode)),
      (CompanyTradingOverAYearId, tradingOverAYearNonUk, previousAddressPage(CheckMode)),
      (CompanyTradingOverAYearId, tradingUnderAYear, checkYourAnswersPage),

      (CompanyPreviousAddressPostCodeLookupId, emptyAnswers, paAddressListPage(CheckMode)),
      (CompanyPreviousAddressId, emptyAnswers, checkYourAnswersPage),

      (CompanyEmailId, emptyAnswers, checkYourAnswersPage),
      (CompanyPhoneId, uk, checkYourAnswersPage),
      (CompanyPhoneId, nonUk, checkYourAnswersPage)
    )
    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, CheckMode)
  }

  "RegisterCompanyNavigator in UpdateMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (CompanyContactAddressPostCodeLookupId, emptyAnswers, contactAddressList(UpdateMode)),
      (CompanyContactAddressId, emptyAnswers, confirmPreviousAddressPage),

      (CompanyAddressYearsId, addressYearsOverAYear, anyMoreChanges),
      (CompanyAddressYearsId, addressYearsUnderAYear, confirmPreviousAddressPage),
      (CompanyAddressYearsId, emptyAnswers, sessionExpiredPage),

      (CompanyPreviousAddressPostCodeLookupId, emptyAnswers, paAddressListPage(UpdateMode)),
      (CompanyPreviousAddressId, emptyAnswers, anyMoreChanges),
      (CompanyPreviousAddressId, updatingContactAddressForRLS, updateContactAddressCYAPage),

      (CompanyConfirmPreviousAddressId, emptyAnswers, sessionExpiredPage),
      (CompanyConfirmPreviousAddressId, samePreviousAddress, anyMoreChanges),
      (CompanyConfirmPreviousAddressId, samePreviousAddressUpdateContactAddress, updateContactAddressCYAPage),
      (CompanyConfirmPreviousAddressId, notSamePreviousAddress, previousPostCodeLookupPage(UpdateMode)),

      (CompanyEmailId, emptyAnswers, anyMoreChanges),
      (CompanyEmailId, updatingContactAddressForRLS, updateContactAddressCYAPage),
      (CompanyPhoneId, emptyAnswers, anyMoreChanges),
      (CompanyPhoneId, updatingContactAddressForRLS, updateContactAddressCYAPage)
    )
    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, UpdateMode)
  }


}

// scalastyle:off number.of.methods
object RegisterCompanyNavigatorSpec extends OptionValues {
  private def emailPage(mode: Mode): Call = routes.CompanyEmailController.onPageLoad(mode)
  private def phonePage(mode: Mode): Call = routes.CompanyPhoneController.onPageLoad(mode)
  private def sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad
  private def anyMoreChanges = controllers.register.routes.AnyMoreChangesController.onPageLoad()
  private def confirmPreviousAddressPage = routes.CompanyConfirmPreviousAddressController.onPageLoad()
  private lazy val updateContactAddressCYAPage:Call = controllers.routes.UpdateContactAddressCYAController.onPageLoad()
  private def checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad()

  private val updatingContactAddressForRLS = UserAnswers(Json.obj()).set(UpdateContactAddressId)(true).asOpt.value

  private def companyNamePage = routes.CompanyNameController.onPageLoad
  private def companyIsRegisteredNamePage = routes.CompanyIsRegisteredNameController.onPageLoad

  private def confirmCompanyDetailsPage = routes.ConfirmCompanyDetailsController.onPageLoad()


  private def whatYouWillNeedDirectorPage = controllers.register.company.directors.routes.WhatYouWillNeedController.onPageLoad()

  private def hasCRNPage(mode: Mode) = routes.HasCompanyCRNController.onPageLoad(mode)

  private def companyRegistrationNumberPage(mode: Mode) = routes.CompanyRegistrationNumberController.onPageLoad(mode)
  private def hasPayePage = routes.HasCompanyPAYEController.onPageLoad(NormalMode)

  private def hasVatPage = routes.HasCompanyVATController.onPageLoad(NormalMode)

  private def payePage(mode: Mode = NormalMode) = routes.CompanyEnterPAYEController.onPageLoad(mode)

  private def vatPage(mode: Mode = NormalMode) = routes.CompanyEnterVATController.onPageLoad(mode)

  private def companyAddressYearsPage(mode: Mode) = routes.CompanyAddressYearsController.onPageLoad(mode)

  private def paPostCodePage(mode: Mode) = routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(mode)

  private def paAddressListPage(mode: Mode) = routes.CompanyAddressListController.onPageLoad(mode)

  private def hasBeenTradingPage(mode: Mode): Call = routes.CompanyTradingOverAYearController.onPageLoad(mode)

  private def previousAddressPage(mode: Mode) = routes.CompanyPreviousAddressController.onPageLoad(mode)

  private def declarationWorkingKnowledgePage(mode: Mode) = controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(mode)

  private def sameContactAddress(mode: Mode) = routes.CompanySameContactAddressController.onPageLoad(mode)

  private def samePreviousAddress = UserAnswers(Json.obj())
    .set(CompanyConfirmPreviousAddressId)(true).asOpt.value

  private def samePreviousAddressUpdateContactAddress = UserAnswers(Json.obj())
    .set(CompanyConfirmPreviousAddressId)(true).asOpt.value
    .set(UpdateContactAddressId)(true).asOpt.value


  private def notSamePreviousAddress = UserAnswers(Json.obj())
    .set(CompanyConfirmPreviousAddressId)(false).asOpt.value

  private def contactAddressPostCode(mode: Mode) = routes.CompanyContactAddressPostCodeLookupController.onPageLoad(mode)

  private def contactAddressList(mode: Mode) = routes.CompanyContactAddressListController.onPageLoad(mode)

  private def contactAddress(mode: Mode) = routes.CompanyContactAddressController.onPageLoad(mode)

  private def addCompanyDirectors(mode: Mode) = routes.AddCompanyDirectorsController.onPageLoad(mode)

  private def companyUpdate = routes.CompanyUpdateDetailsController.onPageLoad()


  protected val uk: UserAnswers = UserAnswers().areYouInUk(true)
  protected val nonUk: UserAnswers = UserAnswers().areYouInUk(false)
  private val addressYearsOverAYear = UserAnswers(uk.json)
    .set(CompanyAddressYearsId)(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = uk.set(CompanyAddressYearsId)(AddressYears.UnderAYear).asOpt.value
  private val tradingOverAYearUk = UserAnswers(Json.obj()).areYouInUk(true)
    .set(CompanyTradingOverAYearId)(true).asOpt.value
  private val tradingOverAYearNonUk = UserAnswers(Json.obj()).areYouInUk(false)
    .set(CompanyTradingOverAYearId)(true).asOpt.value
  private val tradingUnderAYear = UserAnswers().set(CompanyTradingOverAYearId)(false).asOpt.value
  private val isSameContactAddress = UserAnswers(uk.json).companySameContactAddress(true)
  private val notSameContactAddressUk = UserAnswers().areYouInUk(true).companySameContactAddress(false)
  private val notSameContactAddressNonUk = UserAnswers().areYouInUk(false).companySameContactAddress(false)

  private val hasPAYEYes = UserAnswers().set(HasPAYEId)(value = true).asOpt.value
  private val hasPAYENo = UserAnswers().set(HasPAYEId)(value = false).asOpt.value

  private val hasVATYes = UserAnswers().set(HasVATId)(value = true).asOpt.value
  private val hasVATNo = UserAnswers().set(HasVATId)(value = false).asOpt.value

  protected val isRegisteredNameTrue: UserAnswers = UserAnswers().isRegisteredName(true)
  protected val isRegisteredNameFalse: UserAnswers = UserAnswers().isRegisteredName(false)

  val unlimitedCompany: UserAnswers = UserAnswers(Json.obj())
    .set(BusinessTypeId)(BusinessType.UnlimitedCompany).asOpt.value
  val limitedCompany: UserAnswers = UserAnswers(Json.obj())
    .set(BusinessTypeId)(BusinessType.LimitedCompany).asOpt.value
  val hasDirector: UserAnswers = UserAnswers(uk.json)
    .set(DirectorNameId(0))(PersonName("first", "last")).asOpt.value
  def hasCRN(b:Boolean): UserAnswers = UserAnswers(Json.obj())
    .set(HasCompanyCRNId)(b).asOpt.value

  private val confirmAddressTrueLimitedCompany = uk.set(BusinessTypeId)(BusinessType.LimitedCompany).flatMap(
    _.set(ConfirmPartnershipDetailsId)(true)).asOpt.value
  private val confirmAddressTrueUnlimitedCompany = UserAnswers(Json.obj()).set(BusinessTypeId)(BusinessType.UnlimitedCompany).flatMap(
    _.set(ConfirmPartnershipDetailsId)(true)).asOpt.value

  private def previousPostCodeLookupPage(mode: Mode) = routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(mode)

}
