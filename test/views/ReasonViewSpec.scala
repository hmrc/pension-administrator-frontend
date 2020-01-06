/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.ReasonFormProvider
import models.NormalMode
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.behaviours.QuestionViewBehaviours
import views.html.reason

class ReasonViewSpec extends QuestionViewBehaviours[String] {

  val messageKeyPrefix = "whyNoNINO"
  val entityName = "test entity"

  val form = new ReasonFormProvider()(entityName)
  val postCall = Call("GET", "/")

  def viewModel: CommonFormWithHintViewModel = CommonFormWithHintViewModel(
    postCall = postCall,
    title = Message("title"),
    heading = Message("header"),
    mode = NormalMode,
    entityName = entityName
  )

  val view: reason = app.injector.instanceOf[reason]

  def createView(): () => HtmlFormat.Appendable = () =>
    view(form, viewModel)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, viewModel)(fakeRequest, messages)

  "Reason view" when {
    "rendered" must {
      behave like normalPageWithTitle(createView(), messageKeyPrefix,
        title = Message("title"),
        pageHeader = Message("header"))

      behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, postCall.url,
        fields = "value")
    }
  }

}
