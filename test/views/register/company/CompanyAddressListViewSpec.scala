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
import forms.register.company.CompanyAddressListFormProvider
import models.NormalMode
import models.Address
import views.behaviours.ViewBehaviours
import views.html.register.company.companyAddressList

class CompanyAddressListViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "companyAddressList"
  val companyName = "ThisCompanyName"
  val addressIndexes = Seq.range(0, 2)
  val addresses = Seq(
    address("test post code 1"),
    address("test post code 2")
  )

  def address(postCode: String): Address = Address("address line 1", "address line 2", Some("test town"),
    Some("test county"), postcode = Some(postCode), country = "United Kingdom")

  val form = new CompanyAddressListFormProvider()(Seq.empty)

  def createView = () => companyAddressList(frontendAppConfig, form, NormalMode, companyName, addresses)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => companyAddressList(frontendAppConfig, form, NormalMode, companyName, addresses)(fakeRequest, messages)

  "CompanyAddressList view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, messages("site.secondaryHeader") )

    "have link for enter address manually" in {
      createView must haveLink(
        controllers.register.company.routes.CompanyPreviousAddressController.onPageLoad(NormalMode).url,
        "manual-address-link"
      )
    }
  }

  "CompanyAddressList view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- addressIndexes) {
          assertContainsRadioButton(doc, s"value-${option}", "value", option.toString, false)
        }
      }
    }

    for(option <- addressIndexes) {
      s"rendered with a value of '${option}'" must {
        s"have the '${option}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option}"))))
          assertContainsRadioButton(doc, s"value-${option}", "value", option.toString, true)

          for(unselectedOption <- addressIndexes.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption}", "value", unselectedOption.toString, false)
          }
        }
      }
    }
  }
}
