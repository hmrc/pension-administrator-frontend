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

import java.time.LocalDate

import base.SpecBase
import connectors.FakeUserAnswersCacheConnector
import controllers.register.company.directors.routes
import identifiers.Identifier
import identifiers.register.company.directors._
import identifiers.register.company.{AddCompanyDirectorsId, MoreThanTenDirectorsId}
import models._
import models.Mode.checkMode
import models.requests.IdentifiedRequest
import org.scalatest.OptionValues
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{NavigatorBehaviour, UserAnswers}

class DirectorNavigatorSpec extends SpecBase with MockitoSugar with NavigatorBehaviour {

  import DirectorNavigatorSpec._

  val navigator = new DirectorNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)

  //scalastyle:off line.size.limit
  private def routes(mode: Mode): Seq[(Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean)] = Seq(
    (AddCompanyDirectorsId, addCompanyDirectorsMoreThan10, moreThanTenDirectorsPage(mode), true, Some(moreThanTenDirectorsPage(checkMode(mode))), true),
    (AddCompanyDirectorsId, addCompanyDirectorsTrue, directorDetailsPage(mode), true, None, true),
    (DirectorDetailsId(0), emptyAnswers, directorNinoPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (HasDirectorUTRId(0), hasUtrYes, directorEnterUtrPage(mode), true, Some(directorEnterUtrPage(checkMode(mode))), true),
    (HasDirectorUTRId(0), hasUtrNo, directorNoUtrReasonPage(mode), true, Some(directorNoUtrReasonPage(checkMode(mode))), true),
    (CompanyDirectorAddressPostCodeLookupId(0), emptyAnswers, addressListPage(mode), false, Some(addressListPage(checkMode(mode))), false),
    (CompanyDirectorAddressListId(0), emptyAnswers, addressPage(mode), true, Some(addressPage(checkMode(mode))), true),
    (DirectorAddressId(0), emptyAnswers, directorAddressYearsPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (DirectorAddressYearsId(0), addressYearsOverAYear, directorEmailPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (DirectorAddressYearsId(0), addressYearsUnderAYear, paPostCodePage(mode), true, Some(paPostCodePage(checkMode(mode))), true),
    (DirectorAddressYearsId(0), emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),
    (DirectorPreviousAddressPostCodeLookupId(0), emptyAnswers, paAddressListPage(mode), false, Some(paAddressListPage(checkMode(mode))), false),
    (DirectorPreviousAddressListId(0), emptyAnswers, previousAddressPage(mode), true, Some(previousAddressPage(checkMode(mode))), true),
    (DirectorPreviousAddressId(0), defaultAnswers, directorEmailPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (DirectorEmailId(0), defaultAnswers, directorPhonePage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (DirectorPhoneId(0), defaultAnswers, checkYourAnswersPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (CheckYourAnswersId, emptyAnswers, addDirectorsPage(mode), true, None, false)
  )

  private def normalOnlyRoutes: Seq[(Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean)] = Seq(
    (AddCompanyDirectorsId, addCompanyDirectorsFalse, companyReviewPage(NormalMode), true, None, true),
    (DirectorNinoId(0), emptyAnswers, directorHasUtrPage(NormalMode), true, Some(checkYourAnswersPage(NormalMode)), true),
    (DirectorEnterUTRId(0), emptyAnswers, addressPostCodePage(NormalMode), true, Some(checkYourAnswersPage(NormalMode)), true),
    (DirectorNoUTRReasonId(0), emptyAnswers, addressPostCodePage(NormalMode), true, Some(checkYourAnswersPage(NormalMode)), true),
    (MoreThanTenDirectorsId, emptyAnswers, companyReviewPage(NormalMode), true, None, false)
  )

  private def updateOnlyRoutes(): Seq[(Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean)] = Seq(
    (AddCompanyDirectorsId, addCompanyDirectorsFalse, anyMoreChangesPage, true, None, true),
    (MoreThanTenDirectorsId, emptyAnswers, anyMoreChangesPage, true, None, false),
    (DirectorNinoId(0), defaultAnswers, directorHasUtrPage(UpdateMode), false, None, true),
    (DirectorNinoId(0), existingDirectorInUpdate(0), anyMoreChangesPage, false, None, true),
    (DirectorEnterUTRId(0), defaultAnswers, addressPostCodePage(UpdateMode), false, None, true),
    (DirectorEnterUTRId(0), existingDirectorInUpdate(0), anyMoreChangesPage, false, None, true),
    (DirectorNoUTRReasonId(0), defaultAnswers, addressPostCodePage(UpdateMode), false, None, true),
    (DirectorNoUTRReasonId(0), existingDirectorInUpdate(0), anyMoreChangesPage, false, None, true),
    (DirectorAddressYearsId(0), addressYearsOverAYearExistingDirector, anyMoreChangesPage, true, None, true),
    (DirectorAddressYearsId(0), addressYearsUnderAYearExistingDirector, confirmPreviousAddressPage, true, None, true),
    (DirectorConfirmPreviousAddressId(0), confirmPreviousAddressNotSame, previousAddressPage(UpdateMode), false, None, true),
    (DirectorConfirmPreviousAddressId(0), confirmPreviousAddressSame, anyMoreChangesPage, false, None, true),
    (DirectorPreviousAddressId(0), existingDirectorInUpdate(0), anyMoreChangesPage, false, None, true),
    (DirectorEmailId(0), existingDirectorInUpdate(0), anyMoreChangesPage, false, None, true),
    (DirectorPhoneId(0), existingDirectorInUpdate(0), anyMoreChangesPage, false, None, true)
  )

  private def normalRoutes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save(NormalMode)", "Next Page (Check Mode)", "Save(CheckMode"),
    normalOnlyRoutes ++ routes(NormalMode): _*
  )

  private def updateRoutes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save(NormalMode)", "Next Page (Check Mode)", "Save(CheckMode"),
    updateOnlyRoutes ++ routes(UpdateMode): _*
  )

  //scalastyle:on line.size.limit

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, normalRoutes, dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes, dataDescriber, UpdateMode)
  }
}

object DirectorNavigatorSpec extends OptionValues {

  private lazy val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  private lazy val anyMoreChangesPage = controllers.register.routes.AnyMoreChangesController.onPageLoad()
  private lazy val confirmPreviousAddressPage = routes.DirectorConfirmPreviousAddressController.onPageLoad(0)
  def checkYourAnswersPage(mode: Mode) = routes.CheckYourAnswersController.onPageLoad(mode, 0)
  def companyReviewPage(mode: Mode) = controllers.register.company.routes.CompanyReviewController.onPageLoad()
  def moreThanTenDirectorsPage(mode: Mode) = controllers.register.company.routes.MoreThanTenDirectorsController.onPageLoad(mode)
  def directorDetailsPage(mode: Mode) = routes.DirectorDetailsController.onPageLoad(mode, 0)
  def directorNinoPage(mode: Mode) = routes.DirectorNinoController.onPageLoad(mode, 0)
  def directorAddressYearsPage(mode: Mode) = routes.DirectorAddressYearsController.onPageLoad(mode, 0)
  def directorPhonePage(mode: Mode) = routes.DirectorPhoneController.onPageLoad(mode, 0)
  def directorEmailPage(mode: Mode) = routes.DirectorEmailController.onPageLoad(mode, 0)
  def addDirectorsPage(mode: Mode) = controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(mode)

  def paPostCodePage(mode: Mode): Call = routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(mode, 0)

  def paAddressListPage(mode: Mode): Call = routes.DirectorPreviousAddressListController.onPageLoad(mode, 0)

  def previousAddressPage(mode: Mode): Call = routes.DirectorPreviousAddressController.onPageLoad(mode, 0)

  def addressPostCodePage(mode: Mode): Call = routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(mode, 0)

  def addressListPage(mode: Mode): Call = routes.CompanyDirectorAddressListController.onPageLoad(mode, 0)

  def addressPage(mode: Mode): Call = routes.DirectorAddressController.onPageLoad(mode, 0)
  def directorHasUtrPage(mode: Mode): Call = routes.HasDirectorUTRController.onPageLoad(mode, 0)
  def directorEnterUtrPage(mode: Mode): Call = routes.DirectorEnterUTRController.onPageLoad(mode, 0)
  def directorNoUtrReasonPage(mode: Mode): Call = routes.DirectorNoUTRReasonController.onPageLoad(mode, 0)

  private def director(index: Int) =
    PersonDetails(s"testFirstName$index", None, s"testLastName$index", LocalDate.now, isDeleted = (index % 2 == 0), isNew = true)

  private def data = {
    (0 to 19).map(index => Json.obj(
      DirectorDetailsId.toString -> director(index)
    )).toArray
  }

  val emptyAnswers = UserAnswers(Json.obj())
  val defaultAnswers = UserAnswers(Json.obj())
    .set(DirectorDetailsId(0))(director(0).copy(isNew = true)).asOpt.value
  private def existingDirectorInUpdate(index: Index) = UserAnswers(Json.obj())
    .set(DirectorDetailsId(index))(director(index).copy(isNew = false)).asOpt.value
  private val addressYearsOverAYear = defaultAnswers
    .set(DirectorAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = defaultAnswers
    .set(DirectorAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

  private val confirmPreviousAddressSame = existingDirectorInUpdate(0)
    .set(DirectorConfirmPreviousAddressId(0))(true).asOpt.value

  private val confirmPreviousAddressNotSame = existingDirectorInUpdate(0)
    .set(DirectorConfirmPreviousAddressId(0))(false).asOpt.value
  private val hasUtrYes = defaultAnswers
    .set(HasDirectorUTRId(0))(value = true).asOpt.value
  private val hasUtrNo = defaultAnswers
    .set(HasDirectorUTRId(0))(value = false).asOpt.value

  private val addressYearsOverAYearExistingDirector = existingDirectorInUpdate(0)
    .set(DirectorAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYearExistingDirector = existingDirectorInUpdate(0)
    .set(DirectorAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value
  private val addCompanyDirectorsFalse = UserAnswers(Json.obj())
    .set(AddCompanyDirectorsId)(false).asOpt.value
  private val addCompanyDirectorsTrue = UserAnswers(Json.obj())
    .set(AddCompanyDirectorsId)(true).asOpt.value

  val addCompanyDirectorsMoreThan10 = UserAnswers(Json.obj(
    "directors" -> data))

  implicit val ex: IdentifiedRequest = new IdentifiedRequest() {
    val externalId: String = "test-external-id"
  }

}
