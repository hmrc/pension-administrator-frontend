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
import play.twirl.api.Html
import views.behaviours.QuestionViewBehaviours
import views.html.register.declaration

class DeclarationViewSpec extends QuestionViewBehaviours[Boolean] {

  val form: Form[Boolean] = new DeclarationFormProvider()()
  override val errorKey = "agree"

  private val messageKeyPrefix = "declaration"

  val view: declaration = app.injector.instanceOf[declaration]

  private def createView(workingKnowledge: Boolean = true): () => Html = () =>
    view(workingKnowledge)(fakeRequest, messages)

  "Declaration view" must {
    behave like normalPage(createView(), messageKeyPrefix)


    "display the declaration" in {
      createView() must haveDynamicText("declaration.declaration")
    }

    "display the first statement" in {
     createView() must haveDynamicText("declaration.statement1")
    }

    "display the second statement" in {
     createView() must haveDynamicText("declaration.statement2")
    }

    "display the third statement" in {
     createView() must haveDynamicText("declaration.statement3")
    }

    "display the fourth statement" in {
     createView() must haveDynamicText("declaration.statement4")
    }

    "display the 5a statement when user has working knowledge" in {
      createView() must haveDynamicText("declaration.statement5a")
    }

    "display the 5b statement if no working knowledge" in {
      createView(false) must haveDynamicText("declaration.statement5b")
    }

    behave like pageWithSubmitButton(createView())
  }

}
