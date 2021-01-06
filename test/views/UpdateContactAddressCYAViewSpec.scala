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

import models.UpdateMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{AnswerSection, PsaViewDetailsViewModel, SuperSection, AnswerRow}
import views.PsaDetailsViewSpec._
import views.behaviours.{ViewBehaviours, CheckYourAnswersBehaviours}
import views.html.updateContactAddressCYA

class UpdateContactAddressCYAViewSpec extends CheckYourAnswersBehaviours with ViewBehaviours {

  private val messageKeyPrefix = "updateContactAddressCYA"

  private def emptyAnswerSections: Seq[SuperSection] = Nil

  private def secondaryHeader: String = "test-secondaryHeader"

  val fakeCall = Call("method", "url")

  val view: updateContactAddressCYA = app.injector.instanceOf[updateContactAddressCYA]

  def createView(isUserAnswerUpdated: Boolean = false,
                 userAnswersInCompleteAlert: Option[String] = Some("incomplete.alert.message")): () => HtmlFormat.Appendable = () =>
    view(
      PsaViewDetailsViewModel(emptyAnswerSections, secondaryHeader, isUserAnswerUpdated, userAnswersInCompleteAlert),
      controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(UpdateMode)
    )(fakeRequest, messages)

  def createViewWithData: Seq[SuperSection] => HtmlFormat.Appendable = sections =>
    view(
      PsaViewDetailsViewModel(sections, secondaryHeader, isUserAnswerUpdated = false, userAnswersIncompleteMessage = Some("incomplete.alert.message")),
      controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(UpdateMode)
    )(fakeRequest, messages)

  "UpdateContactAddressCYAView page" must {
    "display the correct heading" in {
      val doc = asDocument(createView()())
      assertPageTitleEqualsMessage(doc, messages(s"$messageKeyPrefix.heading", "a"))
    }

    "display the declaration button when user answer is updated" in {
      val doc = Jsoup.parse(createView(isUserAnswerUpdated = true).apply().toString())
      doc must haveLinkWithUrlAndContent(
        "declaration-link",
        controllers.register.routes.VariationWorkingKnowledgeController.onPageLoad(UpdateMode).url,
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

object UpdateContactAddressCYAViewSpec {
  val answer1 = "test-answer-1"
  val answer2 = "test-answer-2"
  val superSectionHeading = "Main Heading"
  val subSectionHeading = "Pension scheme administrator details"
  val headingKey = "Director Name"

  val answerRow = AnswerRow("test-label", Seq(answer1, answer2), answerIsMessageKey = false, None)

  val answerSection = AnswerSection(Some(headingKey), rows = Seq(answerRow))

  val superSection = SuperSection(Some(superSectionHeading), Seq(answerSection))
  val superSection2 = SuperSection(Some(superSectionHeading), Seq(answerSection))

  val seqSuperSection: Seq[SuperSection] = Seq(superSection, superSection2)
}
