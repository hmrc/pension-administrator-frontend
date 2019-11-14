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
import views.behaviours.{QuestionViewBehaviours, ViewBehaviours}
import views.html.register.declaration

class DeclarationViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "declaration"
  private val cancelCall = controllers.routes.IndexController.onPageLoad()
  private val hrefCall = controllers.register.routes.DeclarationController.onClickAgree()

  private def createView = () => declaration(frontendAppConfig, cancelCall, hrefCall)(fakeRequest, messages)

  "Declaration view" must {
    behave like normalPage(createView, messageKeyPrefix)

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

    behave like pageWithContinueButton(createView, hrefCall.url, id = "submit")

    "have a cancel link" in {
      createView must haveLink(cancelCall.url, linkId = "cancel")
    }
  }

}
