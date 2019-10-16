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

import forms.register.VariationDeclarationFitAndProperFormProvider
import play.api.data.Form
import views.behaviours.QuestionViewBehaviours
import views.html.register.variationDeclarationFitAndProper

class VariationDeclarationFitAndProperViewSpec extends QuestionViewBehaviours[Boolean] {

  private val psaName = "test name"

  val form: Form[Boolean] = new VariationDeclarationFitAndProperFormProvider()()
  override val errorKey = "agree"

  val messageKeyPrefix = "declarationFitAndProper.variations"

  private def createView = () => variationDeclarationFitAndProper(frontendAppConfig, form, Some(psaName))(fakeRequest, messages)

  private def createViewUsingForm(form: Form[_]) = variationDeclarationFitAndProper(frontendAppConfig, form, Some(psaName))(fakeRequest, messages)

  "DeclarationFitAndProper (variations) view" must {
    appRunning()
    behave like normalPageWithPageTitleCheck(createView, messageKeyPrefix)

    behave like pageWithReturnLink(createView, controllers.routes.PsaDetailsController.onPageLoad().url)

    "display the correct page title" in {
      val doc = asDocument(createView())
      assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading", psaName)
    }

    "show an error summary when rendered with an error" in {
      val doc = asDocument(createViewUsingForm(form.withError(error)))
      assertRenderedById(doc, "error-summary-heading")
    }

    "show an error in the value field's label when rendered with an error" in {
      val doc = asDocument(createViewUsingForm(form.withError(error)))
      val errorSpan = doc.getElementsByClass("error-summary-list").first
      errorSpan.text mustBe messages(errorMessage)
    }

    "display the declaration" in {
      createView must haveDynamicText("declarationFitAndProper.variations.declaration", psaName)
    }

    "display the first statement" in {
      createView must haveDynamicText("declarationFitAndProper.variations.statement1")
    }

    "display the second statement" in {
      createView must haveDynamicText("declarationFitAndProper.variations.statement2")
    }

    "display the third statement" in {
      createView must haveDynamicText("declarationFitAndProper.variations.statement3")
    }

    "display the fourth statement" in {
      createView must haveDynamicText("declarationFitAndProper.variations.statement4")
    }

    "display the fifth statement" in {
      createView must haveDynamicText("declarationFitAndProper.variations.statement5")
    }

    "display the sixth statement" in {
      createView must haveDynamicText("declarationFitAndProper.variations.statement6")
    }

    behave like pageWithSubmitButton(createView)
  }
}
