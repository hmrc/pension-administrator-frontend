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

import forms.register.DeclarationFormProvider
import play.api.data.Form
import views.behaviours.QuestionViewBehaviours
import views.html.register.declaration

class DeclarationViewSpec extends QuestionViewBehaviours[Boolean] {

  val form: Form[Boolean] = new DeclarationFormProvider()()
  override val errorKey = "agree"

  private val messageKeyPrefix = "declaration"
  private val cancelCall = controllers.routes.IndexController.onPageLoad()

  private def createView = () => declaration(frontendAppConfig, form, cancelCall)(fakeRequest, messages)

  private def createViewUsingForm(form: Form[_]) = declaration(frontendAppConfig, form, cancelCall)(fakeRequest, messages)

  "Declaration view" must {
    behave like normalPage(createView, messageKeyPrefix)

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
      createView must haveDynamicText("declaration.declaration")
    }

    "display the first statement" in {
      createView must haveDynamicText("declaration.statement1")
    }

    "display the second statement" in {
      createView must haveDynamicText("declaration.statement2")
    }

    "display the third statement" in {
      createView must haveDynamicText("declaration.statement3")
    }

    "display the fourth statement" in {
      createView must haveDynamicText("declaration.statement4")
    }

    "have an I Agree checkbox" in {
      createView must haveCheckBox("agree", "agreed")
    }

    "have a label for the I Agree checkbox" in {
      createView must haveLabel("agree", messages("declaration.agree"))
    }

    behave like pageWithSubmitButton(createView)

    "have a cancel link" in {
      createView must haveLink(cancelCall.url, "cancel")
    }
  }

}
