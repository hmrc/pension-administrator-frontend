/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.register

import controllers.ControllerSpecBase
import controllers.actions.FakeAuthAction
import models.NormalMode
import models.admin.ukResidencyToggle
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.mvc.Call
import play.api.test.Helpers.*
import utils.FeatureFlagMockHelper
import views.html.register.whatYouWillNeed

class WhatYouWillNeedControllerSpec extends ControllerSpecBase with BeforeAndAfterEach with FeatureFlagMockHelper {

  val view: whatYouWillNeed = app.injector.instanceOf[whatYouWillNeed]

  private def onwardRoute: Call = controllers.register.routes.BusinessTypeAreYouInUKController.onPageLoad(NormalMode)

  override def beforeEach(): Unit = {
    featureFlagMock(ukResidencyToggle)
  }

  private def controller() =
    new WhatYouWillNeedController(
      FakeAuthAction,
      mockFeatureFlagService,
      controllerComponents,
      view
    )

  private def viewAsString: String = view(onwardRoute, false)(fakeRequest, messages).toString

  //  private def viewUkResidency(): String = view(onwardRoute, true)(fakeRequest, messages).toString

  private def doc: () => Document = () => Jsoup.parse(viewAsString)

  //  private def docUkResidency: Document = Jsoup.parse(viewUkResidency())

  "WhatYouWillNeed Controller" must {

    "return OK and the correct view for a GET" when {
      "ukResidencyToggle is disabled" in {
        val result = controller().onPageLoad()(fakeRequest)
        status(result) mustBe OK
//        contentAsString(result) mustBe viewAsString()
        
        doc().getElementById("li-1").text() shouldBe "the business name, address, previous address, VAT and PAYE references of the business you’re registering"
        doc().getElementById("li-2").text() shouldBe "the phone number and email address of a contactable person within the business"
        doc().getElementById("li-3").text() shouldBe "the details of all the directors or partners associated with the business, " +
          "including their Unique Taxpayer Reference (UTR) and National Insurance number"
      }
      "ukResidencyToggle is enabled" in {
        featureFlagMock(ukResidencyToggle, true)
        val result = controller().onPageLoad()(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString
      }
    }
  }
}
