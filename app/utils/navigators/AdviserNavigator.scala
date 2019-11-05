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

import connectors.UserAnswersCacheConnector
import controllers.register.adviser._
import identifiers.Identifier
import identifiers.register.PAInDeclarationJourneyId
import identifiers.register.adviser._
import javax.inject.{Inject, Singleton}
import models.Mode.journeyMode
import models._
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

@Singleton
class AdviserNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends Navigator {

  override protected def routeMap(ua: UserAnswers): PartialFunction[Identifier, Call] = commonNavigator(ua, NormalMode)

  override protected def editRouteMap(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case AdviserNameId => checkYourAnswers(journeyMode(mode))
    case AdviserDetailsId => checkYourAnswers(journeyMode(mode))
    case AdviserAddressPostCodeLookupId => routes.AdviserAddressListController.onPageLoad(journeyMode(mode))
    case AdviserAddressListId => routes.AdviserAddressController.onPageLoad(journeyMode(mode))
    case AdviserAddressId => checkYourAnswers(journeyMode(mode))
  }

  override protected def updateRouteMap(ua: UserAnswers): PartialFunction[Identifier, Call] = commonNavigator(ua, UpdateMode)

  private def checkYourAnswers(mode: Mode): Call =
    controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad(mode)

  private def commonNavigator(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case AdviserNameId => routes.AdviserDetailsController.onPageLoad(mode)
    case AdviserDetailsId =>
      adviserCompletionCheckNavigator(ua, routes.AdviserAddressPostCodeLookupController.onPageLoad(mode), mode)
    case AdviserAddressPostCodeLookupId => routes.AdviserAddressListController.onPageLoad(mode)
    case AdviserAddressListId => routes.AdviserAddressController.onPageLoad(mode)
    case AdviserAddressId =>
      adviserCompletionCheckNavigator(ua, routes.CheckYourAnswersController.onPageLoad(mode), mode)
    case CheckYourAnswersId => checkYourAnswersRoutes(mode, ua)
  }

  private def adviserCompletionCheckNavigator(ua: UserAnswers, call: Call, mode: Mode): Call = {
    (mode, ua.get(AdviserAddressId), ua.get(IsNewAdviserId)) match {
      case (NormalMode, _, _) => call
      case (UpdateMode, Some(_), _) =>
        controllers.register.routes.AnyMoreChangesController.onPageLoad()
      case (_, _, Some(true)) => call
      case _ => controllers.register.routes.AnyMoreChangesController.onPageLoad()
    }
  }

  private def checkYourAnswersRoutes(mode: Mode, userAnswers: UserAnswers): Call = {
    if (mode == UpdateMode) {
      userAnswers.get(PAInDeclarationJourneyId) match {
        case Some(true) => controllers.register.routes.VariationDeclarationFitAndProperController.onPageLoad()
        case _ => controllers.register.routes.AnyMoreChangesController.onPageLoad()
      }
    } else {
      controllers.register.routes.DeclarationFitAndProperController.onPageLoad()
    }
  }
}
