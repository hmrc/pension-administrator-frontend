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

package views.register.company

import models.Index
import views.behaviours.ViewBehaviours
import views.html.register.company.confirmDeleteDirector

class ConfirmDeleteDirectorViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "confirmDeleteDirector"

  val firstIndex = Index(0)

  def createView = () => confirmDeleteDirector(frontendAppConfig, firstIndex)(fakeRequest, messages)

  "ConfirmDeleteDirector view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, messages("site.secondaryHeader"))

    "have a confirm button" in {
      val doc = asDocument(createView())
      assertRenderedById(doc, "submit")
    }

    "have a cancel button" in {
      val doc = asDocument(createView())
      assertRenderedById(doc, "cancel")
    }
  }
}
