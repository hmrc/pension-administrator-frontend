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
import controllers.register.partnership.partners.routes
import identifiers.Identifier
import identifiers.register.partnership.partners._
import identifiers.register.partnership.{AddPartnersId, MoreThanTenPartnersId}
import models.Mode._
import models._
import models.requests.IdentifiedRequest
import org.scalatest.OptionValues
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.TableFor4
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{NavigatorBehaviour, UserAnswers}

class PartnerNavigatorSpec extends SpecBase with MockitoSugar with NavigatorBehaviour {

  import PartnerNavigatorSpec._

  val navigator = new PartnerNavigator(frontendAppConfig)

  //scalastyle:off line.size.limit
  def routes(mode: Mode): Seq[(Identifier, UserAnswers, Call, Option[Call])] = Seq(
    (AddPartnersId, addPartnersMoreThan10, moreThanTenPartnersPage(mode), None),
    (AddPartnersId, addPartnersTrue, partnerDetailsPage(mode), None),
    (PartnerNameId(0), emptyAnswers, partnerDOBPage(mode), Some(checkYourAnswersPage(mode))),
    (PartnerDOBId(0), emptyAnswers, partnerHasNinoPage(mode), Some(checkYourAnswersPage(mode))),
    (HasPartnerNINOId(0), hasNinoYes, partnerEnterNinoPage(mode), Some(partnerEnterNinoPage(checkMode(mode)))),
    (HasPartnerNINOId(0), hasNinoNo, partnerNoNinoPage(mode), Some(partnerNoNinoPage(checkMode(mode)))),
    (PartnerAddressPostCodeLookupId(0), emptyAnswers, addressListPage(mode), None),
    (PartnerAddressListId(0), emptyAnswers, addressPage(mode), None),
    (PartnerAddressId(0), emptyAnswers, partnerAddressYearsPage(mode), Some(checkYourAnswersPage(mode))),
    (PartnerAddressYearsId(0), addressYearsOverAYear, partnerContactDetailsPage(mode), Some(checkYourAnswersPage(mode))),
    (PartnerAddressYearsId(0), addressYearsUnderAYear, paPostCodePage(mode), Some(paPostCodePage(checkMode(mode)))),
    (PartnerAddressYearsId(0), defaultAnswers, sessionExpiredPage, Some(sessionExpiredPage)),
    (PartnerPreviousAddressPostCodeLookupId(0), emptyAnswers, paAddressListPage(mode), None),
    (PartnerPreviousAddressListId(0), emptyAnswers, previousAddressPage(mode), None),
    (PartnerPreviousAddressId(0), defaultAnswers, partnerContactDetailsPage(mode), Some(checkYourAnswersPage(mode))),
    (PartnerContactDetailsId(0), defaultAnswers, checkYourAnswersPage(mode), Some(checkYourAnswersPage(mode))),
    (CheckYourAnswersId, emptyAnswers, addPartnersPage(mode), None)
  )

  def normalOnlyRoutes: Seq[(Identifier, UserAnswers, Call, Option[Call])] =
    Seq((AddPartnersId, addPartnersFalse, partnershipReviewPage(NormalMode), None),
      (PartnerEnterNINOId(0), emptyAnswers, partnerUniqueTaxReferencePage(NormalMode), Some(checkYourAnswersPage(NormalMode))),
      (PartnerNoNINOReasonId(0), emptyAnswers, partnerUniqueTaxReferencePage(NormalMode), Some(checkYourAnswersPage(NormalMode))),
      (PartnerUniqueTaxReferenceId(0), emptyAnswers, addressPostCodePage(NormalMode), Some(checkYourAnswersPage(NormalMode))),
      (MoreThanTenPartnersId, emptyAnswers, partnershipReviewPage(NormalMode), None)
    )

  def updateOnlyRoutes(): Seq[(Identifier, UserAnswers, Call, Option[Call])] = Seq(
    (AddPartnersId, addPartnersFalse, anyMoreChangesPage, None),
    (PartnerEnterNINOId(0), defaultAnswers, partnerUniqueTaxReferencePage(UpdateMode), None),
    (PartnerEnterNINOId(0), existingPartnerInUpdate(0), anyMoreChangesPage, None),
    (PartnerNoNINOReasonId(0), defaultAnswers, partnerUniqueTaxReferencePage(UpdateMode), None),
    (PartnerNoNINOReasonId(0), existingPartnerInUpdate(0), anyMoreChangesPage, None),
    (PartnerUniqueTaxReferenceId(0), defaultAnswers, addressPostCodePage(UpdateMode), None),
    (PartnerUniqueTaxReferenceId(0), existingPartnerInUpdate(0), anyMoreChangesPage, None),
    (MoreThanTenPartnersId, emptyAnswers, anyMoreChangesPage, None),
    (PartnerAddressYearsId(0), addressYearsOverAYearExistingPartner, anyMoreChangesPage, None),
    (PartnerAddressYearsId(0), addressYearsUnderAYearExistingPartner, confirmPreviousAddress, None),
    (PartnerConfirmPreviousAddressId(0), confirmPreviousAddressSame(0), anyMoreChangesPage, None),
    (PartnerConfirmPreviousAddressId(0), confirmPreviousAddressNotSame(0), previousAddressPage(UpdateMode), None),
    (PartnerPreviousAddressId(0), existingPartnerInUpdate(0), anyMoreChangesPage, None),
    (PartnerContactDetailsId(0), existingPartnerInUpdate(0), anyMoreChangesPage, None)
  )

  def normalRoutes: TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Next Page (Check Mode)"),
    normalOnlyRoutes ++ routes(NormalMode): _*
  )

  def updateRoutes(): TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Next Page (Check Mode)"),
    updateOnlyRoutes ++ routes(UpdateMode): _*
  )

  //scalastyle:on line.size.limit

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, normalRoutes, dataDescriber)
    behave like navigatorWithRoutes(navigator, updateRoutes(), dataDescriber, UpdateMode)
  }
}

object PartnerNavigatorSpec extends OptionValues {

  private lazy val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  private lazy val anyMoreChangesPage = controllers.register.routes.AnyMoreChangesController.onPageLoad()
  private lazy val confirmPreviousAddress = routes.PartnerConfirmPreviousAddressController.onPageLoad(0)

  private def checkYourAnswersPage(mode: Mode) = routes.CheckYourAnswersController.onPageLoad(0, mode)

  private def partnershipReviewPage(mode: Mode) = controllers.register.partnership.routes.PartnershipReviewController.onPageLoad()

  private def partnerDOBPage(mode: Mode) = routes.PartnerDOBController.onPageLoad(mode, 0)

  private def partnerUniqueTaxReferencePage(mode: Mode) = routes.PartnerUniqueTaxReferenceController.onPageLoad(mode, 0)

  private def partnerAddressYearsPage(mode: Mode) = routes.PartnerAddressYearsController.onPageLoad(mode, 0)

  private def partnerContactDetailsPage(mode: Mode) = routes.PartnerContactDetailsController.onPageLoad(mode, 0)

  private def addPartnersPage(mode: Mode) = controllers.register.partnership.routes.AddPartnerController.onPageLoad(mode)

  private def moreThanTenPartnersPage(mode: Mode) = controllers.register.partnership.routes.MoreThanTenPartnersController.onPageLoad(mode)

  private def partnerDetailsPage(mode: Mode) = routes.PartnerNameController.onPageLoad(mode, 0)

  private def partnerHasNinoPage(mode: Mode): Call = routes.HasPartnerNINOController.onPageLoad(mode, 0)

  private def partnerEnterNinoPage(mode: Mode): Call = routes.PartnerEnterNINOController.onPageLoad(mode, 0)

  private def partnerNoNinoPage(mode: Mode): Call = routes.PartnerNoNINOReasonController.onPageLoad(mode, 0)

  def paPostCodePage(mode: Mode): Call = routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, 0)

  def paAddressListPage(mode: Mode): Call = routes.PartnerPreviousAddressListController.onPageLoad(mode, 0)

  def previousAddressPage(mode: Mode): Call = routes.PartnerPreviousAddressController.onPageLoad(mode, 0)

  def addressPostCodePage(mode: Mode): Call = routes.PartnerAddressPostCodeLookupController.onPageLoad(mode, 0)

  def addressListPage(mode: Mode): Call = routes.PartnerAddressListController.onPageLoad(mode, 0)

  def addressPage(mode: Mode): Call = routes.PartnerAddressController.onPageLoad(mode, 0)

  private def partner(index: Int) =
    PersonName(s"testFirstName$index", s"testLastName$index", isDeleted = (index % 2 == 0), isNew = true)

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

  private val addressYearsOverAYearExistingPartner = existingPartnerInUpdate(0)
    .set(PartnerAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYearExistingPartner = existingPartnerInUpdate(0)
    .set(PartnerAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

  private val addPartnersFalse = UserAnswers(Json.obj())
    .set(AddPartnersId)(false).asOpt.value
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


