/*
 * Copyright 2020 HM Revenue & Customs
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

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.duplicateRegistration

class DuplicateRegistrationViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "duplicateRegistration"

  val view: duplicateRegistration = app.injector.instanceOf[duplicateRegistration]

  private def viewHtml: HtmlFormat.Appendable = view()(fakeRequest, messages)

  "DuplicateRegistration view" must {
    behave like normalPage(() => viewHtml, messageKeyPrefix, "p1", "p2")

    behave like pageWithExitToGovUKLink(() => viewHtml, frontendAppConfig.govUkUrl, "go-to-gov-uk")
  }

}
