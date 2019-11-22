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

package views

import play.twirl.api.HtmlFormat
import viewmodels.Message
import views.behaviours.ViewBehaviours
import views.html.alreadyDeletedAdviser

class AlreadyDeletedAdviserViewSpec extends ViewBehaviours {
  val messageKeyPrefix = "adviser.alreadyDeleted"
  val deletedEntity = "Test entity"

  def onwardRoute: String = controllers.routes.PsaDetailsController.onPageLoad().url

  val view: alreadyDeletedAdviser = app.injector.instanceOf[alreadyDeletedAdviser]

  def createView: () => HtmlFormat.Appendable = () => view(onwardRoute)(fakeRequest, messages)

  "Already Deleted view" must {

    "display the correct browser title" in {
      val doc = asDocument(createView())
      assertEqualsMessage(doc, "title",
        messages(s"$messageKeyPrefix.title") + " - " + messages("pension.scheme.administrator.title"))
    }

    "display the correct page title" in {
      val doc = asDocument(createView())
      assertPageTitleEqualsMessage(doc, Message(s"$messageKeyPrefix.heading"))
    }

    "display button to take the user back to the list" in {
      createView must haveLink(
        onwardRoute,
        "return-to-list"
      )
    }

  }
}