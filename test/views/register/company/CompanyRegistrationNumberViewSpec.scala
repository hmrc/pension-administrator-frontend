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

import forms.register.company.CompanyRegistrationNumberFormProvider
import models.NormalMode
import play.api.data.Form
import views.behaviours.StringViewBehaviours
import views.html.register.company.companyRegistrationNumber

class CompanyRegistrationNumberViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "companyRegistrationNumber"

  val form = new CompanyRegistrationNumberFormProvider()()

  def createView = () => companyRegistrationNumber(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[String]) => companyRegistrationNumber(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "CompanyRegistrationNumber view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, messages("site.secondaryHeader"))

    behave like stringPage(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.company.routes.CompanyRegistrationNumberController.onSubmit(NormalMode).url,
      Some(s"$messageKeyPrefix.hint")
    )
  }
}
