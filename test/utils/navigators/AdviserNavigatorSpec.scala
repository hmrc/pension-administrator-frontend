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
import identifiers.register.{AreYouInUKId, PAInDeclarationJourneyId}
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor4
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{NavigatorBehaviour, UserAnswers}

class AdviserNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import AdviserNavigatorSpec._

  val navigator = new AdviserNavigator

  def routes(): TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id", "User Answers", "Next Page (NormalMode)", "Next Page (CheckMode)"),
    (AdviserNameId, emptyAnswers, adviserPostCodeLookUpPage(NormalMode), Some(checkYourAnswersPage(Mode.journeyMode(CheckMode)))),
    (AdviserAddressPostCodeLookupId, emptyAnswers, adviserAddressListPage(NormalMode), Some(adviserAddressListPage(CheckMode))),
    (AdviserAddressListId, emptyAnswers, adviserAddressPage(NormalMode), Some(adviserAddressPage(CheckMode))),
    (AdviserAddressId, emptyAnswers, adviserEmailPage(NormalMode), Some(checkYourAnswersPage(CheckMode))),
    (AdviserEmailId, emptyAnswers, adviserPhonePage(NormalMode), Some(checkYourAnswersPage(CheckMode))),
    (AdviserPhoneId, emptyAnswers, checkYourAnswersPage(NormalMode), Some(checkYourAnswersPage(Mode.journeyMode(CheckMode)))),

    (CheckYourAnswersId, emptyAnswers, declarationFitAndProperPage, None)
  )

  def updateModeRoutes(): TableFor4[Identifier, UserAnswers, Call, Option[Call]] = Table(
    ("Id", "User Answers", "Next Page (NormalMode)", "Next Page (CheckMode)"),
    (AdviserNameId, emptyAnswers, haveMoreChangesPage, Some(checkYourAnswersPage(UpdateMode))),
    (AdviserNameId, adviserUpdated, adviserPostCodeLookUpPage(UpdateMode), Some(checkYourAnswersPage(UpdateMode))),
    (AdviserAddressPostCodeLookupId, emptyAnswers, adviserAddressListPage(UpdateMode), Some(adviserAddressListPage(UpdateMode))),
    (AdviserAddressListId, emptyAnswers, adviserAddressPage(UpdateMode), Some(adviserAddressPage(UpdateMode))),
    (AdviserAddressId, emptyAnswers, haveMoreChangesPage, Some(checkYourAnswersPage(UpdateMode))),
    (AdviserAddressId, adviserUpdated, adviserEmailPage(UpdateMode), None),
    (AdviserEmailId, emptyAnswers, haveMoreChangesPage, None),
    (AdviserEmailId, adviserUpdated, adviserPhonePage(UpdateMode),  Some(checkYourAnswersPage(UpdateMode))),
    (AdviserPhoneId, emptyAnswers, haveMoreChangesPage,  Some(checkYourAnswersPage(UpdateMode))),
    (AdviserPhoneId, adviserUpdated, checkYourAnswersPage(UpdateMode),  Some(checkYourAnswersPage(UpdateMode))),

    (CheckYourAnswersId, emptyAnswers, haveMoreChangesPage, None),
    (CheckYourAnswersId, declarationPensionAdvisorTrue, variationDeclarationFitAndProperPage, None),
    (invalidIdForNavigator, emptyAnswers, defaultPage, Some(defaultPage))
  )

  navigator.getClass.getSimpleName must {
    appRunning()
    behave like nonMatchingNavigator(navigator)
    behave like navigatorWithRoutes(navigator, updateModeRoutes(), dataDescriber, UpdateMode)
  }
}

object AdviserNavigatorSpec extends OptionValues {
  private val invalidIdForNavigator = AreYouInUKId
  private val address = Address("line 1", "line 2", Some("line 3"), Some("line 4"), None, "UK")
  private val adviserUpdated = UserAnswers(Json.obj()).set(IsNewAdviserId)(value = true).asOpt.get
  private val adviserUpdatedWithAddressOnly = UserAnswers(Json.obj())
    .set(IsNewAdviserId)(value = true).asOpt.get
    .set(AdviserAddressId)(address).asOpt.get

  private val declarationPensionAdvisorTrue = UserAnswers(Json.obj()).set(PAInDeclarationJourneyId)(true).asOpt.get

  private def adviserPostCodeLookUpPage(mode: Mode): Call = controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onPageLoad(mode)
  private def adviserEmailPage(mode: Mode): Call = controllers.register.adviser.routes.AdviserEmailController.onPageLoad(mode)
  private def adviserPhonePage(mode: Mode): Call = controllers.register.adviser.routes.AdviserPhoneController.onPageLoad(mode)
  private def adviserAddressListPage(mode: Mode): Call = controllers.register.adviser.routes.AdviserAddressListController.onPageLoad(mode)
  private def adviserAddressPage(mode: Mode): Call = controllers.register.adviser.routes.AdviserAddressController.onPageLoad(mode)
  private def checkYourAnswersPage(mode: Mode): Call = controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad(mode)
  private val variationDeclarationFitAndProperPage: Call = controllers.register.routes.VariationDeclarationFitAndProperController.onPageLoad()
  private val defaultPage: Call = controllers.routes.IndexController.onPageLoad()

  private val declarationFitAndProperPage: Call = controllers.register.routes.DeclarationFitAndProperController.onPageLoad()
  private val haveMoreChangesPage: Call = controllers.register.routes.AnyMoreChangesController.onPageLoad()

}
