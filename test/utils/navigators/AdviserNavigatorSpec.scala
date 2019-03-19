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
import identifiers.register.PAInDeclarationJourneyId
import identifiers.register.adviser._
import models.requests.IdentifiedRequest
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor6
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{NavigatorBehaviour, UserAnswers}

class AdviserNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import AdviserNavigatorSpec._

  val navigator = new AdviserNavigator(FakeUserAnswersCacheConnector)

  def routes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (NormalMode)", "Save(NormalMode)", "Next Page (CheckMode)", "Save(CheckMode"),
    (AdviserNameId, emptyAnswers, adviserDetailsPage(NormalMode), false, Some(checkYourAnswersPage(Mode.journeyMode(CheckMode))), false),
    (AdviserDetailsId, emptyAnswers, adviserPostCodeLookUpPage(NormalMode), false, Some(checkYourAnswersPage(Mode.journeyMode(CheckMode))), false),
    (AdviserAddressPostCodeLookupId, emptyAnswers, adviserAddressListPage(NormalMode), false, Some(adviserAddressListPage(CheckMode)), false),
    (AdviserAddressListId, emptyAnswers, adviserAddressPage(NormalMode), false, Some(adviserAddressPage(CheckMode)), false),
    (AdviserAddressId, emptyAnswers, checkYourAnswersPage(NormalMode), false, Some(checkYourAnswersPage(Mode.journeyMode(CheckMode))), false),
    (CheckYourAnswersId, emptyAnswers, declarationFitAndProperPage, false, None, false)
  )

  def updateModeRoutes(): TableFor6[Identifier, UserAnswers, Call, Boolean, Option[Call], Boolean] = Table(
    ("Id", "User Answers", "Next Page (NormalMode)", "Save(NormalMode)", "Next Page (CheckMode)", "Save(CheckMode"),
    (AdviserNameId, emptyAnswers, adviserDetailsPage(UpdateMode), false, Some(checkYourAnswersPage(UpdateMode)), false),
    (AdviserDetailsId, emptyAnswers, haveMoreChangesPage, false, None, false),
    (AdviserDetailsId, adviserUpdated, adviserPostCodeLookUpPage(UpdateMode), false,  Some(checkYourAnswersPage(UpdateMode)), false),
    (AdviserAddressPostCodeLookupId, emptyAnswers, adviserAddressListPage(UpdateMode), false, None, false),
    (AdviserAddressListId, emptyAnswers, adviserAddressPage(UpdateMode), false, None, false),
    (AdviserAddressId, emptyAnswers, haveMoreChangesPage, false, Some(checkYourAnswersPage(UpdateMode)), false),
    (AdviserAddressId, adviserUpdated, checkYourAnswersPage(UpdateMode), false, None, false),
    (CheckYourAnswersId, emptyAnswers, haveMoreChangesPage, false, None, false),
    (CheckYourAnswersId, declarationPensionAdvisorTrue, variationDeclarationFitAndProperPage, false, None, false)
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like navigatorWithRoutes(navigator, FakeUserAnswersCacheConnector, updateModeRoutes(), dataDescriber, UpdateMode)
  }
}

object AdviserNavigatorSpec extends OptionValues {
  lazy val emptyAnswers = UserAnswers(Json.obj())
  lazy val adviserUpdated = UserAnswers(Json.obj()).set(IsNewAdviserId)(true).asOpt.get
  lazy val declarationPensionAdvisorTrue = UserAnswers(Json.obj()).set(PAInDeclarationJourneyId)(true).asOpt.get

  private def adviserPostCodeLookUpPage(mode: Mode): Call = controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onPageLoad(mode)
  private def adviserDetailsPage(mode: Mode): Call = controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(mode)
  private def adviserAddressListPage(mode: Mode): Call = controllers.register.adviser.routes.AdviserAddressListController.onPageLoad(mode)
  private def adviserAddressPage(mode: Mode): Call = controllers.register.adviser.routes.AdviserAddressController.onPageLoad(mode)
  private  def checkYourAnswersPage(mode: Mode): Call = controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad(mode)
  private val variationDeclarationFitAndProperPage: Call = controllers.register.routes.VariationDeclarationFitAndProperController.onPageLoad()

  lazy val declarationFitAndProperPage: Call = controllers.register.routes.DeclarationFitAndProperController.onPageLoad()
  lazy val haveMoreChangesPage: Call = controllers.register.routes.AnyMoreChangesController.onPageLoad()

  implicit val ex: IdentifiedRequest = new IdentifiedRequest() {
    val externalId: String = "test-external-id"
  }

}
