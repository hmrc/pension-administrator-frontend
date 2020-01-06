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
import connectors.cache.FakeUserAnswersCacheConnector
import controllers.register.company.directors.routes
import identifiers.Identifier
import identifiers.register.company.directors._
import identifiers.register.company.{AddCompanyDirectorsId, MoreThanTenDirectorsId}
import models.Mode.checkMode
import models._
import models.requests.IdentifiedRequest
import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.prop.TableFor4
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import utils.{NavigatorBehaviour, UserAnswers}

class DirectorNavigatorSpec extends SpecBase with MockitoSugar with NavigatorBehaviour {

  import DirectorNavigatorSpec._

  val navigator = new DirectorNavigator(frontendAppConfig)

  //scalastyle:off line.size.limit
  private def routes(mode: Mode): Seq[(Identifier, UserAnswers, Call, Option[Call])] = Seq(
    (AddCompanyDirectorsId, addCompanyDirectorsMoreThan10, moreThanTenDirectorsPage(mode), Some(moreThanTenDirectorsPage(checkMode(mode)))),
    (AddCompanyDirectorsId, addCompanyDirectorsTrue, directorNamePage(mode), None),
    (DirectorNameId(0), emptyAnswers, directorDobPage(mode), Some(checkYourAnswersPage(mode))),
    (DirectorDOBId(0), emptyAnswers, directorHasNinoPage(mode), Some(checkYourAnswersPage(mode))),
    (HasDirectorNINOId(index), hasNinoYes, directorEnterNinoPage(mode), Some(directorEnterNinoPage(checkMode(mode)))),
    (HasDirectorNINOId(index), hasNinoNo, directorNoNinoPage(mode), Some(directorNoNinoPage(checkMode(mode)))),
    (HasDirectorUTRId(0), hasUtrYes, directorEnterUtrPage(mode), Some(directorEnterUtrPage(checkMode(mode)))),
    (HasDirectorUTRId(0), hasUtrNo, directorNoUtrReasonPage(mode), Some(directorNoUtrReasonPage(checkMode(mode)))),
    (CompanyDirectorAddressPostCodeLookupId(index), emptyAnswers, addressListPage(mode), None),
    (DirectorAddressYearsId(index), addressYearsOverAYear, directorEmailPage(mode), Some(checkYourAnswersPage(mode))),
    (DirectorAddressYearsId(index), addressYearsUnderAYear, paPostCodePage(mode), Some(paPostCodePage(checkMode(mode)))),
    (DirectorAddressYearsId(index), emptyAnswers, sessionExpiredPage, Some(sessionExpiredPage)),
    (DirectorPreviousAddressPostCodeLookupId(index), emptyAnswers, paAddressListPage(mode), None),
    (DirectorPreviousAddressId(index), defaultAnswers, directorEmailPage(mode), Some(checkYourAnswersPage(mode))),
    (DirectorEmailId(index), defaultAnswers, directorPhonePage(mode), Some(checkYourAnswersPage(mode))),
    (DirectorPhoneId(index), defaultAnswers, checkYourAnswersPage(mode), Some(checkYourAnswersPage(mode))),
    (CheckYourAnswersId, emptyAnswers, addDirectorsPage(mode), None)
  )

  private def normalOnlyRoutes(): Seq[(Identifier, UserAnswers, Call, Option[Call])] = Seq(
    (AddCompanyDirectorsId, addCompanyDirectorsFalse, companyReviewPage(NormalMode), None),
    (DirectorEnterNINOId(index), emptyAnswers, directorHasUtrPage(NormalMode), Some(checkYourAnswersPage(NormalMode))),
    (DirectorNoNINOReasonId(index), emptyAnswers, directorHasUtrPage(NormalMode), Some(checkYourAnswersPage(NormalMode))),
    (DirectorEnterUTRId(0), emptyAnswers, addressPostCodePage(NormalMode), Some(checkYourAnswersPage(NormalMode))),
    (DirectorNoUTRReasonId(0), emptyAnswers, addressPostCodePage(NormalMode), Some(checkYourAnswersPage(NormalMode))),
    (DirectorAddressId(index), emptyAnswers, directorAddressYearsPage(NormalMode), Some(checkYourAnswersPage(NormalMode))),
    (MoreThanTenDirectorsId, emptyAnswers, companyReviewPage(NormalMode), None)
  )

  private def updateOnlyRoutes(): Seq[(Identifier, UserAnswers, Call, Option[Call])] = Seq(
    (AddCompanyDirectorsId, addCompanyDirectorsFalse, anyMoreChangesPage, None),
    (MoreThanTenDirectorsId, emptyAnswers, anyMoreChangesPage, None),
    (DirectorEnterNINOId(index), defaultAnswers, directorHasUtrPage(UpdateMode), None),
    (DirectorEnterNINOId(index), existingDirectorInUpdate(index), anyMoreChangesPage, None),
    (DirectorNoNINOReasonId(index), defaultAnswers, directorHasUtrPage(UpdateMode), None),
    (DirectorNoNINOReasonId(index), existingDirectorInUpdate(index), anyMoreChangesPage, None),
    (DirectorEnterUTRId(0), defaultAnswers, addressPostCodePage(UpdateMode), None),
    (DirectorEnterUTRId(0), existingDirectorInUpdate(0), anyMoreChangesPage, None),
    (DirectorNoUTRReasonId(0), defaultAnswers, addressPostCodePage(UpdateMode), None),
    (DirectorNoUTRReasonId(0), existingDirectorInUpdate(0), anyMoreChangesPage, None),
    (DirectorAddressId(index), defaultAnswers, directorAddressYearsPage(UpdateMode), None),
    (DirectorAddressId(index), existingDirectorInUpdate(index), confirmPreviousAddressPage, None),
    (DirectorAddressYearsId(index), addressYearsOverAYearExistingDirector, anyMoreChangesPage, None),
    (DirectorAddressYearsId(index), addressYearsUnderAYearExistingDirector, confirmPreviousAddressPage, None),
    (DirectorConfirmPreviousAddressId(index), confirmPreviousAddressNotSame, paPostCodePage(UpdateMode), None),
    (DirectorConfirmPreviousAddressId(index), confirmPreviousAddressSame, anyMoreChangesPage, None),
    (DirectorPreviousAddressId(index), existingDirectorInUpdate(index), anyMoreChangesPage, None),
    (DirectorEmailId(index), existingDirectorInUpdate(index), anyMoreChangesPage, None),
    (DirectorPhoneId(index), existingDirectorInUpdate(index), anyMoreChangesPage, None)
  )

  private def normalRoutes(): TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Next Page (Check Mode)"),
    normalOnlyRoutes ++ routes(NormalMode): _*
  )

  private def updateRoutes(): TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Next Page (Check Mode"),
    updateOnlyRoutes ++ routes(UpdateMode): _*
  )

  //scalastyle:on line.size.limit

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, normalRoutes(), dataDescriber)
    behave like navigatorWithRoutes(navigator, updateRoutes(), dataDescriber, UpdateMode)
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

  private def previousAddressPage(mode: Mode): Call = routes.DirectorPreviousAddressController.onPageLoad(mode, index)

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
