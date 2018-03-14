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

package views.register.company.directors

import forms.register.company.directors.DirectorDetailsFormProvider
import models.NormalMode
import models.register.company.directors.DirectorDetails
import play.api.data.Form
import views.behaviours.QuestionViewBehaviours
import views.html.register.company.directors.directorDetails

class DirectorDetailsViewSpec extends QuestionViewBehaviours[DirectorDetails] {

  private val messageKeyPrefix = "directorDetails"

  override val form = new DirectorDetailsFormProvider()()

  private def createView = () => directorDetails(frontendAppConfig, form, NormalMode, 0)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) => directorDetails(frontendAppConfig, form, NormalMode, 0)(fakeRequest, messages)

  "DirectorDetails view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, messages("site.secondaryHeader") )

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.company.directors.routes.DirectorDetailsController.onSubmit(NormalMode, 0).url,
      "firstName",
      "middleName",
      "lastName"
    )

    behave like pageWithDateField(
      createViewUsingForm,
      "dateOfBirth",
      messages("directorDetails.dateOfBirth"),
      Some(messages("directorDetails.dateOfBirth.hint"))
    )

  }

}
