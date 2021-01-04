/*
 * Copyright 2021 HM Revenue & Customs
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

package views.register.partnership

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.partnership.whatYouWillNeed

class WhatYouWillNeedViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "whatYouWillNeed.partnership"

  val view: whatYouWillNeed = app.injector.instanceOf[whatYouWillNeed]

  def createView: () => HtmlFormat.Appendable = () =>
    view()(fakeRequest, messages)

  "WhatYouWillNeed view" must {
    behave like normalPage(createView, messageKeyPrefix, "body.text1", "body.text2", "body.item1", "body.item2", "body.item3")

    behave like pageWithSubmitButton(createView)
  }

}
