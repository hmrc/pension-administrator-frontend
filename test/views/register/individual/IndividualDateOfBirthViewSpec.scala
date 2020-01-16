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

package views.register.individual

import java.time.LocalDate

import forms.register.individual.IndividualDateOfBirthFormProvider
import models.NormalMode
import play.api.data.Form
import play.twirl.api.Html
import views.behaviours.QuestionViewBehaviours
import views.html.register.individual.individualDateOfBirth

class IndividualDateOfBirthViewSpec extends QuestionViewBehaviours[LocalDate] {

  val messageKeyPrefix = "individualDateOfBirth"

  val form = new IndividualDateOfBirthFormProvider()()

  val view: individualDateOfBirth = app.injector.instanceOf[individualDateOfBirth]

  private def createView: () => Html = () =>
    view(form, NormalMode)(fakeRequest, messages)

  private def createViewUsingForm: Form[LocalDate] => Html = (form: Form[LocalDate]) =>
    view(form, NormalMode)(fakeRequest, messages)

  "IndividualDateOfBirth view" must {
    "behave like a normal page" when {
      "rendered" must {
        behave like normalPageWithTitle(createView, messageKeyPrefix, messages("individualDateOfBirth.heading"), messages("individualDateOfBirth.heading"))
      }
    }

    behave like pageWithDateField(
      createViewUsingForm,
      "dateOfBirth",
      messages("common.dateOfBirth"),
      Some(messages("common.dateOfBirth.hint"))
    )
  }

}
