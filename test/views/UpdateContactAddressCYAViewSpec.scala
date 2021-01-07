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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{PsaViewDetailsViewModel, SuperSection}
import views.PsaDetailsViewSpec._
import views.behaviours.{ViewBehaviours, CheckYourAnswersBehaviours}
import views.html.updateContactAddressCYA

class UpdateContactAddressCYAViewSpec extends CheckYourAnswersBehaviours with ViewBehaviours {

  private val messageKeyPrefix = "updateContactAddressCYA"

  private def emptyAnswerSections: Seq[SuperSection] = Nil

  private val psaName: String = "test-psa-name"

  private val fakeCall = Call("method", "url")

  private val view: updateContactAddressCYA = app.injector.instanceOf[updateContactAddressCYA]

  private val title = "test-title"

  private def createView(isUserAnswerUpdated: Boolean = false,
                 userAnswersInCompleteAlert: Option[String] = Some("incomplete.alert.message")): () => HtmlFormat.Appendable = () =>
    view(
      PsaViewDetailsViewModel(emptyAnswerSections, psaName, isUserAnswerUpdated, userAnswersInCompleteAlert, title = title),
      fakeCall
    )(fakeRequest, messages)

  private def createViewWithData: Seq[SuperSection] => HtmlFormat.Appendable = sections =>
    view(
      PsaViewDetailsViewModel(sections, psaName, isUserAnswerUpdated = false,
        userAnswersIncompleteMessage = Some("incomplete.alert.message"), title = title),
      fakeCall
    )(fakeRequest, messages)

  "UpdateContactAddressCYAView page" must {
    "display the correct heading" in {
      val doc = asDocument(createView()())
      assertPageTitleEqualsMessage(doc, messages(s"$messageKeyPrefix.heading", psaName))
    }

    "display the declaration button when user answer is updated" in {
      val doc = Jsoup.parse(createView(isUserAnswerUpdated = true).apply().toString())
      doc must haveLinkWithUrlAndContent(
        "declaration-link",
        fakeCall.url,
        messages("psaDetails.declaration.link.text")
      )
    }

    "correctly display an AnswerSection" in {
      val doc: Document = asDocument(createViewWithData(seqSuperSection))
      assertRenderedByIdWithText(doc, "cya-0-0-heading", headingKey)
      assertRenderedByIdWithText(doc, "cya-0-0-0-question", answerRow.label)
      assertRenderedByIdWithText(doc, "cya-0-0-0-0-answer", answer1)
      assertRenderedByIdWithText(doc, "cya-0-0-0-1-answer", answer2)
    }

    "display the correct number of sections" in {
      val doc: Document = asDocument(createViewWithData(seqSuperSection))

      assertRenderedById(doc, "supersection-0-heading")
      assertRenderedById(doc, "supersection-1-heading")
      assertNotRenderedById(doc, "supersection-2-heading")
    }
  }
}
