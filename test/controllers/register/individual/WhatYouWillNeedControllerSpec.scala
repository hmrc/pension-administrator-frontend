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

package controllers.register.individual

import play.api.data.Form
import play.api.libs.json.JsBoolean
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.FakeNavigator
import connectors.FakeDataCacheConnector
import controllers.actions._
import play.api.test.Helpers._
import models.NormalMode
import views.html.register.individual.whatYouWillNeed
import controllers.ControllerSpecBase

class WhatYouWillNeedControllerSpec extends ControllerSpecBase {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new WhatYouWillNeedController(frontendAppConfig, messagesApi, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl)

  def viewAsString() = whatYouWillNeed(frontendAppConfig)(fakeRequest, messages).toString

  "WhatYouWillNeed Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to next page on submit" in {

      val result = controller().onSubmit(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}
