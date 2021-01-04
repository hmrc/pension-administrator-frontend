/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.register.company.directors.routes
import identifiers.Identifier
import identifiers.register.company.directors._
import identifiers.register.company.{AddCompanyDirectorsId, MoreThanTenDirectorsId}
import models.Mode.checkMode
import models._
import models.requests.IdentifiedRequest
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor3
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import utils.{Navigator, NavigatorBehaviour, UserAnswers}

class DirectorNavigatorSpec extends SpecBase with MockitoSugar with NavigatorBehaviour {

  import DirectorNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[DirectorNavigator]

  "DirectorNavigator in NormalMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (AddCompanyDirectorsId, addCompanyDirectorsMoreThan10, moreThanTenDirectorsPage(NormalMode)),
      (AddCompanyDirectorsId, addCompanyDirectorsTrue, directorNamePage(NormalMode)),
      (DirectorNameId(0), emptyAnswers, directorDobPage(NormalMode)),
      (DirectorDOBId(0), emptyAnswers, directorHasNinoPage(NormalMode)),
      (HasDirectorNINOId(index), hasNinoYes, directorEnterNinoPage(NormalMode)),
      (HasDirectorNINOId(index), hasNinoNo, directorNoNinoPage(NormalMode)),
      (HasDirectorUTRId(0), hasUtrYes, directorEnterUtrPage(NormalMode)),
      (HasDirectorUTRId(0), hasUtrNo, directorNoUtrReasonPage(NormalMode)),
      (CompanyDirectorAddressPostCodeLookupId(index), emptyAnswers, addressListPage(NormalMode)),
      (DirectorAddressYearsId(index), addressYearsOverAYear, directorEmailPage(NormalMode)),
      (DirectorAddressYearsId(index), addressYearsUnderAYear, paPostCodePage(NormalMode)),
      (DirectorAddressYearsId(index), emptyAnswers, sessionExpiredPage),
      (DirectorPreviousAddressPostCodeLookupId(index), emptyAnswers, paAddressListPage(NormalMode)),
      (DirectorPreviousAddressId(index), defaultAnswers, directorEmailPage(NormalMode)),
      (DirectorEmailId(index), defaultAnswers, directorPhonePage(NormalMode)),
      (DirectorPhoneId(index), defaultAnswers, checkYourAnswersPage(NormalMode)),
      (CheckYourAnswersId, emptyAnswers, addDirectorsPage(NormalMode)),
      (AddCompanyDirectorsId, addCompanyDirectorsFalse, companyReviewPage(NormalMode)),
      (DirectorEnterNINOId(index), emptyAnswers, directorHasUtrPage(NormalMode)),
      (DirectorNoNINOReasonId(index), emptyAnswers, directorHasUtrPage(NormalMode)),
      (DirectorEnterUTRId(0), emptyAnswers, addressPostCodePage(NormalMode)),
      (DirectorNoUTRReasonId(0), emptyAnswers, addressPostCodePage(NormalMode)),
      (DirectorAddressId(index), emptyAnswers, directorAddressYearsPage(NormalMode)),
      (MoreThanTenDirectorsId, emptyAnswers, companyReviewPage(NormalMode))
    )

    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, NormalMode)
  }

  "DirectorNavigator in CheckMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (AddCompanyDirectorsId, addCompanyDirectorsMoreThan10, moreThanTenDirectorsPage(CheckMode)),
      (DirectorNameId(0), emptyAnswers, checkYourAnswersPage(NormalMode)),
      (DirectorDOBId(0), emptyAnswers, checkYourAnswersPage(NormalMode)),
      (HasDirectorNINOId(index), hasNinoYes, directorEnterNinoPage(checkMode(NormalMode))),
      (HasDirectorNINOId(index), hasNinoNo, directorNoNinoPage(checkMode(NormalMode))),
      (HasDirectorUTRId(0), hasUtrYes, directorEnterUtrPage(checkMode(NormalMode))),
      (HasDirectorUTRId(0), hasUtrNo, directorNoUtrReasonPage(checkMode(NormalMode))),
      (DirectorAddressYearsId(index), addressYearsOverAYear, checkYourAnswersPage(NormalMode)),
      (DirectorAddressYearsId(index), addressYearsUnderAYear, paPostCodePage(checkMode(NormalMode))),
      (DirectorAddressYearsId(index), emptyAnswers, sessionExpiredPage),
      (DirectorPreviousAddressId(index), defaultAnswers, checkYourAnswersPage(NormalMode)),
      (DirectorEmailId(index), defaultAnswers, checkYourAnswersPage(NormalMode)),
      (DirectorPhoneId(index), defaultAnswers, checkYourAnswersPage(NormalMode)),
      (DirectorEnterNINOId(index), emptyAnswers, checkYourAnswersPage(NormalMode)),
      (DirectorNoNINOReasonId(index), emptyAnswers, checkYourAnswersPage(NormalMode)),
      (DirectorEnterUTRId(0), emptyAnswers, checkYourAnswersPage(NormalMode)),
      (DirectorNoUTRReasonId(0), emptyAnswers, checkYourAnswersPage(NormalMode)),
      (DirectorAddressId(index), emptyAnswers, checkYourAnswersPage(NormalMode))
    )

    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, CheckMode)
  }

  "DirectorNavigator in UpdateMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (AddCompanyDirectorsId, addCompanyDirectorsMoreThan10, moreThanTenDirectorsPage(UpdateMode)),
      (AddCompanyDirectorsId, addCompanyDirectorsTrue, directorNamePage(UpdateMode)),
      (DirectorNameId(0), emptyAnswers, directorDobPage(UpdateMode)),
      (DirectorDOBId(0), emptyAnswers, directorHasNinoPage(UpdateMode)),
      (HasDirectorNINOId(index), hasNinoYes, directorEnterNinoPage(UpdateMode)),
      (HasDirectorNINOId(index), hasNinoNo, directorNoNinoPage(UpdateMode)),
      (HasDirectorUTRId(0), hasUtrYes, directorEnterUtrPage(UpdateMode)),
      (HasDirectorUTRId(0), hasUtrNo, directorNoUtrReasonPage(UpdateMode)),
      (CompanyDirectorAddressPostCodeLookupId(index), emptyAnswers, addressListPage(UpdateMode)),
      (DirectorAddressYearsId(index), addressYearsOverAYear, directorEmailPage(UpdateMode)),
      (DirectorAddressYearsId(index), addressYearsUnderAYear, paPostCodePage(UpdateMode)),
      (DirectorAddressYearsId(index), emptyAnswers, sessionExpiredPage),
      (DirectorPreviousAddressPostCodeLookupId(index), emptyAnswers, paAddressListPage(UpdateMode)),
      (DirectorPreviousAddressId(index), defaultAnswers, directorEmailPage(UpdateMode)),
      (DirectorEmailId(index), defaultAnswers, directorPhonePage(UpdateMode)),
      (DirectorPhoneId(index), defaultAnswers, checkYourAnswersPage(UpdateMode)),
      (CheckYourAnswersId, emptyAnswers, addDirectorsPage(UpdateMode)),
      (AddCompanyDirectorsId, addCompanyDirectorsFalse, anyMoreChangesPage),
      (MoreThanTenDirectorsId, emptyAnswers, anyMoreChangesPage),
      (DirectorEnterNINOId(index), defaultAnswers, directorHasUtrPage(UpdateMode)),
      (DirectorEnterNINOId(index), existingDirectorInUpdate(index), anyMoreChangesPage),
      (DirectorNoNINOReasonId(index), defaultAnswers, directorHasUtrPage(UpdateMode)),
      (DirectorNoNINOReasonId(index), existingDirectorInUpdate(index), anyMoreChangesPage),
      (DirectorEnterUTRId(0), defaultAnswers, addressPostCodePage(UpdateMode)),
      (DirectorEnterUTRId(0), existingDirectorInUpdate(0), anyMoreChangesPage),
      (DirectorNoUTRReasonId(0), defaultAnswers, addressPostCodePage(UpdateMode)),
      (DirectorNoUTRReasonId(0), existingDirectorInUpdate(0), anyMoreChangesPage),
      (DirectorAddressId(index), defaultAnswers, directorAddressYearsPage(UpdateMode)),
      (DirectorAddressId(index), existingDirectorInUpdate(index), confirmPreviousAddressPage),
      (DirectorAddressYearsId(index), addressYearsOverAYearExistingDirector, anyMoreChangesPage),
      (DirectorAddressYearsId(index), addressYearsUnderAYearExistingDirector, confirmPreviousAddressPage),
      (DirectorConfirmPreviousAddressId(index), confirmPreviousAddressNotSame, paPostCodePage(UpdateMode)),
      (DirectorConfirmPreviousAddressId(index), confirmPreviousAddressSame, anyMoreChangesPage),
      (DirectorPreviousAddressId(index), existingDirectorInUpdate(index), anyMoreChangesPage),
      (DirectorEmailId(index), existingDirectorInUpdate(index), anyMoreChangesPage),
      (DirectorPhoneId(index), existingDirectorInUpdate(index), anyMoreChangesPage)
    )

    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, UpdateMode)
  }

  "DirectorNavigator in CheckUpdateMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (AddCompanyDirectorsId, addCompanyDirectorsMoreThan10, moreThanTenDirectorsPage(CheckUpdateMode)),
      (DirectorNameId(0), emptyAnswers, checkYourAnswersPage(UpdateMode)),
      (DirectorDOBId(0), emptyAnswers, checkYourAnswersPage(UpdateMode)),
      (HasDirectorNINOId(index), hasNinoYes, directorEnterNinoPage(CheckUpdateMode)),
      (HasDirectorNINOId(index), hasNinoNo, directorNoNinoPage(CheckUpdateMode)),
      (HasDirectorUTRId(0), hasUtrYes, directorEnterUtrPage(CheckUpdateMode)),
      (HasDirectorUTRId(0), hasUtrNo, directorNoUtrReasonPage(CheckUpdateMode)),
      (DirectorAddressYearsId(index), addressYearsOverAYear, checkYourAnswersPage(UpdateMode)),
      (DirectorAddressYearsId(index), addressYearsUnderAYear, paPostCodePage(CheckUpdateMode)),
      (DirectorAddressYearsId(index), emptyAnswers, sessionExpiredPage),
      (DirectorPreviousAddressId(index), defaultAnswers, checkYourAnswersPage(UpdateMode)),
      (DirectorEmailId(index), defaultAnswers, checkYourAnswersPage(UpdateMode)),
      (DirectorPhoneId(index), defaultAnswers, checkYourAnswersPage(UpdateMode))
    )

    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, CheckUpdateMode)
  }
}

object DirectorNavigatorSpec extends OptionValues {

  private val index = 0
  private lazy val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  private lazy val anyMoreChangesPage = controllers.register.routes.AnyMoreChangesController.onPageLoad()
  private lazy val confirmPreviousAddressPage = routes.DirectorConfirmPreviousAddressController.onPageLoad(index)

  private def checkYourAnswersPage(mode: Mode): Call = routes.CheckYourAnswersController.onPageLoad(mode, index)

  private def companyReviewPage(mode: Mode): Call = controllers.register.company.routes.CompanyReviewController.onPageLoad()

  private def moreThanTenDirectorsPage(mode: Mode): Call = controllers.register.company.routes.MoreThanTenDirectorsController.onPageLoad(mode)

  private def directorNamePage(mode: Mode): Call = routes.DirectorNameController.onPageLoad(mode, index)

  private def directorDobPage(mode: Mode): Call = routes.DirectorDOBController.onPageLoad(mode, index)

  private def directorHasNinoPage(mode: Mode): Call = routes.HasDirectorNINOController.onPageLoad(mode, index)

  private def directorEnterNinoPage(mode: Mode): Call = routes.DirectorEnterNINOController.onPageLoad(mode, index)

  private def directorNoNinoPage(mode: Mode): Call = routes.DirectorNoNINOReasonController.onPageLoad(mode, index)

  private def directorAddressYearsPage(mode: Mode): Call = routes.DirectorAddressYearsController.onPageLoad(mode, index)

  private def directorPhonePage(mode: Mode): Call = routes.DirectorPhoneController.onPageLoad(mode, index)

  private def directorEmailPage(mode: Mode): Call = routes.DirectorEmailController.onPageLoad(mode, index)

  private def addDirectorsPage(mode: Mode): Call = controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(mode)

  private def paPostCodePage(mode: Mode): Call = routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(mode, index)

  private def paAddressListPage(mode: Mode): Call = routes.DirectorPreviousAddressListController.onPageLoad(mode, index)

  private def addressPostCodePage(mode: Mode): Call = routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(mode, index)

  private def addressListPage(mode: Mode): Call = routes.CompanyDirectorAddressListController.onPageLoad(mode, index)

  def addressPage(mode: Mode): Call = routes.DirectorAddressController.onPageLoad(mode, 0)
  def directorHasUtrPage(mode: Mode): Call = routes.HasDirectorUTRController.onPageLoad(mode, 0)
  def directorEnterUtrPage(mode: Mode): Call = routes.DirectorEnterUTRController.onPageLoad(mode, 0)
  def directorNoUtrReasonPage(mode: Mode): Call = routes.DirectorNoUTRReasonController.onPageLoad(mode, 0)


  private def director(index: Int) =
    PersonName(s"testFirstName$index", s"testLastName$index", isDeleted = index % 2 == 0, isNew = true)

  private def data: Array[JsObject] = {
    (0 to 19).map(index => Json.obj(
      DirectorNameId.toString -> director(index)
    )).toArray
  }
  private val defaultAnswers = UserAnswers(Json.obj())
    .set(DirectorNameId(index))(director(index).copy(isNew = true)).asOpt.value

  private def existingDirectorInUpdate(index: Index): UserAnswers = UserAnswers(Json.obj())
    .set(DirectorNameId(index))(director(index).copy(isNew = false)).asOpt.value

  private val addressYearsOverAYear = defaultAnswers
    .set(DirectorAddressYearsId(index))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = defaultAnswers
    .set(DirectorAddressYearsId(index))(AddressYears.UnderAYear).asOpt.value
  private val hasNinoYes = defaultAnswers
    .set(HasDirectorNINOId(index))(value = true).asOpt.value
  private val hasNinoNo = defaultAnswers
    .set(HasDirectorNINOId(index))(value = false).asOpt.value
  private val hasUtrYes = defaultAnswers
    .set(HasDirectorUTRId(0))(value = true).asOpt.value
  private val hasUtrNo = defaultAnswers
    .set(HasDirectorUTRId(0))(value = false).asOpt.value
  private val confirmPreviousAddressSame = existingDirectorInUpdate(index)
    .set(DirectorConfirmPreviousAddressId(index))(value = true).asOpt.value
  private val confirmPreviousAddressNotSame = existingDirectorInUpdate(index)
    .set(DirectorConfirmPreviousAddressId(index))(value = false).asOpt.value
  private val addressYearsOverAYearExistingDirector = existingDirectorInUpdate(index)
    .set(DirectorAddressYearsId(index))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYearExistingDirector = existingDirectorInUpdate(index)
    .set(DirectorAddressYearsId(index))(AddressYears.UnderAYear).asOpt.value
  private val addCompanyDirectorsFalse = UserAnswers(Json.obj())
    .set(AddCompanyDirectorsId)(value = false).asOpt.value
  private val addCompanyDirectorsTrue = UserAnswers(Json.obj())
    .set(AddCompanyDirectorsId)(value = true).asOpt.value

  private val addCompanyDirectorsMoreThan10 = UserAnswers(Json.obj(
    "directors" -> data))

  implicit val ex: IdentifiedRequest = new IdentifiedRequest() {
    val externalId: String = "test-external-id"
  }

}
