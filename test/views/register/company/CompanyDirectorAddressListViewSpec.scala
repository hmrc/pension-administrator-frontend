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
import forms.register.company.CompanyDirectorAddressListFormProvider
import models.{Index, NormalMode}
import models.register.company.CompanyDirectorAddressList
import views.behaviours.ViewBehaviours
import views.html.register.company.companyDirectorAddressList

class CompanyDirectorAddressListViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "common.previousAddressList"

  val form = new CompanyDirectorAddressListFormProvider()()

  val firstIndex = Index(0)
  val directorName = "fullName"

  def createView = () => companyDirectorAddressList(frontendAppConfig, form, NormalMode, firstIndex, directorName)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => companyDirectorAddressList(frontendAppConfig, form, NormalMode, firstIndex, directorName)(fakeRequest, messages)

  "DirectorPreviousAddressList view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, directorName)
  }

  "DirectorPreviousAddressList view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- CompanyDirectorAddressList.options) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, false)
        }
      }
    }

    for(option <- CompanyDirectorAddressList.options) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, true)

          for(unselectedOption <- CompanyDirectorAddressList.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}