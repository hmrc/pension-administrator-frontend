/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.register.adviser.routes._
import controllers.register.routes.{AnyMoreChangesController, DeclarationFitAndProperController, VariationDeclarationFitAndProperController}
import identifiers.Identifier
import identifiers.register.PAInDeclarationJourneyId
import identifiers.register.adviser._
import javax.inject.{Inject, Singleton}
import models.Mode.journeyMode
import models._
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

@Singleton
class AdviserNavigator @Inject() extends Navigator {

  override protected def routeMap(ua: UserAnswers): PartialFunction[Identifier, Call] = normalAndUpdateRoutes(ua, NormalMode)

  override protected def editRouteMap(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case AdviserNameId => checkYourAnswers(journeyMode(mode))
    case AdviserEmailId => checkYourAnswers(journeyMode(mode))
    case AdviserPhoneId => checkYourAnswers(journeyMode(mode))
    case AdviserAddressPostCodeLookupId => AdviserAddressListController.onPageLoad(mode)
    case AdviserAddressListId => AdviserAddressController.onPageLoad(journeyMode(mode))
    case AdviserAddressId => checkYourAnswers(journeyMode(mode))
  }

  override protected def updateRouteMap(ua: UserAnswers): PartialFunction[Identifier, Call] = normalAndUpdateRoutes(ua, UpdateMode)

  private def checkYourAnswers(mode: Mode): Call = CheckYourAnswersController.onPageLoad(mode)

  private def normalAndUpdateRoutes(ua: UserAnswers, mode: Mode): PartialFunction[Identifier, Call] = {
    case AdviserNameId => adviserCompletionCheckNavigator(ua, AdviserAddressPostCodeLookupController.onPageLoad(mode), mode)
    case AdviserAddressPostCodeLookupId => AdviserAddressListController.onPageLoad(mode)
    case AdviserAddressListId => AdviserAddressController.onPageLoad(mode)
    case AdviserAddressId =>
      adviserCompletionCheckNavigator(ua, AdviserEmailController.onPageLoad(mode), mode)
    case AdviserEmailId => adviserCompletionCheckNavigator(ua, AdviserPhoneController.onPageLoad(mode), mode)
    case AdviserPhoneId => adviserCompletionCheckNavigator(ua,
      (if(mode == NormalMode) checkYourAnswers(mode) else controllers.routes.PsaDetailsController.onPageLoad()), mode)
    case CheckYourAnswersId => checkYourAnswersRoutes(mode, ua)
  }

  private def adviserCompletionCheckNavigator(ua: UserAnswers, call: Call, mode: Mode): Call = {
    (mode, ua.get(AdviserEmailId), ua.get(AdviserPhoneId), ua.get(AdviserAddressId), ua.get(IsNewAdviserId)) match {
      case (NormalMode, _, _, _, _) => call
      case (UpdateMode, Some(_), Some(_), Some(_), Some(true)) => checkYourAnswers(mode)
      case (UpdateMode, Some(_), Some(_), Some(_), _) => AnyMoreChangesController.onPageLoad()
      case (_, _, _, _, Some(true)) => call
      case _ => controllers.routes.PsaDetailsController.onPageLoad()
    }
  }

  private def checkYourAnswersRoutes(mode: Mode, userAnswers: UserAnswers): Call = {
    if (mode == UpdateMode) {
      userAnswers.get(PAInDeclarationJourneyId) match {
        case Some(true) => VariationDeclarationFitAndProperController.onPageLoad()
        case _ => AnyMoreChangesController.onPageLoad()
      }
    } else {
      DeclarationFitAndProperController.onPageLoad()
    }
  }
}
