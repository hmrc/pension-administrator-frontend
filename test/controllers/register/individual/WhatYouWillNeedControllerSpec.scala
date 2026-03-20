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
import controllers.actions._
import identifiers.register.individual.WhatYouWillNeedId
import models.NormalMode
import models.admin.ukResidencyToggle
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.{FakeNavigator, FeatureFlagMockHelper, UserAnswers}
import utils.navigators.IndividualNavigatorV2
import views.html.register.individual.whatYouWillNeed

class WhatYouWillNeedControllerSpec
  extends ControllerSpecBase
    with BeforeAndAfterEach
    with FeatureFlagMockHelper {

  val view: whatYouWillNeed = app.injector.instanceOf[whatYouWillNeed]

  private val onwardRoute: Call = controllers.routes.IndexController.onPageLoad
  private val navigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val navigatorV2 = mock[IndividualNavigatorV2]

  private val ua: UserAnswers = UserAnswers(Json.obj())

  override def beforeEach(): Unit = featureFlagMock(ukResidencyToggle)

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new WhatYouWillNeedController(
      navigator,
      navigatorV2,
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      mockFeatureFlagService,
      controllerComponents,
      view
    )

  private def viewAsString(toggle: Boolean) =
    view(toggle)(fakeRequest, messages).toString

  private def doc(toggle: Boolean): Document =
    Jsoup.parse(viewAsString(toggle))

  "WhatYouWillNeed Controller" must {

    "render the page correctly for GET" when {

      "ukResidencyToggle is disabled" in {
        val result = controller().onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(false)

        doc(false).getElementById("li-1").text mustBe
          "your name, address, previous addresses and National Insurance number"
        doc(false).getElementById("li-2").text mustBe
          "your contact phone number and email address"
      }

      "ukResidencyToggle is enabled" in {
        featureFlagMock(ukResidencyToggle, true)

        val result = controller().onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(true)

        doc(true).getElementById("li-1").text mustBe
          "name, address and any previous addresses in the last year"
        doc(true).getElementById("li-2").text mustBe
          "National Insurance number"
        doc(true).getElementById("li-3").text mustBe
          "phone number and email address"
      }
    }

    "redirect to the next page for POST" when {

      "ukResidencyToggle is disabled" in {
        val data = new FakeDataRetrievalAction(Some(Json.obj()))

        val result = controller(data).onSubmit(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "ukResidencyToggle is enabled" in {
        featureFlagMock(ukResidencyToggle, true)

        val data = new FakeDataRetrievalAction(Some(Json.obj()))

        when(navigatorV2.nextPage(WhatYouWillNeedId, NormalMode, ua))
          .thenReturn(onwardRoute)

        val result = controller(data).onSubmit(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }
  }
}