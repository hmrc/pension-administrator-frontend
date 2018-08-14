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

import forms.register.company.CompanyDetailsFormProvider
import models.NormalMode
import models.register.company.CompanyDetails
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.company.companyDetails

class CompanyDetailsViewSpec extends QuestionViewBehaviours[CompanyDetails] {

  val messageKeyPrefix = "companyDetails"

  override val form = new CompanyDetailsFormProvider()()

  def createView: () => HtmlFormat.Appendable = () =>
    companyDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    companyDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "CompanyDetails view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, messages("site.secondaryHeader"))

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.company.routes.CompanyDetailsController.onSubmit(NormalMode).url,
      "vatRegistrationNumber",
      "payeEmployerReferenceNumber"
    )

    behave like pageWithLabel(createViewUsingForm, "vatRegistrationNumber", messages("companyDetails.vatRegistrationNumber"))

    behave like pageWithLabel(createViewUsingForm, "payeEmployerReferenceNumber", messages("companyDetails.payeEmployerReferenceNumber"))
  }
}
