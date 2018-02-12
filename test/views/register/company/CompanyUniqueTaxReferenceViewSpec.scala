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

import forms.register.company.CompanyUniqueTaxReferenceFormProvider
import models.NormalMode
import play.api.data.Form
import views.behaviours.StringViewBehaviours
import views.html.register.company.companyUniqueTaxReference

class CompanyUniqueTaxReferenceViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "companyUniqueTaxReference"
  val expectedHintKey = "companyUniqueTaxReference.hint"
  val expectedBodyText = "companyUniqueTaxReference.body"

  val form = new CompanyUniqueTaxReferenceFormProvider()()

  def createView = () => companyUniqueTaxReference(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[String]) => companyUniqueTaxReference(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "CompanyUniqueTaxReference view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like stringPage(createViewUsingForm, messageKeyPrefix,
      controllers.register.company.routes.CompanyUniqueTaxReferenceController.onSubmit(NormalMode).url, Some(expectedHintKey) )

    "display correct body text" in {

      val doc = asDocument(createView())
      assertContainsText(doc, messages(expectedBodyText))

    }
  }
}
