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

import forms.register.company.BusinessDetailsFormProvider
import models.NormalMode
import models.register.company.BusinessDetails
import play.api.data.Form
import views.behaviours.QuestionViewBehaviours
import views.html.register.company.businessDetails

class BusinessDetailsViewSpec extends QuestionViewBehaviours[BusinessDetails] {

  val messageKeyPrefix = "businessDetails"

  val form = new BusinessDetailsFormProvider()()

  def createView = () => businessDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => businessDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "CompanyUniqueTaxReference view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, messages("site.secondaryHeader"))

    behave like pageWithTextFields(createViewUsingForm, messageKeyPrefix, controllers.register.company.routes.CompanyDetailsController.onSubmit(NormalMode).url,
      "companyName", "utr")

    behave like pageWithLabel(createViewUsingForm, "companyName", messages("businessDetails.companyName"))

    behave like pageWithLabel(createViewUsingForm, "utr", messages("businessDetails.utr"))

  }
}
