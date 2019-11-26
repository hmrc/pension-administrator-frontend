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

import play.api.mvc.Call
import views.behaviours.ViewBehaviours
import views.html.register.variationDeclaration

class VariationDeclarationViewSpec extends ViewBehaviours {

  private val messageKeyPrefix = "declaration.variations"

  private def returnLink: String = controllers.routes.PsaDetailsController.onPageLoad().url
  private val href: Call = controllers.register.routes.VariationDeclarationController.onClickAgree()

  val view: variationDeclaration = app.injector.instanceOf[variationDeclaration]

  private def createView(isWorkingKnowledge: Boolean = true) = () => view(
    Some("Mark Wright"), isWorkingKnowledge, href)(fakeRequest, messages)

  "Declaration variation view" must {
    appRunning()
    behave like normalPage(createView(), messageKeyPrefix)

    behave like pageWithReturnLink(createView(), returnLink)

    "display the declaration" in {
      createView() must haveDynamicText("declaration.variations.statement0")
    }

    "display the first statement" in {
      createView() must haveDynamicText("declaration.variations.statement1")
    }

    "display the second statement" in {
      createView() must haveDynamicText("declaration.variations.statement2")
    }

    "display the thirdOne statement" in {
      createView() must haveDynamicText("declaration.variations.statement31")
      createView() must notHaveDynamicText("declaration.variations.statement32")
    }

    "display the thirdTwo statement" in {
      createView(isWorkingKnowledge = false) must notHaveDynamicText("declaration.variations.statement31")
      createView(isWorkingKnowledge = false) must haveDynamicText("declaration.variations.statement32")
    }

    "display the fourth statement" in {
      createView() must haveDynamicText("declaration.variations.statement4")
    }

    behave like pageWithContinueButton(createView(), href.url, id = "submit")
  }
  app.stop()
}
