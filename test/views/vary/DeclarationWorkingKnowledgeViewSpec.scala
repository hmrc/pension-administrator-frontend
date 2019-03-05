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

import forms.register.DeclarationWorkingKnowledgeFormProvider
import models.UpdateMode
import models.register.DeclarationWorkingKnowledge
import play.api.data.Form
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

  private def createViewUsingForm = (form: Form[_]) => declarationWorkingKnowledge(
    frontendAppConfig,
    form,
    UpdateMode,
    psaName,
    personWithWorkingKnowledgeName)(fakeRequest, messages)

  "DeclarationWorkingKnowledge view (variations)" must {

    behave like normalPageWithoutPageTitleCheck(createView, messageKeyPrefix)

    "display the correct page heading" in {
      val doc = asDocument(createView())
      assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading", psaName, personWithWorkingKnowledgeName)
    }

    "contain radio buttons for the value" in {
      val doc = asDocument(createViewUsingForm(form))
      for (option <- DeclarationWorkingKnowledge.options) {
        assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, false)
      }
    }

    for (option <- DeclarationWorkingKnowledge.options) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, true)

          for (unselectedOption <- DeclarationWorkingKnowledge.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}
