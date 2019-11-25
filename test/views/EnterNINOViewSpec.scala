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

import controllers.register.company.routes
import forms.register.NINOFormProvider
import models.{NormalMode, ReferenceValue}
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.behaviours.QuestionViewBehaviours
import views.html.enterNINO

class EnterNINOViewSpec extends QuestionViewBehaviours[ReferenceValue] {

  val messageKeyPrefix = "nino"

  private val name = "Test Name"

  val form = new NINOFormProvider()(name)

  private val postCall: Call = routes.CompanyEnterVATController.onSubmit(NormalMode)

  private def viewModel: CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = postCall,
      title = Message(s"$messageKeyPrefix.title", Message("theCompany").resolve),
      heading = Message(s"$messageKeyPrefix.heading", name),
      mode = NormalMode,
      entityName = name
    )

  val view: enterNINO = app.injector.instanceOf[enterNINO]

  def createView: () => HtmlFormat.Appendable = () =>
    view(
      form,
      viewModel
    )(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(
      form,
      viewModel
    )(fakeRequest, messages)

  "EnterNINO view" must {
    behave like normalPageWithDynamicTitle(
      view = createView,
      messageKeyPrefix = messageKeyPrefix,
      dynamicContent = Message("theCompany").resolve
    )

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      postCall.url,
      fields = "value"
    )
  }
}
