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

import forms.register.DeclarationFormProvider
import play.api.data.Form
import views.behaviours.QuestionViewBehaviours
import views.html.vary.declarationVariation

class DeclarationVariationViewSpec extends QuestionViewBehaviours[Boolean] {

  val form: Form[Boolean] = new DeclarationFormProvider()()

  override val errorKey = "agree"

  private val messageKeyPrefix = "declaration.variations"

  private def returnLink = controllers.routes.PsaDetailsController.onPageLoad().url

  private def createView(isWorkingKnowldge : Boolean, isFitAndProper  : Boolean) = () => declarationVariation(
    frontendAppConfig, form, "Mark Wright", isWorkingKnowldge, isFitAndProper)(fakeRequest, messages)

  private def createViewUsingForm(form: Form[_]) = declarationVariation(frontendAppConfig, form, "Mark Wright",
    isWorkingKnowldge = true, isFitAndProper = true)(fakeRequest, messages)

  "Declaration view" must {

    appRunning()

    behave like normalPage(createView(isWorkingKnowldge = true, isFitAndProper = true), messageKeyPrefix)

    behave like pageWithReturnLink(createView(isWorkingKnowldge = true, isFitAndProper = true), returnLink)

    behave like pageWithSubmitButton(createView(isWorkingKnowldge = true, isFitAndProper = true))

    "show an error summary when rendered with an error" in {
      val doc = asDocument(createViewUsingForm(form.withError(error)))
      assertRenderedById(doc, "error-summary-heading")
    }

    "show an error in the value field's label when rendered with an error" in {
      val doc = asDocument(createViewUsingForm(form.withError(error)))
      val errorSpan = doc.getElementsByClass("error-notification").first
      errorSpan.text mustBe messages(errorMessage)
    }

    "display the declaration" in {
      createView(isWorkingKnowldge = true, isFitAndProper = true) must haveDynamicText("declaration.variations.statement0")
    }

    "display the first statement" in {
      createView(isWorkingKnowldge = true, isFitAndProper = true) must haveDynamicText("declaration.variations.statement1")
    }

    "display the second statement" in {
      createView(isWorkingKnowldge = true, isFitAndProper = true) must haveDynamicText("declaration.variations.statement2")
    }

    "not display the second statement" in {
      createView(isWorkingKnowldge = true, isFitAndProper = false) must notHaveDynamicText("declaration.variations.statement2")
    }

    "display the third statement" in {
      createView(isWorkingKnowldge = true, isFitAndProper = true) must haveDynamicText("declaration.variations.statement3")
    }

    "display the fourth statement" in {
      createView(isWorkingKnowldge = true, isFitAndProper = true) must haveDynamicText("declaration.variations.statement4")
      createView(isWorkingKnowldge = true, isFitAndProper = true) must notHaveDynamicText("declaration.variations.statement5")
    }

    "display the fifth statement" in {
      createView(isWorkingKnowldge = false, isFitAndProper = true) must notHaveDynamicText("declaration.variations.statement4")
      createView(isWorkingKnowldge = false, isFitAndProper = true) must haveDynamicText("declaration.variations.statement5")
    }

    "have an Agree checkbox" in {
      createView(isWorkingKnowldge = true, isFitAndProper = true) must haveCheckBox("agree", "agreed")
    }

    "have a label for the I Agree checkbox" in {
      createView(isWorkingKnowldge = true, isFitAndProper = true) must haveLabel("agree", messages("declaration.variations.agree"))
    }

  }

}
