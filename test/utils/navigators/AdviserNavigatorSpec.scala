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
import connectors.FakeUserAnswersCacheConnector
import identifiers.Identifier
import identifiers.register.adviser._
import models.requests.IdentifiedRequest
import models.{CheckMode, Mode, NormalMode}
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import uk.gov.hmrc.http.HeaderCarrier
import utils.{NavigatorBehaviour, UserAnswers}

//scalastyle:off line.size.limit

class AdviserNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import AdviserNavigatorSpec._

  val navigator = new AdviserNavigator(FakeUserAnswersCacheConnector)

  def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (NormalMode)", "Save(NormalMode)", "Next Page (CheckMode)", "Save(CheckMode"),
    (AdviserNameId, emptyAnswers, adviserDetailsPage, true, Some(checkYourAnswersPage), false),
    (AdviserDetailsId, emptyAnswers, adviserPostCodeLookUpPage, true, Some(checkYourAnswersPage), false),
    (AdviserAddressPostCodeLookupId, emptyAnswers, adviserAddressListPage(NormalMode), false, Some(adviserAddressListPage(CheckMode)), false),
    (AdviserAddressListId, emptyAnswers, adviserAddressPage(NormalMode), true, Some(adviserAddressPage(CheckMode)), true),
    (AdviserAddressId, emptyAnswers, checkYourAnswersPage, true, Some(checkYourAnswersPage), false),
    (CheckYourAnswersId, emptyAnswers, declarationFitAndProperPage, true, None, false)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, routes(), dataDescriber)
  }
}

object AdviserNavigatorSpec extends OptionValues {
  lazy val emptyAnswers = UserAnswers(Json.obj())
  lazy val adviserPostCodeLookUpPage: Call = controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onPageLoad(NormalMode)
  lazy val adviserDetailsPage: Call = controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(NormalMode)

  def adviserAddressListPage(mode: Mode): Call = controllers.register.adviser.routes.AdviserAddressListController.onPageLoad(mode)

  def adviserAddressPage(mode: Mode): Call = controllers.register.adviser.routes.AdviserAddressController.onPageLoad(mode)

  lazy val checkYourAnswersPage: Call = controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad()
  lazy val declarationFitAndProperPage: Call = controllers.register.routes.DeclarationFitAndProperController.onPageLoad()

  implicit val ex: IdentifiedRequest = new IdentifiedRequest() {
    val externalId: String = "test-external-id"
  }

}
