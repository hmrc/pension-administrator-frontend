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

package views.register.individual

import java.time.LocalDate
import play.api.data.Form
import forms.register.individual.IndividualDateOfBirthFormProvider
import models.NormalMode
import views.behaviours.QuestionViewBehaviours
import views.html.register.individual.individualDateOfBirth

class IndividualDateOfBirthViewSpec extends QuestionViewBehaviours[LocalDate] {

  val messageKeyPrefix = "individualDateOfBirth"

  val form = new IndividualDateOfBirthFormProvider()()

  def createView = () => individualDateOfBirth(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[LocalDate]) => individualDateOfBirth(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "IndividualDateOfBirth view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, messages("site.secondaryHeader"))

    behave like pageWithDateField(
      createViewUsingForm,
      "dateOfBirth",
      messages("common.dateOfBirth"),
      Some(messages("common.dateOfBirth.hint"))
    )
  }
}
