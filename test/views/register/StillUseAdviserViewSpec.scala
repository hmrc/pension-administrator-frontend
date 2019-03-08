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

import forms.register.StillUseAdviserFormProvider
import models.UpdateMode
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.stillUseAdviser

class StillUseAdviserViewSpec extends ViewBehaviours {

  private val psaName = "Mr Smith"
  private val personWithWorkingKnowledgeName = "Bill Bloggs"

  private val messageKeyPrefix = "stillUseAdviser"

  private val form = new StillUseAdviserFormProvider()()

  private def createView: () => HtmlFormat.Appendable = () => stillUseAdviser(
      frontendAppConfig,
    form,
    UpdateMode,
    Some(psaName),
    personWithWorkingKnowledgeName)(fakeRequest, messages)

  "DeclarationWorkingKnowledge view (variations)" must {

    behave like normalPageWithoutPageTitleCheck(createView, messageKeyPrefix, "p1")

    "display the correct page heading" in {
      val doc = asDocument(createView())
      assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading", psaName, personWithWorkingKnowledgeName)
    }

    "display the second (dynamic) statement" in {
      createView must haveDynamicText("stillUseAdviser.p2", psaName, personWithWorkingKnowledgeName)
    }

  }
}
