/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.mvc.Call
import viewmodels.{AlreadyDeletedViewModel, Message}
import views.behaviours.ViewBehaviours
import views.html.alreadyDeleted

class AlreadyDeletedViewSpec extends ViewBehaviours {
  val messageKeyPrefix = "alreadyDeleted"
  val deletedEntity = "Test entity"
  def viewmodel = AlreadyDeletedViewModel(Message("alreadyDeleted.director.title"), deletedEntity, Call("GET", "/"))

  val expectedGuidanceKeys = Seq(
    Message("alreadyDeleted.lede", deletedEntity),
    Message("alreadyDeleted.text")
  )

  def createView = () => alreadyDeleted(frontendAppConfig, viewmodel)(fakeRequest, messages)

  "Already Deleted view" must {

    "display the correct browser title" in {
      val doc = asDocument(createView())
      assertEqualsMessage(doc, "title", Message(s"$messageKeyPrefix.director.title", deletedEntity))
    }

    "display the correct page title" in {
      val doc = asDocument(createView())
      assertPageTitleEqualsMessage(doc, Message(s"$messageKeyPrefix.heading", deletedEntity))
    }

    "display the correct guidance" in {
      val doc = asDocument(createView())
      for (key <- expectedGuidanceKeys) assertContainsText(doc, key)
    }

    "display button to take the user back to the list" in {
      createView must haveLink(
        "/",
        "return-to-list"
      )
    }

  }
}