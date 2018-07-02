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
import connectors.FakeDataCacheConnector
import controllers.register.company.directors.routes
import identifiers.Identifier
import identifiers.register.company.{AddCompanyDirectorsId, MoreThanTenDirectorsId}
import identifiers.register.company.directors._
import models.requests.IdentifiedRequest
import models._
import org.scalatest.OptionValues
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier
import utils.{NavigatorBehaviour, UserAnswers}

class DirectorNavigatorSpec extends SpecBase with MockitoSugar with NavigatorBehaviour {
  import DirectorNavigatorSpec._
  val navigator = new DirectorNavigator(FakeDataCacheConnector, frontendAppConfig)

  //scalastyle:off line.size.limit
  def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                       "User Answers",                "Next Page (Normal Mode)",       "Save(NormalMode)", "Next Page (Check Mode)",             "Save(CheckMode"),
    (AddCompanyDirectorsId,                      addCompanyDirectorsFalse,      companyReviewPage,               true,               Some(companyReviewPage),              true),
    (AddCompanyDirectorsId,                      addCompanyDirectorsMoreThan10, moreThanTenDirectorsPage,        true,               Some(moreThanTenDirectorsPage),       true),
    (AddCompanyDirectorsId,                      addCompanyDirectorsTrue,       directorDetailsPage,             true,               Some(directorDetailsPage),            true),
    (MoreThanTenDirectorsId,                     emptyAnswers,                  companyReviewPage,               true,               None,                                 false),
    (DirectorDetailsId(0),                       emptyAnswers,                  directorNinoPage,                true,               Some(checkYourAnswersPage),           true),
    (DirectorNinoId(0),                          emptyAnswers,                  directorUniqueTaxReferencePage,  true,               Some(checkYourAnswersPage),           true),
    (DirectorUniqueTaxReferenceId(0),            emptyAnswers,                  addressPostCodePage(NormalMode), true,               Some(checkYourAnswersPage),           true),
    (CompanyDirectorAddressPostCodeLookupId(0),  emptyAnswers,                  addressListPage(NormalMode),     false,              Some(addressListPage(CheckMode)),     false),
    (CompanyDirectorAddressListId(0),            emptyAnswers,                  addressPage(NormalMode),         true,               Some(addressPage(CheckMode)),         true),
    (DirectorAddressId(0),                       emptyAnswers,                  directorAddressYearsPage,        true,               Some(checkYourAnswersPage),           true),
    (DirectorAddressYearsId(0),                  addressYearsOverAYear,         directorContactDetailsPage,      true,               Some(checkYourAnswersPage),           true),
    (DirectorAddressYearsId(0),                  addressYearsUnderAYear,        paPostCodePage(NormalMode),      true,               Some(paPostCodePage(CheckMode)),      true),
    (DirectorAddressYearsId(0),                  emptyAnswers,                  sessionExpiredPage,              false,              Some(sessionExpiredPage),             false),
    (DirectorPreviousAddressPostCodeLookupId(0), emptyAnswers,                  paAddressListPage(NormalMode),   false,              Some(paAddressListPage(CheckMode)),   false),
    (DirectorPreviousAddressListId(0),           emptyAnswers,                  previousAddressPage(NormalMode), true,               Some(previousAddressPage(CheckMode)), true),
    (DirectorPreviousAddressId(0),               emptyAnswers,                  directorContactDetailsPage,      true,               Some(checkYourAnswersPage),           true),
    (DirectorContactDetailsId(0),                emptyAnswers,                  checkYourAnswersPage,            true,               Some(checkYourAnswersPage),           true),
    (CheckYourAnswersId,                         emptyAnswers,                  addDirectorsPage,                 true,               None,                                 false)
  )

  //scalastyle:on line.size.limit

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routes(), dataDescriber)
  }
}

object DirectorNavigatorSpec extends OptionValues {

  private lazy val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  private lazy val checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad(0)
  private lazy val companyReviewPage = controllers.register.company.routes.CompanyReviewController.onPageLoad()
  private lazy val moreThanTenDirectorsPage = controllers.register.company.routes.MoreThanTenDirectorsController.onPageLoad(NormalMode)
  private lazy val directorDetailsPage = routes.DirectorDetailsController.onPageLoad(NormalMode, 0)
  private lazy val directorNinoPage = routes.DirectorNinoController.onPageLoad(NormalMode, 0)
  private lazy val directorUniqueTaxReferencePage = routes.DirectorUniqueTaxReferenceController.onPageLoad(NormalMode, 0)
  private lazy val directorAddressYearsPage = routes.DirectorAddressYearsController.onPageLoad(NormalMode, 0)
  private lazy val directorContactDetailsPage = routes.DirectorContactDetailsController.onPageLoad(NormalMode, 0)
  private lazy val addDirectorsPage = controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode)

  def paPostCodePage(mode: Mode): Call = routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(mode, 0)
  def paAddressListPage(mode: Mode): Call = routes.DirectorPreviousAddressListController.onPageLoad(mode, 0)
  def previousAddressPage(mode: Mode): Call = routes.DirectorPreviousAddressController.onPageLoad(mode, 0)
  def addressPostCodePage(mode: Mode): Call = routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(mode, 0)
  def addressListPage(mode: Mode): Call = routes.CompanyDirectorAddressListController.onPageLoad(mode, 0)
  def addressPage(mode: Mode): Call = routes.DirectorAddressController.onPageLoad(mode, 0)

  private def data = {
    (0 to 19).map(index => Json.obj(
      DirectorDetailsId.toString -> PersonDetails(s"testFirstName$index", None, s"testLastName$index", LocalDate.now, isDeleted = (index%2==0))
    )).toArray
  }
  val emptyAnswers = UserAnswers(Json.obj())
  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(DirectorAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value
  private val addCompanyDirectorsFalse = UserAnswers(Json.obj())
    .set(AddCompanyDirectorsId)(false).asOpt.value
  private val addCompanyDirectorsTrue = UserAnswers(Json.obj())
    .set(AddCompanyDirectorsId)(true).asOpt.value

  val addCompanyDirectorsMoreThan10 = UserAnswers(Json.obj(
    "directors" -> data))

  implicit val ex: IdentifiedRequest = new IdentifiedRequest() {val externalId: String = "test-external-id"}
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private def dataDescriber(answers: UserAnswers): String = answers.toString

}
