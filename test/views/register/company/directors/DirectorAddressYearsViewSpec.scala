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

import forms.register.company.directors.DirectorAddressYearsFormProvider
import models.register.company.directors.DirectorAddressYears
import models.{Index, NormalMode}
import play.api.data.Form
import views.behaviours.ViewBehaviours
import views.html.register.company.directors.directorAddressYears

class DirectorAddressYearsViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "directorAddressYears"

  val form = new DirectorAddressYearsFormProvider()()
  val index = Index(0)
  val directorName = "test first name test middle name test last name"

  def createView = () => directorAddressYears(frontendAppConfig, form, NormalMode, index, directorName)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => directorAddressYears(frontendAppConfig, form, NormalMode, index, directorName)(fakeRequest, messages)

  "DirectorAddressYears view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)
  }

  "DirectorAddressYears view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- DirectorAddressYears.options) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, false)
        }
      }
    }

    for(option <- DirectorAddressYears.options) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, true)

          for(unselectedOption <- DirectorAddressYears.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}
