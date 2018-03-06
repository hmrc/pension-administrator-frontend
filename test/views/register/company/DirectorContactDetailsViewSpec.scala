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

package views.register.company

import play.api.data.Form
import controllers.register.company.routes
import forms.register.company.DirectorContactDetailsFormProvider
import models.{Index, NormalMode}
import models.register.company.DirectorContactDetails
import views.behaviours.QuestionViewBehaviours
import views.html.register.company.directorContactDetails

class DirectorContactDetailsViewSpec extends QuestionViewBehaviours[DirectorContactDetails] {

  val messageKeyPrefix = "directorContactDetails"
  val index = Index(0)
  val directorName = "test first name test middle name test last name"

  override val form = new DirectorContactDetailsFormProvider()()

  def createView = () => directorContactDetails(frontendAppConfig, form, NormalMode, index, directorName)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => directorContactDetails(frontendAppConfig, form, NormalMode, index, directorName)(fakeRequest, messages)

  "DirectorContactDetails view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, directorName)

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, controllers.register.company.routes.DirectorContactDetailsController.onSubmit(NormalMode, index).url, "email", "phone")
  }
}
