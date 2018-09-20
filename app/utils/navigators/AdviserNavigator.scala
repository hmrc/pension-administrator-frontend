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

import connectors.UserAnswersCacheConnector
import controllers.register.adviser._
import identifiers.register.adviser._
import javax.inject.{Inject, Singleton}
import models.{CheckMode, NormalMode}
import play.api.mvc.Call
import utils.Navigator

@Singleton
class AdviserNavigator @Inject()(val dataCacheConnector: UserAnswersCacheConnector) extends Navigator {

  private def checkYourAnswers(): Call =
    controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad()

  override def routeMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case AdviserDetailsId => NavigateTo.save(routes.AdviserAddressPostCodeLookupController.onPageLoad(NormalMode))
    case AdviserAddressPostCodeLookupId => NavigateTo.dontSave(routes.AdviserAddressListController.onPageLoad(NormalMode))
    case AdviserAddressListId => NavigateTo.save(routes.AdviserAddressController.onPageLoad(NormalMode))
    case AdviserAddressId => NavigateTo.save(routes.CheckYourAnswersController.onPageLoad())
    case CheckYourAnswersId => NavigateTo.save(controllers.register.routes.DeclarationFitAndProperController.onPageLoad())
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }

  override protected def editRouteMap(from: NavigateFrom): Option[NavigateTo] = from.id match {
    case AdviserDetailsId => NavigateTo.dontSave(checkYourAnswers())
    case AdviserAddressPostCodeLookupId => NavigateTo.dontSave(routes.AdviserAddressListController.onPageLoad(CheckMode))
    case AdviserAddressListId => NavigateTo.save(routes.AdviserAddressController.onPageLoad(CheckMode))
    case AdviserAddressId => NavigateTo.dontSave(checkYourAnswers())
    case _ => NavigateTo.dontSave(controllers.routes.SessionExpiredController.onPageLoad())
  }
}
