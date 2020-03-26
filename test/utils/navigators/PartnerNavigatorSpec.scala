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
import controllers.register.partnership.partners.routes
import identifiers.Identifier
import identifiers.register.partnership.partners._
import identifiers.register.partnership.{AddPartnersId, MoreThanTenPartnersId}
import models.Mode._
import models._
import models.requests.IdentifiedRequest
import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.prop.{TableFor3, TableFor4}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Navigator, NavigatorBehaviour, UserAnswers}

class PartnerNavigatorSpec extends SpecBase with MockitoSugar with NavigatorBehaviour {

  import PartnerNavigatorSpec._
  
  val navigator: Navigator = injector.instanceOf[PartnerNavigator]

  "PartnerNavigator in NormalMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (AddPartnersId, addPartnersMoreThan10, moreThanTenPartnersPage(NormalMode)),
      (AddPartnersId, addPartnersTrue, partnerDetailsPage(NormalMode)),
      (PartnerNameId(0), emptyAnswers, partnerDOBPage(NormalMode)),
      (PartnerDOBId(0), emptyAnswers, partnerHasNinoPage(NormalMode)),
      (HasPartnerNINOId(0), hasNinoYes, partnerEnterNinoPage(NormalMode)),
      (HasPartnerNINOId(0), hasNinoNo, partnerNoNinoPage(NormalMode)),
      (HasPartnerUTRId(0), hasUtrYes, partnerEnterUtrPage(NormalMode)),
      (HasPartnerUTRId(0), hasUtrNo, partnerNoUtrReasonPage(NormalMode)),
      (PartnerAddressPostCodeLookupId(0), emptyAnswers, addressListPage(NormalMode)),
      (PartnerAddressYearsId(0), addressYearsOverAYear, partnerEmailPage(NormalMode)),
      (PartnerAddressYearsId(0), addressYearsUnderAYear, paPostCodePage(NormalMode)),
      (PartnerAddressYearsId(0), defaultAnswers, sessionExpiredPage),
      (PartnerPreviousAddressPostCodeLookupId(0), emptyAnswers, paAddressListPage(NormalMode)),
      (PartnerPreviousAddressId(0), defaultAnswers, partnerEmailPage(NormalMode)),
      (PartnerEmailId(0), defaultAnswers, partnerPhonePage(NormalMode)),
      (PartnerPhoneId(0), defaultAnswers, checkYourAnswersPage(NormalMode)),
      (CheckYourAnswersId, only1Partner, tellUsAboutAnotherPartnerPage(NormalMode)),
      (CheckYourAnswersId, twoPartners, addPartnersPage(NormalMode)),
      (AddPartnersId, addPartnersFalse, tellUsAboutAnotherPartnerPage(NormalMode)),
      (AddPartnersId, addPartnersFalseMoreThan1, partnershipReviewPage(NormalMode)),
      (PartnerEnterNINOId(0), emptyAnswers, partnerHasUtrPage(NormalMode)),
      (PartnerNoNINOReasonId(0), emptyAnswers, partnerHasUtrPage(NormalMode)),
      (PartnerEnterUTRId(0), emptyAnswers, addressPostCodePage(NormalMode)),
      (PartnerNoUTRReasonId(0), emptyAnswers, addressPostCodePage(NormalMode)),
      (PartnerAddressId(0), emptyAnswers, partnerAddressYearsPage(NormalMode)),
      (MoreThanTenPartnersId, emptyAnswers, partnershipReviewPage(NormalMode))

    )
    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, NormalMode)
  }

  "PartnerNavigator in CheckMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (PartnerNameId(0), emptyAnswers, checkYourAnswersPage(NormalMode)),
      (PartnerDOBId(0), emptyAnswers, checkYourAnswersPage(NormalMode)),
      (HasPartnerNINOId(0), hasNinoYes, partnerEnterNinoPage(CheckMode)),
      (HasPartnerNINOId(0), hasNinoNo, partnerNoNinoPage(CheckMode)),
      (HasPartnerUTRId(0), hasUtrYes, partnerEnterUtrPage(CheckMode)),
      (HasPartnerUTRId(0), hasUtrNo, partnerNoUtrReasonPage(CheckMode)),
      (PartnerAddressYearsId(0), addressYearsOverAYear, checkYourAnswersPage(NormalMode)),
      (PartnerAddressYearsId(0), addressYearsUnderAYear, paPostCodePage(CheckMode)),
      (PartnerAddressYearsId(0), defaultAnswers, sessionExpiredPage),
      (PartnerPreviousAddressId(0), defaultAnswers, checkYourAnswersPage(NormalMode)),
      (PartnerEmailId(0), defaultAnswers, checkYourAnswersPage(NormalMode)),
      (PartnerPhoneId(0), defaultAnswers, checkYourAnswersPage(NormalMode)),
      (PartnerEnterNINOId(0), emptyAnswers, checkYourAnswersPage(NormalMode)),
      (PartnerNoNINOReasonId(0), emptyAnswers, checkYourAnswersPage(NormalMode)),
      (PartnerEnterUTRId(0), emptyAnswers, checkYourAnswersPage(NormalMode)),
      (PartnerNoUTRReasonId(0), emptyAnswers, checkYourAnswersPage(NormalMode)),
      (PartnerAddressId(0), emptyAnswers, checkYourAnswersPage(NormalMode))
    )
    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, CheckMode)
  }

  "PartnerNavigator in UpdateMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (AddPartnersId, addPartnersMoreThan10, moreThanTenPartnersPage(UpdateMode)),
      (AddPartnersId, addPartnersTrue, partnerDetailsPage(UpdateMode)),
      (PartnerNameId(0), emptyAnswers, partnerDOBPage(UpdateMode)),
      (PartnerDOBId(0), emptyAnswers, partnerHasNinoPage(UpdateMode)),
      (HasPartnerNINOId(0), hasNinoYes, partnerEnterNinoPage(UpdateMode)),
      (HasPartnerNINOId(0), hasNinoNo, partnerNoNinoPage(UpdateMode)),
      (HasPartnerUTRId(0), hasUtrYes, partnerEnterUtrPage(UpdateMode)),
      (HasPartnerUTRId(0), hasUtrNo, partnerNoUtrReasonPage(UpdateMode)),
      (PartnerAddressPostCodeLookupId(0), emptyAnswers, addressListPage(UpdateMode)),
      (PartnerAddressYearsId(0), addressYearsOverAYear, partnerEmailPage(UpdateMode)),
      (PartnerAddressYearsId(0), addressYearsUnderAYear, paPostCodePage(UpdateMode)),
      (PartnerAddressYearsId(0), defaultAnswers, sessionExpiredPage),
      (PartnerPreviousAddressPostCodeLookupId(0), emptyAnswers, paAddressListPage(UpdateMode)),
      (PartnerPreviousAddressId(0), defaultAnswers, partnerEmailPage(UpdateMode)),
      (PartnerEmailId(0), defaultAnswers, partnerPhonePage(UpdateMode)),
      (PartnerPhoneId(0), defaultAnswers, checkYourAnswersPage(UpdateMode)),
      (CheckYourAnswersId, only1Partner, tellUsAboutAnotherPartnerPage(UpdateMode)),
      (CheckYourAnswersId, twoPartners, addPartnersPage(UpdateMode)),
      (AddPartnersId, addPartnersFalse, anyMoreChangesPage),
      (PartnerEnterNINOId(0), defaultAnswers, partnerHasUtrPage(UpdateMode)),
      (PartnerEnterNINOId(0), existingPartnerInUpdate(0), anyMoreChangesPage),
      (PartnerNoNINOReasonId(0), defaultAnswers, partnerHasUtrPage(UpdateMode)),
      (PartnerNoNINOReasonId(0), existingPartnerInUpdate(0), anyMoreChangesPage),
      (PartnerEnterUTRId(0), defaultAnswers, addressPostCodePage(UpdateMode)),
      (PartnerEnterUTRId(0), existingPartnerInUpdate(0), anyMoreChangesPage),
      (MoreThanTenPartnersId, emptyAnswers, anyMoreChangesPage),
      (PartnerAddressId(0), defaultAnswers, partnerAddressYearsPage(UpdateMode)),
      (PartnerAddressId(0), existingPartnerInUpdate(0), confirmPreviousAddress),
      (PartnerAddressYearsId(0), addressYearsOverAYearExistingPartner, anyMoreChangesPage),
      (PartnerAddressYearsId(0), addressYearsUnderAYearExistingPartner, confirmPreviousAddress),
      (PartnerConfirmPreviousAddressId(0), confirmPreviousAddressSame(0), anyMoreChangesPage),
      (PartnerConfirmPreviousAddressId(0), confirmPreviousAddressNotSame(0), paPostCodePage(UpdateMode)),
      (PartnerPreviousAddressId(0), existingPartnerInUpdate(0), anyMoreChangesPage),
      (PartnerEmailId(0), existingPartnerInUpdate(0), anyMoreChangesPage),
      (PartnerPhoneId(0), existingPartnerInUpdate(0), anyMoreChangesPage)
    )
    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, UpdateMode)
  }

  "PartnerNavigator in CheckUpdateMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (PartnerNameId(0), emptyAnswers, checkYourAnswersPage(UpdateMode)),
      (PartnerDOBId(0), emptyAnswers, checkYourAnswersPage(UpdateMode)),
      (HasPartnerNINOId(0), hasNinoYes, partnerEnterNinoPage(CheckUpdateMode)),
      (HasPartnerNINOId(0), hasNinoNo, partnerNoNinoPage(CheckUpdateMode)),
      (HasPartnerUTRId(0), hasUtrYes, partnerEnterUtrPage(CheckUpdateMode)),
      (HasPartnerUTRId(0), hasUtrNo, partnerNoUtrReasonPage(CheckUpdateMode)),
      (PartnerAddressYearsId(0), addressYearsOverAYear, checkYourAnswersPage(UpdateMode)),
      (PartnerAddressYearsId(0), addressYearsUnderAYear, paPostCodePage(CheckUpdateMode)),
      (PartnerAddressYearsId(0), defaultAnswers, sessionExpiredPage),
      (PartnerPreviousAddressId(0), defaultAnswers, checkYourAnswersPage(UpdateMode)),
      (PartnerEmailId(0), defaultAnswers, checkYourAnswersPage(UpdateMode)),
      (PartnerPhoneId(0), defaultAnswers, checkYourAnswersPage(UpdateMode))

    )
    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, CheckUpdateMode)
  }
}

object PartnerNavigatorSpec extends OptionValues {

  private lazy val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  private lazy val anyMoreChangesPage = controllers.register.routes.AnyMoreChangesController.onPageLoad()
  private lazy val confirmPreviousAddress = routes.PartnerConfirmPreviousAddressController.onPageLoad(0)

  private def checkYourAnswersPage(mode: Mode) = routes.CheckYourAnswersController.onPageLoad(0, mode)

  private def partnershipReviewPage(mode: Mode) = controllers.register.partnership.routes.PartnershipReviewController.onPageLoad()

  private def tellUsAboutAnotherPartnerPage(mode: Mode) = controllers.register.partnership.routes.TellUsAboutAnotherPartnerController.onPageLoad(mode)

  private def partnerDOBPage(mode: Mode) = routes.PartnerDOBController.onPageLoad(mode, 0)

  private def partnerAddressYearsPage(mode: Mode) = routes.PartnerAddressYearsController.onPageLoad(mode, 0)

  private def addPartnersPage(mode: Mode) = controllers.register.partnership.routes.AddPartnerController.onPageLoad(mode)

  private def moreThanTenPartnersPage(mode: Mode) = controllers.register.partnership.routes.MoreThanTenPartnersController.onPageLoad(mode)

  private def partnerDetailsPage(mode: Mode) = routes.PartnerNameController.onPageLoad(mode, 0)

  private def partnerHasNinoPage(mode: Mode): Call = routes.HasPartnerNINOController.onPageLoad(mode, 0)

  private def partnerEnterNinoPage(mode: Mode): Call = routes.PartnerEnterNINOController.onPageLoad(mode, 0)

  private def partnerNoNinoPage(mode: Mode): Call = routes.PartnerNoNINOReasonController.onPageLoad(mode, 0)

  private def partnerPhonePage(mode: Mode): Call = routes.PartnerPhoneController.onPageLoad(mode, 0)

  private def partnerEmailPage(mode: Mode): Call = routes.PartnerEmailController.onPageLoad(mode, 0)

  def paPostCodePage(mode: Mode): Call = routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, 0)

  def paAddressListPage(mode: Mode): Call = routes.PartnerPreviousAddressListController.onPageLoad(mode, 0)

  def previousAddressPage(mode: Mode): Call = routes.PartnerPreviousAddressController.onPageLoad(mode, 0)

  def addressPostCodePage(mode: Mode): Call = routes.PartnerAddressPostCodeLookupController.onPageLoad(mode, 0)

  def addressListPage(mode: Mode): Call = routes.PartnerAddressListController.onPageLoad(mode, 0)

  def addressPage(mode: Mode): Call = routes.PartnerAddressController.onPageLoad(mode, 0)

  def partnerHasUtrPage(mode: Mode): Call = routes.HasPartnerUTRController.onPageLoad(mode, 0)
  def partnerEnterUtrPage(mode: Mode): Call = routes.PartnerEnterUTRController.onPageLoad(mode, 0)
  def partnerNoUtrReasonPage(mode: Mode): Call = routes.PartnerNoUTRReasonController.onPageLoad(mode, 0)

  private def partner(index: Int) =
    PersonName(s"testFirstName$index", s"testLastName$index", isDeleted = (index % 2 == 0), isNew = true)

  private def createPartners(n: Int) = {
    def partner(index: Int) = PersonName(s"testFirstName$index", s"testLastName$index", isDeleted = false, isNew = true)
    (0 until n).map(index => Json.obj(
      PartnerNameId.toString -> partner(index))
    ).toArray
  }

  private def data = {
    (0 to 19).map(index => Json.obj(
      PartnerNameId.toString -> partner(index))
    ).toArray
  }

  val defaultAnswers = UserAnswers(Json.obj())
    .set(PartnerNameId(0))(partner(0).copy(isNew = true)).asOpt.value

  private def existingPartnerInUpdate(index: Index) = UserAnswers(Json.obj())
    .set(PartnerNameId(index))(partner(index).copy(isNew = false)).asOpt.value

  private val addressYearsOverAYear = defaultAnswers
    .set(PartnerAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = defaultAnswers
    .set(PartnerAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

  private val hasUtrYes = defaultAnswers
    .set(HasPartnerUTRId(0))(value = true).asOpt.value
  private val hasUtrNo = defaultAnswers
    .set(HasPartnerUTRId(0))(value = false).asOpt.value

  private val addressYearsOverAYearExistingPartner = existingPartnerInUpdate(0)
    .set(PartnerAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYearExistingPartner = existingPartnerInUpdate(0)
    .set(PartnerAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

  private val addPartnersFalse = UserAnswers(Json.obj())
    .set(AddPartnersId)(false).asOpt.value

  private val addPartnersFalseMoreThan1 = UserAnswers(Json.obj(
    "partners" -> createPartners(3))).set(AddPartnersId)(false).asOpt.value

  private val only1Partner = UserAnswers(Json.obj(
    "partners" -> createPartners(1)))

  private val twoPartners = UserAnswers(Json.obj(
    "partners" -> createPartners(2)))

  private val addPartnersTrue = UserAnswers(Json.obj())
    .set(AddPartnersId)(true).asOpt.value

  private val hasNinoYes = defaultAnswers
    .set(HasPartnerNINOId(0))(value = true).asOpt.value
  private val hasNinoNo = defaultAnswers
    .set(HasPartnerNINOId(0))(value = false).asOpt.value

  private def confirmPreviousAddressSame(index: Int) = existingPartnerInUpdate(0)
    .set(PartnerConfirmPreviousAddressId(0))(true).asOpt.value

  private def confirmPreviousAddressNotSame(index: Int) = existingPartnerInUpdate(0)
    .set(PartnerConfirmPreviousAddressId(0))(false).asOpt.value

  val addPartnersMoreThan10 = UserAnswers(Json.obj(
    "partners" -> data))

  implicit val ex: IdentifiedRequest = new IdentifiedRequest() {
    val externalId: String = "test-external-id"
  }
}


