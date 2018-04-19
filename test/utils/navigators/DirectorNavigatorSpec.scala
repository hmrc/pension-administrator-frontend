/*
 * Copyright 2018 HM Revenue & Customs
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
import controllers.register.company.directors.routes
import identifiers.Identifier
import identifiers.register.company.{AddCompanyDirectorsId, MoreThanTenDirectorsId}
import identifiers.register.company.directors._
import models.register.company.directors.DirectorDetails
import models.{AddressYears, CheckMode, Mode, NormalMode}
import org.scalatest.OptionValues
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.TableFor4
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.UserAnswers

class DirectorNavigatorSpec extends SpecBase with MockitoSugar with NavigatorBehaviour {
  import DirectorNavigatorSpec._
  val navigator = new DirectorNavigator(frontendAppConfig)

  private val routes: TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id",                                        "User Answers",                 "Next Page (Normal Mode)",            "Next Page (Check Mode)"),
    (AddCompanyDirectorsId,                         addCompanyDirectorsFalse,       companyReviewPage,                    None),
    (AddCompanyDirectorsId,                         addCompanyDirectorsMoreThan10,  moreThanTenDirectorsPage,             None),
    (AddCompanyDirectorsId,                         addCompanyDirectorsTrue,        directorDetailsPage,                  None),
    (MoreThanTenDirectorsId,                        emptyAnswers,                   companyReviewPage,                    None),
    (DirectorDetailsId(0),                          emptyAnswers,                   directorNinoPage,                     Some(checkYourAnswersPage)),
    (DirectorNinoId(0),                             emptyAnswers,                   directorUniqueTaxReferencePage,       Some(checkYourAnswersPage)),
    (DirectorUniqueTaxReferenceId(0),               emptyAnswers,                   addressPostCodePage(NormalMode),      Some(checkYourAnswersPage)),
    (CompanyDirectorAddressPostCodeLookupId(0),     emptyAnswers,                   addressListPage(NormalMode),          Some(addressListPage(CheckMode))),
    (CompanyDirectorAddressListId(0),               emptyAnswers,                   addressPage(NormalMode),              Some(addressPage(CheckMode))),
    (DirectorAddressId(0),                          emptyAnswers,                   directorAddressYearsPage,             Some(checkYourAnswersPage)),
    (DirectorAddressYearsId(0),                     addressYearsOverAYear,          directorContactDetailsPage,           Some(checkYourAnswersPage)),
    (DirectorAddressYearsId(0),                     addressYearsUnderAYear,         paPostCodePage(NormalMode),           Some(paPostCodePage(CheckMode))),
    (DirectorAddressYearsId(0),                     emptyAnswers,                   sessionExpiredPage,                   None),
    (DirectorPreviousAddressPostCodeLookupId(0),    emptyAnswers,                   paAddressListPage(NormalMode),        Some(paAddressListPage(CheckMode))),
    (DirectorPreviousAddressListId(0),              emptyAnswers,                   previousAddressPage(NormalMode),      Some(previousAddressPage(CheckMode))),
    (DirectorPreviousAddressId(0),                  emptyAnswers,                   directorContactDetailsPage,           Some(checkYourAnswersPage)),
    (DirectorContactDetailsId(0),                   emptyAnswers,                   checkYourAnswersPage,                 Some(checkYourAnswersPage))
  )

  navigator.getClass.getSimpleName must {
    behave like navigatorWithRoutes(navigator, routes)
  }
}

object DirectorNavigatorSpec extends OptionValues {

  val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  val checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad(0)
  val companyReviewPage = controllers.register.company.routes.CompanyReviewController.onPageLoad()
  val moreThanTenDirectorsPage = controllers.register.company.routes.MoreThanTenDirectorsController.onPageLoad(NormalMode)
  val directorDetailsPage = routes.DirectorDetailsController.onPageLoad(NormalMode, 0)
  val directorNinoPage = routes.DirectorNinoController.onPageLoad(NormalMode, 0)
  val directorUniqueTaxReferencePage = routes.DirectorUniqueTaxReferenceController.onPageLoad(NormalMode, 0)
  def addressPostCodePage(mode: Mode) = routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(mode, 0)
  def addressListPage(mode: Mode) = routes.CompanyDirectorAddressListController.onPageLoad(mode, 0)
  def addressPage(mode: Mode) = routes.DirectorAddressController.onPageLoad(mode, 0)
  val directorAddressYearsPage = routes.DirectorAddressYearsController.onPageLoad(NormalMode, 0)
  val directorContactDetailsPage = routes.DirectorContactDetailsController.onPageLoad(NormalMode, 0)
  def paPostCodePage(mode: Mode) = routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(mode, 0)
  def paAddressListPage(mode: Mode) = routes.DirectorPreviousAddressListController.onPageLoad(mode, 0)
  def previousAddressPage(mode: Mode) = routes.DirectorPreviousAddressController.onPageLoad(mode, 0)

  private def data = {
    (0 to 9).map(index => Json.obj(
      DirectorDetailsId.toString -> DirectorDetails(s"testFirstName$index", None, s"testLastName$index", LocalDate.now))
    ).toArray
  }
  val emptyAnswers = new UserAnswers(Json.obj())
  val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value
  val addCompanyDirectorsFalse = UserAnswers(Json.obj())
    .set(AddCompanyDirectorsId)(false).asOpt.value
  val addCompanyDirectorsTrue = UserAnswers(Json.obj())
    .set(AddCompanyDirectorsId)(true).asOpt.value

  val addCompanyDirectorsMoreThan10 = UserAnswers(Json.obj(
    "directors" -> data))
}