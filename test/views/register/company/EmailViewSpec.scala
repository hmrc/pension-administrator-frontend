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

package views.register.company

import controllers.register.company.routes
import forms.EmailFormProvider
import models.NormalMode
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.behaviours.QuestionViewBehaviours
import views.html.email

class EmailViewSpec extends QuestionViewBehaviours[String] {

  val messageKeyPrefix = "email"

  private val name = "Test Name"

  val form = new EmailFormProvider()()

  private val postCall: Call = routes.CompanyEmailController.onSubmit(NormalMode)

  private def viewModel: CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = postCall,
      title = Message(s"$messageKeyPrefix.title", Message("theCompany")),
      heading = Message(s"$messageKeyPrefix.heading", name),
      mode = NormalMode,
      entityName = name
    )

  val view: email = app.injector.instanceOf[email]

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

  "Email view" must {
    behave like normalPageWithDynamicTitle(
      view = createView,
      messageKeyPrefix = messageKeyPrefix,
      dynamicContent = Message("theCompany")
    )

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      postCall.url,
      "value"
    )
  }

}
