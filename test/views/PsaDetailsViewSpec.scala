/*
 * Copyright 2018 HM Revenue & Customs
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

import org.jsoup.nodes.Document
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{AnswerRow, AnswerSection, SuperSection}
import views.PsaDetailsViewSpec._
import views.behaviours.{CheckYourAnswersBehaviours, ViewBehaviours}
import views.html.psa_details

class PsaDetailsViewSpec extends CheckYourAnswersBehaviours with ViewBehaviours {

  private val messageKeyPrefix = "psaDetails"

  private def emptyAnswerSections: Seq[SuperSection] = Nil

  private def secondaryHeader: String = "test-secondaryHeader"

  val fakeCall = Call("method", "url")

  def createView: () => HtmlFormat.Appendable = () =>
    psa_details(
      frontendAppConfig,
      emptyAnswerSections,
      secondaryHeader
    )(fakeRequest, messages)

  def createViewWithData: Seq[SuperSection] => HtmlFormat.Appendable = sections =>
    psa_details(
      frontendAppConfig,
      sections,
      secondaryHeader
    )(fakeRequest, messages)

  "supersection page" must {

    behave like normalPageWithoutPageTitleCheck(createView, messageKeyPrefix)

    "display the correct page title" in {
      val doc = asDocument(createView())
      assertPageTitleEqualsMessage(doc, secondaryHeader)
    }
    "display heading" in {
      val doc: Document = asDocument(createViewWithData(seqSuperSection))
      assertRenderedByIdWithText(doc, "supersection-0-heading", superSectionHeading)
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

object PsaDetailsViewSpec {
  val answer1 = "test-answer-1"
  val answer2 = "test-answer-2"
  val superSectionHeading = "Main Heading"
  val headingKey = "Director Name"

  val answerRow = AnswerRow("test-label", Seq(answer1, answer2), answerIsMessageKey = false, None)

  val answerSection = AnswerSection(Some(headingKey), rows = Seq(answerRow))

  val superSection = SuperSection(Some(superSectionHeading), Seq(answerSection))
  val superSection2 = SuperSection(Some(superSectionHeading), Seq(answerSection))

  val seqSuperSection = Seq(superSection, superSection2)
}
