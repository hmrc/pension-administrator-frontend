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

package views

import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.Section
import views.behaviours.{CheckYourAnswersBehaviours, ViewBehaviours}
import views.html.check_your_answers

class CheckYourAnswersViewSpec extends CheckYourAnswersBehaviours with ViewBehaviours {

  private val messageKeyPrefix = "checkYourAnswers"

  private def emptyAnswerSections: Seq[Section] = Nil

  val fakeCall = Call("method", "url")

  def createView: () => HtmlFormat.Appendable = () =>
    check_your_answers(
      frontendAppConfig,
      emptyAnswerSections,
      None,
      fakeCall
    )(fakeRequest, messages)

  def createViewWithData: (Seq[Section]) => HtmlFormat.Appendable = (sections) =>
    check_your_answers(
      frontendAppConfig,
      sections,
      None,
      fakeCall
    )(fakeRequest, messages)

  "check_your_answers view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithSubmitButton(createView)

    behave like checkYourAnswersPage(createViewWithData)
  }

}