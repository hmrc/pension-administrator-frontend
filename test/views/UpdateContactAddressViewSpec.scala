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

package views

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.updateContactAddress

class UpdateContactAddressViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "updateContactAddress"
  val view: updateContactAddress = app.injector.instanceOf[updateContactAddress]

  private def createView: () => HtmlFormat.Appendable = () =>
    view(Seq("a", "b"), "/url")(fakeRequest, messages)

  "updateContactAddress view" must {

    appRunning()

    behave like normalPage(createView, messageKeyPrefix, "p1", "p2", "p3", "p4")

    "display continue button" in {
      createView must haveLink(
        "/url",
        "submit"
      )
    }
  }

}
