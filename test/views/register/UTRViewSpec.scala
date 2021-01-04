/*
 * Copyright 2021 HM Revenue & Customs
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

import forms.UTRFormProvider
import play.api.data.Form
import play.twirl.api.Html
import viewmodels.Message
import views.behaviours.QuestionViewBehaviours
import views.html.register.utr

class UTRViewSpec extends QuestionViewBehaviours[String] {

  val form: Form[String] = new UTRFormProvider()()

  private val messageKeyPrefix = "utr"
  private val businessType = "limited company"
  private val onwardUrl = controllers.routes.IndexController.onPageLoad()

  val view: utr = app.injector.instanceOf[utr]

  private def createView: () => Html = () => view(form, businessType, onwardUrl)(fakeRequest, messages)

  private def createViewUsingForm(form: Form[_]): Html = view(form, businessType, onwardUrl)(fakeRequest, messages)

  "UTR view" must {
    
    "display the correct browser title" in {
      val doc = asDocument(createView())
      assertEqualsMessage(doc, "title", messages(s"$messageKeyPrefix.title", businessType)  + " - " + messages("pension.scheme.administrator.title"))
    }

    "display the correct page header" in {
      val doc = asDocument(createView())
      assertPageTitleEqualsMessage(doc, Message(s"$messageKeyPrefix.heading", businessType))
    }


    "display the correct guidance" in {
      val doc = asDocument(createView())
      for (key <- Seq("p1", "p2", "p3")) assertContainsText(doc, messages(s"$messageKeyPrefix.$key"))
    }

    "show an error summary when rendered with an error" in {
      val doc = asDocument(createViewUsingForm(form.withError(error)))
      assertRenderedById(doc, "error-summary-heading")
    }

    "show an error in the value field's label when rendered with an error" in {
      val doc = asDocument(createViewUsingForm(form.withError(error)))
      val errorSpan = doc.getElementsByClass("error-notification").first
      errorSpan.text mustBe s"${messages("site.error")} ${messages(errorMessage)}"
    }

    behave like pageWithSubmitButton(createView)
  }

}
