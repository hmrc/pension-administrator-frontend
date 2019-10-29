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
import models.Mode.checkMode
import models._
import models.requests.IdentifiedRequest
import org.scalatest.OptionValues
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.TableFor6
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import utils.{NavigatorBehaviour, UserAnswers}

class DirectorNavigatorSpec extends SpecBase with MockitoSugar with NavigatorBehaviour {

  import DirectorNavigatorSpec._

  val navigator = new DirectorNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)

  //scalastyle:off line.size.limit
  private def routes(mode: Mode): Seq[(Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean)] = Seq(
    (AddCompanyDirectorsId, addCompanyDirectorsMoreThan10, moreThanTenDirectorsPage(mode), true, Some(moreThanTenDirectorsPage(checkMode(mode))), true),
    (AddCompanyDirectorsId, addCompanyDirectorsTrue, directorNamePage(mode), true, None, true),
    (DirectorNameId(0), emptyAnswers, directorDobPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (DirectorDOBId(0), emptyAnswers, directorHasNinoPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (HasDirectorNINOId(index), hasNinoYes, directorEnterNinoPage(mode), true, Some(directorEnterNinoPage(checkMode(mode))), true),
    (HasDirectorNINOId(index), hasNinoNo, directorNoNinoPage(mode), true, Some(directorNoNinoPage(checkMode(mode))), true),
    (CompanyDirectorAddressPostCodeLookupId(index), emptyAnswers, addressListPage(mode), false, Some(addressListPage(checkMode(mode))), false),
    (CompanyDirectorAddressListId(index), emptyAnswers, addressPage(mode), true, Some(addressPage(checkMode(mode))), true),
    (DirectorAddressId(index), emptyAnswers, directorAddressYearsPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (DirectorAddressYearsId(index), addressYearsOverAYear, directorEmailPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (DirectorAddressYearsId(index), addressYearsUnderAYear, paPostCodePage(mode), true, Some(paPostCodePage(checkMode(mode))), true),
    (DirectorAddressYearsId(index), emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),
    (DirectorPreviousAddressPostCodeLookupId(index), emptyAnswers, paAddressListPage(mode), false, Some(paAddressListPage(checkMode(mode))), false),
    (DirectorPreviousAddressListId(index), emptyAnswers, previousAddressPage(mode), true, Some(previousAddressPage(checkMode(mode))), true),
    (DirectorPreviousAddressId(index), defaultAnswers, directorEmailPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (DirectorEmailId(index), defaultAnswers, directorPhonePage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (DirectorPhoneId(index), defaultAnswers, checkYourAnswersPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (CheckYourAnswersId, emptyAnswers, addDirectorsPage(mode), true, None, false)
  )

  private def normalOnlyRoutes(): Seq[(Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean)] = Seq(
    (AddCompanyDirectorsId, addCompanyDirectorsFalse, companyReviewPage(NormalMode), true, None, true),
    (DirectorEnterNINOId(index), emptyAnswers, directorUniqueTaxReferencePage(NormalMode), true, Some(checkYourAnswersPage(NormalMode)), true),
    (DirectorNoNINOReasonId(index), emptyAnswers, directorUniqueTaxReferencePage(NormalMode), true, Some(checkYourAnswersPage(NormalMode)), true),
    (DirectorUniqueTaxReferenceId(index), emptyAnswers, addressPostCodePage(NormalMode), true, Some(checkYourAnswersPage(NormalMode)), true),
    (MoreThanTenDirectorsId, emptyAnswers, companyReviewPage(NormalMode), true, None, false)
  )

  private def updateOnlyRoutes(): Seq[(Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean)] = Seq(
    (AddCompanyDirectorsId, addCompanyDirectorsFalse, anyMoreChangesPage, true, None, true),
    (MoreThanTenDirectorsId, emptyAnswers, anyMoreChangesPage, true, None, false),
    (DirectorEnterNINOId(index), defaultAnswers, directorUniqueTaxReferencePage(UpdateMode), false, None, true),
    (DirectorEnterNINOId(index), existingDirectorInUpdate(index), anyMoreChangesPage, false, None, true),
    (DirectorNoNINOReasonId(index), defaultAnswers, directorUniqueTaxReferencePage(UpdateMode), false, None, true),
    (DirectorNoNINOReasonId(index), existingDirectorInUpdate(index), anyMoreChangesPage, false, None, true),
    (DirectorUniqueTaxReferenceId(index), defaultAnswers, addressPostCodePage(UpdateMode), false, None, true),
    (DirectorUniqueTaxReferenceId(index), existingDirectorInUpdate(index), anyMoreChangesPage, false, None, true),
    (DirectorAddressYearsId(index), addressYearsOverAYearExistingDirector, anyMoreChangesPage, true, None, true),
    (DirectorAddressYearsId(index), addressYearsUnderAYearExistingDirector, confirmPreviousAddressPage, true, None, true),
    (DirectorConfirmPreviousAddressId(index), confirmPreviousAddressNotSame, previousAddressPage(UpdateMode), false, None, true),
    (DirectorConfirmPreviousAddressId(index), confirmPreviousAddressSame, anyMoreChangesPage, false, None, true),
    (DirectorPreviousAddressId(index), existingDirectorInUpdate(index), anyMoreChangesPage, false, None, true),
    (DirectorEmailId(index), existingDirectorInUpdate(index), anyMoreChangesPage, false, None, true),
    (DirectorPhoneId(index), existingDirectorInUpdate(index), anyMoreChangesPage, false, None, true)
  )

  private def normalRoutes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
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
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, normalRoutes(), dataDescriber)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateRoutes(), dataDescriber, UpdateMode)
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

  private def directorUniqueTaxReferencePage(mode: Mode): Call = routes.DirectorUniqueTaxReferenceController.onPageLoad(mode, index)

  private def directorAddressYearsPage(mode: Mode): Call = routes.DirectorAddressYearsController.onPageLoad(mode, index)

  private def directorPhonePage(mode: Mode): Call = routes.DirectorPhoneController.onPageLoad(mode, index)

  private def directorEmailPage(mode: Mode): Call = routes.DirectorEmailController.onPageLoad(mode, index)

  private def addDirectorsPage(mode: Mode): Call = controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(mode)

  private def paPostCodePage(mode: Mode): Call = routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(mode, index)

  private def paAddressListPage(mode: Mode): Call = routes.DirectorPreviousAddressListController.onPageLoad(mode, index)

  private def previousAddressPage(mode: Mode): Call = routes.DirectorPreviousAddressController.onPageLoad(mode, index)

  private def addressPostCodePage(mode: Mode): Call = routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(mode, index)

  private def addressListPage(mode: Mode): Call = routes.CompanyDirectorAddressListController.onPageLoad(mode, index)

  private def addressPage(mode: Mode): Call = routes.DirectorAddressController.onPageLoad(mode, index)

  private def director(index: Int) =
    PersonName(s"testFirstName$index", s"testLastName$index", isDeleted = (index % 2 == 0), isNew = true)

  private def data: Array[JsObject] = {
    (0 to 19).map(index => Json.obj(
      DirectorNameId.toString -> director(index)
    )).toArray
  }

  private val emptyAnswers = UserAnswers(Json.obj())
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
