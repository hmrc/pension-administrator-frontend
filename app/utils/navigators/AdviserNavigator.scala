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
import identifiers.register.adviser._
import javax.inject.{Inject, Singleton}
import models.{CheckMode, Mode, NormalMode, UpdateMode}
import play.api.mvc.Call
import utils.Navigator

@Singleton
class AdviserNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends Navigator {

  private def checkYourAnswers(mode:Mode): Call =
    controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad(mode)

  override def routeMap(from: NavigateFrom): Option[NavigateTo] = commonNavigator(from, NormalMode)

  override protected def editRouteMap(from: NavigateFrom, mode: Mode): Option[NavigateTo] = from.id match {
    case AdviserNameId => NavigateTo.dontSave(checkYourAnswers(Mode.journeyMode(mode)))
    case AdviserDetailsId => NavigateTo.dontSave(checkYourAnswers(Mode.journeyMode(mode)))
    case AdviserAddressPostCodeLookupId => NavigateTo.dontSave(routes.AdviserAddressListController.onPageLoad(CheckMode))
    case AdviserAddressListId => NavigateTo.dontSave(routes.AdviserAddressController.onPageLoad(CheckMode))
    case AdviserAddressId => NavigateTo.dontSave(checkYourAnswers(Mode.journeyMode(mode)))
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  override protected def updateRouteMap(from: NavigateFrom): Option[NavigateTo] = commonNavigator(from, UpdateMode)

  private def commonNavigator(from: NavigateFrom, mode: Mode): Option[NavigateTo] = from.id match {
    case AdviserNameId => NavigateTo.dontSave(routes.AdviserDetailsController.onPageLoad(mode))
    case AdviserDetailsId => isAdviserChange(from, NavigateTo.dontSave(routes.AdviserAddressPostCodeLookupController.onPageLoad(mode)), mode)
    case AdviserAddressPostCodeLookupId => NavigateTo.dontSave(routes.AdviserAddressListController.onPageLoad(mode))
    case AdviserAddressListId => NavigateTo.dontSave(routes.AdviserAddressController.onPageLoad(mode))
    case AdviserAddressId => isAdviserChange(from, NavigateTo.dontSave(routes.CheckYourAnswersController.onPageLoad(mode)), mode)
    case CheckYourAnswersId => if(mode == UpdateMode) {
      NavigateTo.dontSave(controllers.register.routes.AnyMoreChangesController.onPageLoad())
    } else {
      NavigateTo.dontSave(controllers.register.routes.DeclarationFitAndProperController.onPageLoad())
    }
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  private def isAdviserChange(from: NavigateFrom, call: Option[NavigateTo], mode: Mode): Option[NavigateTo] = {
    if(mode==NormalMode){
      call
    } else {
      from.userAnswers.get(IsNewAdviserId) match {
        case Some(true) => call
        case _ =>  NavigateTo.dontSave(controllers.register.routes.AnyMoreChangesController.onPageLoad())
      }
    }
  }
}
