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
import models.{Address, NormalMode}
import play.api.data.Form
import views.behaviours.QuestionViewBehaviours
import views.html.register.company.companyPreviousAddress

class CompanyPreviousAddressViewSpec extends QuestionViewBehaviours[Address] {

  val messageKeyPrefix = "companyPreviousAddress"

  override val form = new AddressFormProvider()()

  def createView = () => companyPreviousAddress(frontendAppConfig, form, NormalMode, Seq.empty)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => companyPreviousAddress(frontendAppConfig, form, NormalMode, Seq.empty)(fakeRequest, messages)

  "CompanyPreviousAddress view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithSecondaryHeader(createView, messages("site.secondaryHeader"))

    behave like pageWithBackLink(createView)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.company.routes.CompanyPreviousAddressController.onSubmit(NormalMode).url,
      "addressLine1", "addressLine2", "addressLine3", "addressLine4"
    )
  }
}
