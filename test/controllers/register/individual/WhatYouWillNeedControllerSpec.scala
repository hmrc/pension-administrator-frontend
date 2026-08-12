/*
 * Copyright 2026 HM Revenue & Customs
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

import controllers.ControllerSpecBase
import controllers.actions.*
import identifiers.register.individual.WhatYouWillNeedId
import models.NormalMode
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers.*
import utils.navigators.IndividualNavigatorV2
import utils.{FakeNavigator, UserAnswers}
import views.html.register.individual.whatYouWillNeed

class WhatYouWillNeedControllerSpec
  extends ControllerSpecBase {

  val view: whatYouWillNeed = app.injector.instanceOf[whatYouWillNeed]

  private val onwardRoute: Call = controllers.routes.IndexController.onPageLoad
  private val navigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val navigatorV2 = mock[IndividualNavigatorV2]

  private val ua: UserAnswers = UserAnswers(Json.obj())

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new WhatYouWillNeedController(
      navigator,
      navigatorV2,
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      controllerComponents,
      view
    )

  private def viewAsString() =
    view()(fakeRequest, messages).toString


  "WhatYouWillNeed Controller" must {

    "render the page correctly for GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page for POST" in {

      val data = new FakeDataRetrievalAction(Some(Json.obj()))

      when(navigatorV2.nextPage(WhatYouWillNeedId, NormalMode, ua))
        .thenReturn(onwardRoute)

      val result = controller(data).onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }
}