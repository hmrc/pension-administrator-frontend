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

import forms.AddressFormProvider
import models.{Address, Index, NormalMode}
import play.api.data.Form
import utils.InputOption
import views.behaviours.QuestionViewBehaviours
import views.html.register.company.directors.directorPreviousAddress

class DirectorPreviousAddressViewSpec extends QuestionViewBehaviours[Address] {

  val messageKeyPrefix = "directorPreviousAddress"
  val index = Index(0)
  val directorName = "test first name test middle name test last name"
  val countryOptions : Seq[InputOption] = Seq.empty

  override val form = new AddressFormProvider()()

  def createView = () => directorPreviousAddress(frontendAppConfig, form, NormalMode, index, directorName, countryOptions)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => directorPreviousAddress(frontendAppConfig, form, NormalMode, index, directorName, countryOptions)(fakeRequest, messages)

  "DirectorPreviousAddress view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, directorName)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.company.directors.routes.DirectorPreviousAddressController.onSubmit(NormalMode, index).url,
      "addressLine1", "addressLine2", "addressLine3", "addressLine4")
  }
}
