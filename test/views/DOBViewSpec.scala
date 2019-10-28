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

import forms.PersonDetailsFormProvider
import models.{Mode, NormalMode, PersonDetails, UpdateMode}
import play.api.data.Form
import play.api.mvc.Call
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.behaviours.QuestionViewBehaviours
import views.html.dob

class DOBViewSpec extends QuestionViewBehaviours[PersonDetails] {

  private val messageKeyPrefix = "dob"

  override val form = new PersonDetailsFormProvider()()

  private def viewModel(mode: Mode = NormalMode) =
    CommonFormWithHintViewModel(
      postCall = Call("POST", "http://www.test.com"),
      title = "directorDob.title",
      heading = Message("dob.heading"),
      None,
      None,
      mode,
      "test psa"
    )

  private def createView(mode: Mode = NormalMode) = () =>
    dob(frontendAppConfig, form, viewModel(mode))(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) =>
    dob(frontendAppConfig, form, viewModel())(fakeRequest, messages)

  "PersonDetails view" must {

    behave like normalPageWithNoPageTitleCheck(createView(), messageKeyPrefix)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.company.directors.routes.DirectorNameController.onSubmit(NormalMode, 0).url
    )

    behave like pageWithDateField(
      createViewUsingForm,
      "value",
      messages("common.dateOfBirth"),
      Some(messages("common.dateOfBirth.hint"))
    )

  }

}
