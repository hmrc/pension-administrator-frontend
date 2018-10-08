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

import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.SuperSection
import views.behaviours.{CheckYourAnswersBehaviours, ViewBehaviours}
import views.html.{check_your_answers, psa_details}

class PsaDetailsViewSpec extends CheckYourAnswersBehaviours with ViewBehaviours {

  private val messageKeyPrefix = "checkYourAnswers"

  private def emptyAnswerSections: Seq[SuperSection] = Nil

  private def secondaryHeader: String = "test-secondaryHeader"

  val fakeCall = Call("method", "url")

  def createView: () => HtmlFormat.Appendable = () =>
    psa_details(
      frontendAppConfig,
      emptyAnswerSections,
      secondaryHeader,
      fakeCall
    )(fakeRequest, messages)

  def createViewWithData: Seq[SuperSection] => HtmlFormat.Appendable = sections =>
    psa_details(
      frontendAppConfig,
      sections,
      secondaryHeader,
      fakeCall
    )(fakeRequest, messages)

  "check_your_answers view" must {
    behave like normalPageWithoutPageTitleCheck(createView, messageKeyPrefix)

    "display the correct page title" in {
      val doc = asDocument(createView())
      assertPageTitleEqualsMessage(doc, secondaryHeader)
    }

    behave like superSectionPage(createViewWithData)
  }

}