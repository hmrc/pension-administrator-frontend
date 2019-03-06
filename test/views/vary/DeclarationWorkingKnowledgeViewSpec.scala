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

package views.vary

import forms.vary.DeclarationWorkingKnowledgeFormProvider
import models.UpdateMode
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.vary.declarationWorkingKnowledge

class DeclarationWorkingKnowledgeViewSpec extends ViewBehaviours {

  private val psaName = "Mr Smith"
  private val personWithWorkingKnowledgeName = "Bill Bloggs"

  private val messageKeyPrefix = "declarationWorkingKnowledge.variations"

  private val form = new DeclarationWorkingKnowledgeFormProvider()()

  private def createView: () => HtmlFormat.Appendable = () => declarationWorkingKnowledge(
      frontendAppConfig,
    form,
    UpdateMode,
    psaName,
    personWithWorkingKnowledgeName)(fakeRequest, messages)

  "DeclarationWorkingKnowledge view (variations)" must {

    behave like normalPageWithoutPageTitleCheck(createView, messageKeyPrefix, "p1")

    "display the correct page heading" in {
      val doc = asDocument(createView())
      assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading", psaName, personWithWorkingKnowledgeName)
    }

    "display the second (dynamic) statement" in {
      createView must haveDynamicText("declarationWorkingKnowledge.variations.p2", psaName, personWithWorkingKnowledgeName)
    }

  }
}
