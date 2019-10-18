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

package controllers.register.company

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, FakeAllowAccessProvider, FakeAuthAction, FakeDataRetrievalAction}
import controllers.register.BusinessNameControllerBehaviour
import identifiers.register.BusinessTypeId
import models.register.BusinessType
import models.requests.DataRequest
import models.{Mode, PSAUser, UserType}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import utils.{FakeNavigator, UserAnswers}

class CompanyNameControllerSpec extends ControllerSpecBase with BusinessNameControllerBehaviour {

  implicit val dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), "cacheId",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None), UserAnswers())

  def validData = UserAnswers(Json.obj(
      BusinessTypeId.toString -> BusinessType.LimitedCompany.toString
    ))

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  def createController(userAnswers: UserAnswers): CompanyNameController =
    new CompanyNameController(
      frontendAppConfig,
      messagesApi,
      new FakeUserAnswersCacheConnector{},
      new FakeNavigator(onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      new FakeDataRetrievalAction(Some(userAnswers.json)),
      new DataRequiredActionImpl()
    ){
      override def href: Call = onwardRoute
    }

  "CompanyUTRController" must {

    behave like businessNameController(validData, createController)
  }

}