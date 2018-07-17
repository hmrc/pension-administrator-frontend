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
import identifiers.Identifier
import identifiers.register.partnership.partners._
import identifiers.register.partnership.{AddPartnersId, MoreThanTenPartnersId}
import models._
import models.requests.IdentifiedRequest
import org.scalatest.OptionValues
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier
import utils.{NavigatorBehaviour, UserAnswers}
import controllers.register.partnership.partners.routes

class PartnerNavigatorSpec extends SpecBase with MockitoSugar with NavigatorBehaviour {
  import PartnerNavigatorSpec._
  val navigator = new PartnerNavigator(FakeDataCacheConnector, frontendAppConfig)

  //scalastyle:off line.size.limit
  def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id",                                       "User Answers",                "Next Page (Normal Mode)",       "Save(NormalMode)", "Next Page (Check Mode)",             "Save(CheckMode"),
    (AddPartnersId,                             addPartnersFalse,              partnershipReviewPage,           true,               Some(partnershipReviewPage),          true),
    (AddPartnersId,                             addPartnersMoreThan10,         moreThanTenPartnersPage,         true,               Some(moreThanTenPartnersPage),        true),
    (AddPartnersId,                             addPartnersTrue,               partnerDetailsPage,              true,               Some(partnerDetailsPage),             true),
    (MoreThanTenPartnersId,                     emptyAnswers,                  partnershipReviewPage,           true,               None,                                 false),
    (PartnerDetailsId(0),                       emptyAnswers,                  partnerNinoPage,                 true,               Some(checkYourAnswersPage),           true),
    (PartnerNinoId(0),                          emptyAnswers,                  partnerUniqueTaxReferencePage,   true,               Some(checkYourAnswersPage),           true),
    (PartnerUniqueTaxReferenceId(0),            emptyAnswers,                  addressPostCodePage(NormalMode), true,               Some(checkYourAnswersPage),           true),
    (PartnerAddressPostCodeLookupId(0),         emptyAnswers,                  addressListPage(NormalMode),     false,              Some(addressListPage(CheckMode)),     false),
    (PartnerAddressListId(0),                   emptyAnswers,                  addressPage(NormalMode),         true,               Some(addressPage(CheckMode)),         true),
    (PartnerAddressId(0),                       emptyAnswers,                  partnerAddressYearsPage,         true,               Some(checkYourAnswersPage),           true),
    (PartnerAddressYearsId(0),                  addressYearsOverAYear,         partnerContactDetailsPage,       true,               Some(checkYourAnswersPage),           true),
    (PartnerAddressYearsId(0),                  addressYearsUnderAYear,        paPostCodePage(NormalMode),      true,               Some(paPostCodePage(CheckMode)),      true),
    (PartnerAddressYearsId(0),                  emptyAnswers,                  sessionExpiredPage,              false,              Some(sessionExpiredPage),             false),
    (PartnerPreviousAddressPostCodeLookupId(0), emptyAnswers,                  paAddressListPage(NormalMode),   false,              Some(paAddressListPage(CheckMode)),   false),
    (PartnerPreviousAddressListId(0),           emptyAnswers,                  previousAddressPage(NormalMode), true,               Some(previousAddressPage(CheckMode)), true),
    (PartnerPreviousAddressId(0),               emptyAnswers,                  partnerContactDetailsPage,       true,               Some(checkYourAnswersPage),           true),
    (PartnerContactDetailsId(0),                emptyAnswers,                  checkYourAnswersPage,            true,               Some(checkYourAnswersPage),           true),
    (CheckYourAnswersId,                        emptyAnswers,                  addPartnersPage,                 true,               None,                                 false)
  )

  //scalastyle:on line.size.limit

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeDataCacheConnector, routes(), dataDescriber)
  }
}

object PartnerNavigatorSpec extends OptionValues {

  private lazy val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  private lazy val checkYourAnswersPage = routes.CheckYourAnswersController.onPageLoad(0)
  private lazy val partnershipReviewPage = controllers.register.partnership.routes.PartnershipReviewController.onPageLoad()
  private lazy val moreThanTenPartnersPage = controllers.register.partnership.routes.MoreThanTenPartnersController.onPageLoad(NormalMode)
  private lazy val partnerDetailsPage = routes.PartnerDetailsController.onPageLoad(NormalMode, 0)
  private lazy val partnerNinoPage = routes.PartnerNinoController.onPageLoad(NormalMode, 0)
  private lazy val partnerUniqueTaxReferencePage = routes.PartnerUniqueTaxReferenceController.onPageLoad(NormalMode, 0)
  private lazy val partnerAddressYearsPage = routes.PartnerAddressYearsController.onPageLoad(NormalMode, 0)
  private lazy val partnerContactDetailsPage = routes.PartnerContactDetailsController.onPageLoad(NormalMode, 0)
  private lazy val addPartnersPage = controllers.register.partnership.routes.AddPartnerController.onPageLoad()

  def paPostCodePage(mode: Mode): Call = routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, 0)
  def paAddressListPage(mode: Mode): Call = routes.PartnerPreviousAddressListController.onPageLoad(mode, 0)
  def previousAddressPage(mode: Mode): Call = routes.PartnerPreviousAddressController.onPageLoad(mode, 0)
  def addressPostCodePage(mode: Mode): Call = routes.PartnerAddressPostCodeLookupController.onPageLoad(mode, 0)
  def addressListPage(mode: Mode): Call = routes.PartnerAddressListController.onPageLoad(mode, 0)
  def addressPage(mode: Mode): Call = routes.PartnerAddressController.onPageLoad(mode, 0)

  private def data = {
    (0 to 19).map(index => Json.obj(
      PartnerDetailsId.toString -> PersonDetails(s"testFirstName$index", None, s"testLastName$index", LocalDate.now, isDeleted = (index%2==0))
    )).toArray
  }
  val emptyAnswers = UserAnswers(Json.obj())
  private val addressYearsOverAYear = UserAnswers(Json.obj())
    .set(PartnerAddressYearsId(0))(AddressYears.OverAYear).asOpt.value
  private val addressYearsUnderAYear = UserAnswers(Json.obj())
    .set(PartnerAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value
  private val addPartnersFalse = UserAnswers(Json.obj())
    .set(AddPartnersId)(false).asOpt.value
  private val addPartnersTrue = UserAnswers(Json.obj())
    .set(AddPartnersId)(true).asOpt.value

  val addPartnersMoreThan10 = UserAnswers(Json.obj(
    "partners" -> data))

  implicit val ex: IdentifiedRequest = new IdentifiedRequest() {val externalId: String = "test-external-id"}
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private def dataDescriber(answers: UserAnswers): String = answers.toString

}


