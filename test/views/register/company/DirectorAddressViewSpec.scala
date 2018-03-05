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

import forms.AddressFormProvider
import models.{Address, Index, NormalMode}
import play.api.data.Form
import utils.InputOption
import views.behaviours.QuestionViewBehaviours
import views.html.register.company.directorAddress

class DirectorAddressViewSpec extends QuestionViewBehaviours[Address] {

  val messageKeyPrefix = "directorAddress"

  val firstIndex = Index(1)

  val fullName = "directorName"

  val options = Seq.empty[InputOption]

  override val form = new AddressFormProvider()()

  def createView = () => directorAddress(frontendAppConfig, form, NormalMode, firstIndex, fullName, options)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => directorAddress(frontendAppConfig, form, NormalMode, firstIndex, fullName, options)(fakeRequest, messages)

  "directorAddress view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.company.routes.DirectorAddressController.onSubmit(NormalMode, firstIndex).url,
      "addressLine1", "addressLine2", "addressLine3", "addressLine4"
    )

    behave like pageWithSecondaryHeader(createView, fullName)
  }
}
