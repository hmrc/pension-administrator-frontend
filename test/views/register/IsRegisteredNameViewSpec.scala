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

import controllers.register.routes
import forms.register.{AreYouInUKFormProvider, IsRegisteredNameFormProvider}
import models.{CheckMode, Mode, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewmodels.{AreYouInUKViewModel, CommonFormViewModel, Message}
import views.behaviours.{ViewBehaviours, YesNoViewBehaviours}
import views.html.register.{areYouInUK, isRegisteredName}

class IsRegisteredNameViewSpec extends ViewBehaviours with YesNoViewBehaviours {

  private val messageKeyPrefix = "isRegisteredName.company"

  val formProvider = new IsRegisteredNameFormProvider()

  val form: Form[Boolean] = formProvider("isRegisteredName.company")

  private def viewmodel(mode: Mode) =
    CommonFormViewModel(
      mode,
      postCall = controllers.register.company.routes.CompanyIsRegisteredNameController.onSubmit(),
      title = Message("isRegisteredName.company.title"),
      heading = Message("isRegisteredName.company.heading")
    )

  val view: isRegisteredName = app.injector.instanceOf[isRegisteredName]

  private def createView(mode: Mode = NormalMode): () => HtmlFormat.Appendable = () =>
    view(
      form,
      viewmodel(mode)
    )(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(
      form,
      viewmodel(NormalMode)
    )(fakeRequest, messages)

  "IsRegisteredName view" must {
    behave like normalPage(createView(CheckMode), messageKeyPrefix)

    behave like pageWithSubmitButton(createView())

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix, viewmodel(NormalMode).postCall.url, s"$messageKeyPrefix.heading")

  }
  app.stop()
}
