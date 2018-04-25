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
import identifiers.register.advisor._
import models.NormalMode
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

@Singleton
class AdviserNavigator @Inject() extends Navigator {

  private def checkYourAnswers()(answers: UserAnswers): Call =
    controllers.register.advisor.routes.CheckYourAnswersController.onPageLoad()

  override def routeMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case AdvisorDetailsId =>
      _ => controllers.register.advisor.routes.AdvisorAddressPostCodeLookupController.onPageLoad(NormalMode)
    case AdvisorAddressPostCodeLookupId =>
      _ => controllers.register.advisor.routes.AdvisorAddressListController.onPageLoad(NormalMode)
    case AdvisorAddressListId =>
      _ => controllers.register.advisor.routes.AdvisorAddressController.onPageLoad(NormalMode)
    case AdvisorAddressId =>
      _ => controllers.register.advisor.routes.CheckYourAnswersController.onPageLoad()
    case CheckYourAnswersId =>
      _ => controllers.register.routes.DeclarationFitAndProperController.onPageLoad()
  }

  override protected def editRouteMap: PartialFunction[Identifier, UserAnswers => Call] = {
    case AdvisorDetailsId =>
      checkYourAnswers()
    case AdvisorAddressPostCodeLookupId =>
      _ => controllers.register.advisor.routes.AdvisorAddressListController.onPageLoad(NormalMode)
    case AdvisorAddressListId =>
      _ => controllers.register.advisor.routes.AdvisorAddressController.onPageLoad(NormalMode)
    case AdvisorAddressId =>
      checkYourAnswers()
  }
}
