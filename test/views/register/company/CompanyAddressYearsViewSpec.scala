/*
 * Copyright 2019 HM Revenue & Customs
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

import forms.address.AddressYearsFormProvider
import models.{AddressYears, NormalMode, TolerantAddress}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import utils.countryOptions.CountryOptions
import views.behaviours.ViewBehaviours
import views.html.register.company.companyAddressYears

class CompanyAddressYearsViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "companyAddressYears"

  val address = TolerantAddress(
    Some("add1"), Some("add2"),
    None, None,
    Some("NE11NE"), Some("GB")
  )

  val form = new AddressYearsFormProvider()("companyAddressYears.error.required")
  val countryOptions = new CountryOptions(environment, frontendAppConfig)

  def createView: () => HtmlFormat.Appendable = () => companyAddressYears(frontendAppConfig, address, form, NormalMode, countryOptions)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    companyAddressYears(
      frontendAppConfig,
      address,
      form,
      NormalMode,
      countryOptions
    )(fakeRequest, messages)

  "CompanyAddressYears view" must {
    behave like normalPage(createView, messageKeyPrefix)
  }

  "CompanyAddressYears view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- AddressYears.options) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, false)
        }
      }
    }

    for (option <- AddressYears.options) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, true)

          for (unselectedOption <- AddressYears.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, false)
          }
        }
      }
    }
  }
}
