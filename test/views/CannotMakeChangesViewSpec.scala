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

import models.UpdateMode
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.cannotMakeChanges

class CannotMakeChangesViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "cannotMakeChanges"
  private val administratorName = "Mark Wright"
  val view: cannotMakeChanges = app.injector.instanceOf[cannotMakeChanges]

  private def createView: () => HtmlFormat.Appendable = () =>
    view(Some(administratorName), UpdateMode)(fakeRequest, messages)

  "cannotMakeChanges view" must {

    appRunning()

    behave like normalPage(createView, messageKeyPrefix, "p2")

    behave like pageWithReturnLink(createView, controllers.routes.PsaDetailsController.onPageLoad().url)

    "have the correct P1" in {
      val doc = asDocument(createView())
      assertContainsText(doc, messages(s"$messageKeyPrefix.p1", administratorName))
    }
  }

}
