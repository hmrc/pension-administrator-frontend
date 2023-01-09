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
import controllers.register.administratorPartnership.contactDetails.routes._
import controllers.register.administratorPartnership.partnershipDetails.routes._
import controllers.register.administratorPartnership.routes._
import identifiers.register._
import identifiers.register.partnership._
import identifiers.register.partnership.partners.PartnerNameId
import identifiers.{Identifier, UpdateContactAddressId}
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor3
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Navigator, NavigatorBehaviour, UserAnswers}

class RegisterPartnershipNavigatorV2Spec extends SpecBase with NavigatorBehaviour {

  import RegisterPartnershipNavigatorV2Spec._

  val navigator: Navigator = injector.instanceOf[RegisterPartnershipNavigatorV2]

  "RegisterPartnershipNavigatorV2 in NormalMode" must {
    // scalastyle:off method.length
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (BusinessUTRId, emptyAnswers, partnershipNamePage),
      (BusinessNameId, uk, partnershipIsRegisteredNamePage),
      (IsRegisteredNameId, isRegisteredNameTrue, confirmPartnershipDetailsPage),
      (IsRegisteredNameId, isRegisteredNameFalse, companyUpdateDetailsPage),

      (HasPAYEId, hasPAYEYes, payePage(NormalMode)),
      (HasPAYEId, hasPAYENo, hasVatPage),
      (EnterPAYEId, emptyAnswers, hasVatPage),

      (HasVATId, hasVatYes, vatPage(NormalMode)),
      (HasVATId, hasVatNo, checkYourAnswersPartnershipDetailsPage),

      (EnterVATId, emptyAnswers, checkYourAnswersPartnershipDetailsPage),

      (PartnershipSameContactAddressId, isSameContactAddress, partnershipAddressYearsPage(NormalMode)),
      (PartnershipSameContactAddressId, notSameContactAddressNonUk, partnershipContactAddressPage(NormalMode)),
      (PartnershipSameContactAddressId, notSameContactAddressUk, partnershipContactPostcodePage(NormalMode)),
      (PartnershipSameContactAddressId, emptyAnswers, sessionExpiredPage),

      (PartnershipContactAddressPostCodeLookupId, emptyAnswers, partnerhshipContactAddressListPage(NormalMode)),
      (PartnershipContactAddressId, emptyAnswers, partnershipAddressYearsPage(NormalMode)),

      (PartnershipAddressYearsId, addressYearsUnderAYear, partnershipTradingOverAYearPage(NormalMode)),
      (PartnershipAddressYearsId, addressYearsOverAYear, partnershipEmailPage),
      (PartnershipAddressYearsId, emptyAnswers, sessionExpiredPage),

      (PartnershipTradingOverAYearId, tradingOverAYearUk, partnershipPreviousPostCodePage(NormalMode)),
      (PartnershipTradingOverAYearId, tradingOverAYearNonUk, partnershipPreviousAddressPage(NormalMode)),
      (PartnershipTradingOverAYearId, tradingUnderAYear, partnershipEmailPage),
      (PartnershipTradingOverAYearId, emptyAnswers, sessionExpiredPage),

      (PartnershipPreviousAddressPostCodeLookupId, emptyAnswers, partnershipPreviousAddressListPage(NormalMode)),

      (PartnershipPreviousAddressId, emptyAnswers, partnershipEmailPage),


      (PartnershipEmailId, emptyAnswers, partnershipPhonePage),
      (PartnershipPhoneId, emptyAnswers, checkYourAnswersContactDetailsPage),

      (CheckYourAnswersId, emptyAnswers, wynPage),
      (CheckYourAnswersId, hasPartner, addPartnersPage(NormalMode)),

      (PartnershipReviewId, emptyAnswers, declarationWorkingKnowledgePage(NormalMode)),

      (WhatYouWillNeedId, emptyAnswers, partnershipSameContactAddressPage),

      (WhatYouWillNeedIdV2, emptyAnswers, partnershipHasPayePage),

      (PartnershipAddressId, ukAddress, reconsiderAreYouInUk),
      (PartnershipAddressId, euEea, partnershipDetailsWynPage),
      (PartnershipAddressId, restOfTheWorld, outsideEuEea),
      (PartnershipAddressId, emptyAnswers, sessionExpiredPage)
    )

    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, NormalMode)
  }

  "RegisterPartnershipNavigator in CheckMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),

      (HasPAYEId, hasPAYEYes, payePage(CheckMode)),
      (HasPAYEId, hasPAYENo, checkYourAnswersPartnershipDetailsPage),
      (EnterPAYEId, emptyAnswers, checkYourAnswersPartnershipDetailsPage),

      (HasVATId, hasVatYes, vatPage(CheckMode)),
      (HasVATId, hasVatNo, checkYourAnswersPartnershipDetailsPage),

      (EnterVATId, emptyAnswers, checkYourAnswersPartnershipDetailsPage),

      (PartnershipSameContactAddressId, isSameContactAddress, partnershipAddressYearsPage(CheckMode)),
      (PartnershipSameContactAddressId, notSameContactAddressNonUk, partnershipContactAddressPage(CheckMode)),
      (PartnershipSameContactAddressId, notSameContactAddressUk, partnershipContactPostcodePage(CheckMode)),
      (PartnershipSameContactAddressId, emptyAnswers, sessionExpiredPage),


      (PartnershipContactAddressPostCodeLookupId, emptyAnswers, partnerhshipContactAddressListPage(CheckMode)),
      (PartnershipContactAddressId, emptyAnswers, checkYourAnswersContactDetailsPage),

      (PartnershipAddressYearsId, addressYearsUnderAYear, partnershipTradingOverAYearPage(CheckMode)),
      (PartnershipAddressYearsId, addressYearsOverAYear, checkYourAnswersContactDetailsPage),
      (PartnershipAddressYearsId, emptyAnswers, sessionExpiredPage),


      (PartnershipTradingOverAYearId, tradingOverAYearUk, partnershipPreviousPostCodePage(CheckMode)),
      (PartnershipTradingOverAYearId, tradingOverAYearNonUk, partnershipPreviousAddressPage(CheckMode)),
      (PartnershipTradingOverAYearId, tradingUnderAYear, checkYourAnswersContactDetailsPage),
      (PartnershipTradingOverAYearId, emptyAnswers, sessionExpiredPage),


      (PartnershipPreviousAddressPostCodeLookupId, emptyAnswers, partnershipPreviousAddressListPage(CheckMode)),
      (PartnershipPreviousAddressId, emptyAnswers, checkYourAnswersContactDetailsPage),

      (PartnershipEmailId, emptyAnswers, checkYourAnswersContactDetailsPage),
      (PartnershipPhoneId, emptyAnswers, checkYourAnswersContactDetailsPage),

    )

    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, CheckMode)
  }

  "RegisterPartnershipNavigator in UpdateMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (PartnershipContactAddressPostCodeLookupId, emptyAnswers, partnerhshipContactAddressListPage(UpdateMode)),
      (PartnershipContactAddressId, emptyAnswers, confirmPreviousAddressPage),

      (PartnershipAddressYearsId, addressYearsUnderAYear, confirmPreviousAddressPage),
      (PartnershipAddressYearsId, addressYearsOverAYear, anyMoreChanges),
      (PartnershipAddressYearsId, emptyAnswers, sessionExpiredPage),

      (PartnershipConfirmPreviousAddressId, varianceConfirmPreviousAddressNo, partnershipPreviousPostCodePage(UpdateMode)),
      (PartnershipConfirmPreviousAddressId, varianceConfirmPreviousAddressYes, anyMoreChanges),
      (PartnershipConfirmPreviousAddressId, emptyAnswers, sessionExpiredPage),

      (PartnershipPreviousAddressPostCodeLookupId, emptyAnswers, partnershipPreviousAddressListPage(UpdateMode)),


      (PartnershipPreviousAddressId, updatingContactAddressForRLS, updateContactAddressCYAPage),
      (PartnershipPreviousAddressId, emptyAnswers, anyMoreChanges),


      (PartnershipEmailId, emptyAnswers, anyMoreChanges),
      (PartnershipPhoneId, emptyAnswers, anyMoreChanges)

    )

    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, UpdateMode)
  }

}


object RegisterPartnershipNavigatorV2Spec extends OptionValues {

  private lazy val updatingContactAddressForRLS = UserAnswers(Json.obj()).set(UpdateContactAddressId)(true).asOpt.value

  private lazy val updateContactAddressCYAPage: Call = controllers.routes.UpdateContactAddressCYAController.onPageLoad()
  private lazy val sessionExpiredPage: Call = controllers.routes.SessionExpiredController.onPageLoad

  private lazy val confirmPartnershipDetailsPage: Call = ConfirmPartnershipDetailsController.onPageLoad()

  private lazy val partnershipNamePage = PartnershipNameController.onPageLoad

  private lazy val companyUpdateDetailsPage = controllers.register.company.routes.CompanyUpdateDetailsController.onPageLoad()

  private lazy val partnershipIsRegisteredNamePage = PartnershipIsRegisteredNameController.onPageLoad

  private lazy val partnershipSameContactAddressPage: Call = PartnershipSameContactAddressController.onPageLoad(NormalMode)

  private lazy val checkYourAnswersContactDetailsPage: Call = controllers.register.administratorPartnership.contactDetails.routes.CheckYourAnswersController.onPageLoad()

  private lazy val checkYourAnswersPartnershipDetailsPage: Call = controllers.register.administratorPartnership.partnershipDetails.routes.CheckYourAnswersController.onPageLoad()

  private lazy val partnershipEmailPage: Call = PartnershipEmailController.onPageLoad(NormalMode)

  private lazy val partnershipPhonePage: Call = PartnershipPhoneController.onPageLoad(NormalMode)

  private lazy val hasVatPage: Call = HasPartnershipVATController.onPageLoad(NormalMode)

  private def vatPage(mode: Mode): Call = PartnershipEnterVATController.onPageLoad(mode)

  private def partnershipTradingOverAYearPage(mode: Mode): Call = PartnershipTradingOverAYearController.onPageLoad(mode)

  private lazy val wynPage: Call = controllers.register.partnership.partners.routes.WhatYouWillNeedController.onPageLoad()

  private lazy val partnershipDetailsWynPage = controllers.register.administratorPartnership.partnershipDetails.routes.WhatYouWillNeedController.onPageLoad()

  private def addPartnersPage(mode: Mode): Call = AddPartnerController.onPageLoad(NormalMode)

  private def partnershipAddressYearsPage(mode: Mode): Call = PartnershipAddressYearsController.onPageLoad(mode)

  private def partnershipContactPostcodePage(mode: Mode): Call = PartnershipContactAddressPostCodeLookupController.onPageLoad(mode)

  private def partnerhshipContactAddressListPage(mode: Mode): Call = PartnershipContactAddressListController.onPageLoad(mode)

  private def partnershipContactAddressPage(mode: Mode): Call = PartnershipContactAddressController.onPageLoad(mode)

  private def partnershipPreviousPostCodePage(mode: Mode): Call = PartnershipPreviousAddressPostCodeLookupController.onPageLoad(mode)

  private def partnershipPreviousAddressListPage(mode: Mode): Call = PartnershipPreviousAddressListController.onPageLoad(mode)

  private lazy val confirmPreviousAddressPage: Call = PartnershipConfirmPreviousAddressController.onPageLoad()

  private def partnershipPreviousAddressPage(mode: Mode): Call = PartnershipPreviousAddressController.onPageLoad(mode)

  private def declarationWorkingKnowledgePage(mode: Mode): Call = controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(mode)

  private def anyMoreChanges: Call = controllers.register.routes.AnyMoreChangesController.onPageLoad()

  private lazy val partnershipHasPayePage: Call = HasPartnershipPAYEController.onPageLoad(NormalMode)

  private def payePage(mode: Mode): Call = PartnershipEnterPAYEController.onPageLoad(mode)

  private lazy val reconsiderAreYouInUk: Call = controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(CheckMode)

  private lazy val outsideEuEea: Call = OutsideEuEeaController.onPageLoad()


  protected val ukAddress: UserAnswers = UserAnswers().set(PartnershipAddressId)(address("GB")).asOpt.get
  protected val euEea: UserAnswers = UserAnswers().set(PartnershipAddressId)(address("AT")).asOpt.get
  protected val restOfTheWorld: UserAnswers = UserAnswers().set(PartnershipAddressId)(address("AF")).asOpt.get

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

  private def address(countryCode: String) = Address("addressLine1", "addressLine2", Some("addressLine3"), Some("addressLine4"), Some("NE11AA"), countryCode).toTolerantAddress

}