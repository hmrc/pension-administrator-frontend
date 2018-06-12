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

import javax.inject.{Inject, Singleton}

import identifiers.Identifier
import identifiers.register.adviser._
import models.{CheckMode, NormalMode}
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}
import controllers.register.adviser._

@Singleton
class AdviserNavigator @Inject() extends Navigator {

  private def checkYourAnswers()(answers: UserAnswers): Call =
    controllers.register.adviser.routes.CheckYourAnswersController.onPageLoad()

  override def routeMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case AdviserDetailsId =>
      _ => routes.AdviserAddressPostCodeLookupController.onPageLoad(NormalMode)
    case AdviserAddressPostCodeLookupId =>
      _ => routes.AdviserAddressListController.onPageLoad(NormalMode)
    case AdviserAddressListId =>
      _ => routes.AdviserAddressController.onPageLoad(NormalMode)
    case AdviserAddressId =>
      _ => routes.CheckYourAnswersController.onPageLoad()
    case CheckYourAnswersId =>
      _ => controllers.register.routes.DeclarationFitAndProperController.onPageLoad()
  }

  override protected def editRouteMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case AdviserDetailsId =>
      checkYourAnswers()
    case AdviserAddressPostCodeLookupId =>
      _ => routes.AdviserAddressListController.onPageLoad(CheckMode)
    case AdviserAddressListId =>
      _ => routes.AdviserAddressController.onPageLoad(CheckMode)
    case AdviserAddressId =>
      checkYourAnswers()
  }
}
