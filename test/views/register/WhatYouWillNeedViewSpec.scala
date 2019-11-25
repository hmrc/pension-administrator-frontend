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

package views.register

import play.api.mvc.Call
import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.register.whatYouWillNeed

class WhatYouWillNeedViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "whatYouWillNeed.main"

  private def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  private def createView: () => Html = () => whatYouWillNeed(onwardRoute)(fakeRequest, messages)

  "WhatYouWillNeed view" must {
    behave like normalPage(createView, messageKeyPrefix, "body.text", "body.item1", "body.item2", "body.item3", "body.hint")

    behave like pageWithSubmitButton(createView)

    "have anchor element with correct target and content" in {
      asDocument(createView()) must haveLinkWithUrlAndContent(
        linkId = "submit",
        url = onwardRoute.url,
        expectedContent = messages("site.continue"))
    }

    "have correct CSS class against hint text" in {
      val actual = asDocument(createView())
      actual.select("#hintText")
        .hasClass("panel panel-border-wide") mustBe true
    }
  }

}
