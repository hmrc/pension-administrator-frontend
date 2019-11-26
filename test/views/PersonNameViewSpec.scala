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

import forms.PersonNameFormProvider
import models.{Mode, NormalMode, PersonName, UpdateMode}
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.behaviours.QuestionViewBehaviours
import views.html.personName

class PersonNameViewSpec extends QuestionViewBehaviours[PersonName] {

  private val messageKeyPrefix = "directorName"

  override val form = new PersonNameFormProvider()()

  private lazy val viewModel =
    CommonFormWithHintViewModel(
      postCall = Call("POST", "http://www.test.com"),
      title = "directorName.heading",
      heading = Message("directorName.heading"),
      None,
      None,
      NormalMode,
      "test psa"
    )

  val view: personName = app.injector.instanceOf[personName]

  private def createView(mode: Mode = NormalMode): () => HtmlFormat.Appendable = () =>
    view(form, viewModel, mode)(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, viewModel, NormalMode)(fakeRequest, messages)

  "PersonName view" must {

    behave like normalPageWithNoPageTitleCheck(createView(), messageKeyPrefix)

    behave like pageWithReturnLink(createView(mode = UpdateMode), controllers.routes.PsaDetailsController.onPageLoad().url)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.company.directors.routes.DirectorNameController.onSubmit(NormalMode, 0).url,
      "firstName",
      "lastName"
    )

  }
  app.stop()
}
