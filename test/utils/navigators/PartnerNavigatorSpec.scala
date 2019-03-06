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
import controllers.register.partnership.partners.routes
import identifiers.Identifier
import identifiers.register.partnership.partners._
import identifiers.register.partnership.{AddPartnersId, MoreThanTenPartnersId}
import models.Mode._
import models._
import models.requests.IdentifiedRequest
import org.scalatest.OptionValues
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{NavigatorBehaviour, UserAnswers}

class PartnerNavigatorSpec extends SpecBase with MockitoSugar with NavigatorBehaviour {

  import PartnerNavigatorSpec._

  val navigator = new PartnerNavigator(FakeUserAnswersCacheConnector, frontendAppConfig)

  //scalastyle:off line.size.limit
  def routes(mode: Mode): Seq[(Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean)] = Seq(
    (AddPartnersId, addPartnersMoreThan10, moreThanTenPartnersPage(mode), true, Some(moreThanTenPartnersPage(checkMode(mode))), true),
    (AddPartnersId, addPartnersTrue, partnerDetailsPage(mode), true, Some(partnerDetailsPage(checkMode(mode))), true),
    (PartnerDetailsId(0), emptyAnswers, partnerNinoPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (PartnerNinoId(0), emptyAnswers, partnerUniqueTaxReferencePage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (PartnerUniqueTaxReferenceId(0), emptyAnswers, addressPostCodePage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (PartnerAddressPostCodeLookupId(0), emptyAnswers, addressListPage(mode), false, Some(addressListPage(checkMode(mode))), false),
    (PartnerAddressListId(0), emptyAnswers, addressPage(mode), true, Some(addressPage(checkMode(mode))), true),
    (PartnerAddressId(0), emptyAnswers, partnerAddressYearsPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (PartnerAddressYearsId(0), addressYearsOverAYear, partnerContactDetailsPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (PartnerAddressYearsId(0), addressYearsUnderAYear, paPostCodePage(mode), true, Some(paPostCodePage(checkMode(mode))), true),
    (PartnerAddressYearsId(0), emptyAnswers, sessionExpiredPage, false, Some(sessionExpiredPage), false),
    (PartnerPreviousAddressPostCodeLookupId(0), emptyAnswers, paAddressListPage(mode), false, Some(paAddressListPage(checkMode(mode))), false),
    (PartnerPreviousAddressListId(0), emptyAnswers, previousAddressPage(mode), true, Some(previousAddressPage(checkMode(mode))), true),
    (PartnerPreviousAddressId(0), emptyAnswers, partnerContactDetailsPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (PartnerContactDetailsId(0), emptyAnswers, checkYourAnswersPage(mode), true, Some(checkYourAnswersPage(mode)), true),
    (CheckYourAnswersId, emptyAnswers, addPartnersPage(mode), true, None, false)
  )

  def normalOnlyRoutes: Seq[(Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean)] =
    Seq((AddPartnersId, addPartnersFalse, partnershipReviewPage(NormalMode), true, None, true),
    (MoreThanTenPartnersId, emptyAnswers, partnershipReviewPage(NormalMode), true, None, false)
  )

  def updateOnlyRoutes: Seq[(Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean)] = Seq(
  (AddPartnersId, addPartnersFalse, anyMoreChangesPage, true, None, true),
  (MoreThanTenPartnersId, emptyAnswers, anyMoreChangesPage, true, None, false)
  )

  def normalRoutes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (Normal Mode)", "Save(NormalMode)", "Next Page (Check Mode)", "Save(CheckMode"),
    normalOnlyRoutes ++ routes(NormalMode): _*
  )

  def updateRoutes: TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
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

object PartnerNavigatorSpec extends OptionValues {

  private lazy val sessionExpiredPage = controllers.routes.SessionExpiredController.onPageLoad()
  private lazy val anyMoreChangesPage = controllers.vary.routes.AnyMoreChangesController.onPageLoad()
  def checkYourAnswersPage(mode: Mode) = routes.CheckYourAnswersController.onPageLoad(0, mode)
  def partnershipReviewPage(mode: Mode) = controllers.register.partnership.routes.PartnershipReviewController.onPageLoad()
  def partnerNinoPage(mode: Mode) = routes.PartnerNinoController.onPageLoad(mode, 0)
  def partnerUniqueTaxReferencePage(mode: Mode) = routes.PartnerUniqueTaxReferenceController.onPageLoad(mode, 0)
  def partnerAddressYearsPage(mode: Mode) = routes.PartnerAddressYearsController.onPageLoad(mode, 0)
  def partnerContactDetailsPage(mode: Mode) = routes.PartnerContactDetailsController.onPageLoad(mode, 0)
  def addPartnersPage(mode: Mode) = controllers.register.partnership.routes.AddPartnerController.onPageLoad(mode)

  def moreThanTenPartnersPage(mode: Mode) = controllers.register.partnership.routes.MoreThanTenPartnersController.onPageLoad(mode)

  def partnerDetailsPage(mode: Mode) = routes.PartnerDetailsController.onPageLoad(mode, 0)

  def paPostCodePage(mode: Mode): Call = routes.PartnerPreviousAddressPostCodeLookupController.onPageLoad(mode, 0)

  def paAddressListPage(mode: Mode): Call = routes.PartnerPreviousAddressListController.onPageLoad(mode, 0)

  def previousAddressPage(mode: Mode): Call = routes.PartnerPreviousAddressController.onPageLoad(mode, 0)

  def addressPostCodePage(mode: Mode): Call = routes.PartnerAddressPostCodeLookupController.onPageLoad(mode, 0)

  def addressListPage(mode: Mode): Call = routes.PartnerAddressListController.onPageLoad(mode, 0)

  def addressPage(mode: Mode): Call = routes.PartnerAddressController.onPageLoad(mode, 0)

  private def data = {
    (0 to 19).map(index => Json.obj(
      PartnerDetailsId.toString -> PersonDetails(s"testFirstName$index", None, s"testLastName$index", LocalDate.now, isDeleted = (index % 2 == 0))
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

  implicit val ex: IdentifiedRequest = new IdentifiedRequest() {
    val externalId: String = "test-external-id"
  }
}


