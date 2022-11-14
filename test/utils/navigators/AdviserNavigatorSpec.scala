/*
 * Copyright 2022 HM Revenue & Customs
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
import identifiers.Identifier
import identifiers.register.adviser._
import identifiers.register.{AreYouInUKId, PAInDeclarationJourneyId}
import models._
import org.scalatest.OptionValues
import org.scalatest.prop.TableFor3
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.{Navigator, NavigatorBehaviour, UserAnswers}

class AdviserNavigatorSpec extends SpecBase with NavigatorBehaviour {

  import AdviserNavigatorSpec._

  val navigator: Navigator = injector.instanceOf[AdviserNavigator]

  "AdviserNavigator in NormalMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (AdviserNameId, emptyAnswers, adviserPostCodeLookUpPage(NormalMode)),
      (AdviserAddressPostCodeLookupId, emptyAnswers, adviserAddressListPage(NormalMode)),
      (AdviserAddressId, emptyAnswers, adviserEmailPage(NormalMode)),
      (AdviserEmailId, emptyAnswers, adviserPhonePage(NormalMode)),
      (AdviserPhoneId, emptyAnswers, checkYourAnswersPage(NormalMode)),
      (CheckYourAnswersId, emptyAnswers, declarationFitAndProperPage)
    )

    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, NormalMode)
  }

  "AdviserNavigator in CheckMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (AdviserNameId, emptyAnswers, checkYourAnswersPage(NormalMode)),
      (AdviserAddressPostCodeLookupId, emptyAnswers, adviserAddressListPage(CheckMode)),
      (AdviserAddressId, emptyAnswers, checkYourAnswersPage(NormalMode)),
      (AdviserEmailId, emptyAnswers, checkYourAnswersPage(NormalMode)),
      (AdviserPhoneId, emptyAnswers, checkYourAnswersPage(NormalMode))
    )

    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, CheckMode)
  }

  "AdviserNavigator in UpdateMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page (NormalMode)"),
      (AdviserNameId, emptyAnswers, psaDetailsPage),
      (AdviserNameId, adviserUpdated, adviserPostCodeLookUpPage(UpdateMode)),
      (AdviserAddressPostCodeLookupId, emptyAnswers, adviserAddressListPage(UpdateMode)),
      (AdviserAddressId, emptyAnswers, psaDetailsPage),
      (AdviserAddressId, adviserUpdated, adviserEmailPage(UpdateMode)),
      (AdviserEmailId, emptyAnswers, psaDetailsPage),
      (AdviserEmailId, adviserUpdated, adviserPhonePage(UpdateMode)),
      (AdviserPhoneId, emptyAnswers, psaDetailsPage),
      (AdviserPhoneId, adviserUpdated, psaDetailsPage),

      (CheckYourAnswersId, emptyAnswers, haveMoreChangesPage),
      (CheckYourAnswersId, declarationPensionAdvisorTrue, variationDeclarationFitAndProperPage),
      (invalidIdForNavigator, emptyAnswers, defaultPage)
    )

    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, UpdateMode)
  }

  "AdviserNavigator in CheckUpdateMode" must {
    def routes(): TableFor3[Identifier, UserAnswers, Call] = Table(
      ("Id", "User Answers", "Next Page"),
      (AdviserNameId, emptyAnswers, checkYourAnswersPage(UpdateMode)),
      (AdviserNameId, adviserUpdated, checkYourAnswersPage(UpdateMode)),
      (AdviserAddressPostCodeLookupId, emptyAnswers, adviserAddressListPage(CheckUpdateMode)),
      (AdviserAddressId, emptyAnswers, checkYourAnswersPage(UpdateMode)),
      (AdviserEmailId, adviserUpdated, checkYourAnswersPage(UpdateMode)),
      (AdviserPhoneId, emptyAnswers, checkYourAnswersPage(UpdateMode)),
      (AdviserPhoneId, adviserUpdated, checkYourAnswersPage(UpdateMode))
    )

    behave like navigatorWithRoutesWithMode(navigator, routes(), dataDescriber, CheckUpdateMode)
  }
}

object AdviserNavigatorSpec extends OptionValues {
  private val invalidIdForNavigator = AreYouInUKId
  private val adviserUpdated = UserAnswers(Json.obj()).set(IsNewAdviserId)(value = true).asOpt.get

  private val declarationPensionAdvisorTrue = UserAnswers(Json.obj()).set(PAInDeclarationJourneyId)(true).asOpt.get

  private def adviserPostCodeLookUpPage(mode: Mode): Call = controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onPageLoad(mode)
  private def adviserEmailPage(mode: Mode): Call = controllers.register.adviser.routes.AdviserEmailController.onPageLoad(mode)
  private def adviserPhonePage(mode: Mode): Call = controllers.register.adviser.routes.AdviserPhoneController.onPageLoad(mode)
  private def adviserAddressListPage(mode: Mode): Call = controllers.register.adviser.routes.AdviserAddressListController.onPageLoad(mode)
  private def checkYourAnswersPage(mode: Mode): Call = controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad(mode)
  private val variationDeclarationFitAndProperPage: Call = controllers.register.routes.VariationDeclarationFitAndProperController.onPageLoad()
  private val defaultPage: Call = controllers.routes.IndexController.onPageLoad

  private val declarationFitAndProperPage: Call = controllers.register.routes.DeclarationFitAndProperController.onPageLoad()
  private val haveMoreChangesPage: Call = controllers.register.routes.AnyMoreChangesController.onPageLoad
  private val psaDetailsPage: Call = controllers.routes.PsaDetailsController.onPageLoad()

}
