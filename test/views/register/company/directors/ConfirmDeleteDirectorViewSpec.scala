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

package views.register.company.directors

import models.{Index, NormalMode}
import play.twirl.api.Html
import views.behaviours.ViewBehaviours
import views.html.register.company.directors.confirmDeleteDirector

class ConfirmDeleteDirectorViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "confirmDeleteDirector"

  val firstIndex = Index(0)

  val view: confirmDeleteDirector = app.injector.instanceOf[confirmDeleteDirector]

  def createView: () => Html = () => view(firstIndex, "directorName", NormalMode)(fakeRequest, messages)

  "ConfirmDeleteDirector view" must {

    "have the correct banner title" in {
      val doc = asDocument(createView())
      val nav = doc.getElementById("proposition-menu")
      val span = nav.children.first
      span.text mustBe messages("site.service_name")
    }

    "display the correct browser title" in {
      val doc = asDocument(createView())
      assertEqualsMessage(doc, "title", messages(s"$messageKeyPrefix.title") + " - " + messages("pension.scheme.administrator.title"))
    }

    "display the correct page title" in {
      val doc = asDocument(createView())
      assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading", "directorName")
    }

    "have a confirm button" in {
      val doc = asDocument(createView())
      assertRenderedById(doc, "submit")
    }

    "have a cancel link" in {
      createView must haveLink(controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(NormalMode).url, "cancel")
    }
  }

}
