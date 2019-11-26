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

package views.register.company

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.company.whatYouWillNeed

class WhatYouWillNeedViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "whatYouWillNeed"

  val view: whatYouWillNeed = app.injector.instanceOf[whatYouWillNeed]


  def createView: () => HtmlFormat.Appendable = () => view()(fakeRequest, messages)

  "WhatYouWillNeed view" must {
    behave like normalPage(createView, messageKeyPrefix, "body.text", "company.body.item1", "company.body.item2", "company.body.item3")

    behave like pageWithSubmitButton(createView)
  }
  app.stop()
}
