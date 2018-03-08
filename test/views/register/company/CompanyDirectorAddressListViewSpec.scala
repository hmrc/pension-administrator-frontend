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
import models.{Address, Index, NormalMode}
import views.behaviours.ViewBehaviours
import views.html.register.company.companyDirectorAddressList

class CompanyDirectorAddressListViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "common.selectAddress"

  val form = new CompanyDirectorAddressListFormProvider()(Seq.empty)

  val firstIndex = Index(0)
  val directorName = "fullName"

  val addressIndices: Seq[Int] = Seq.range(0, 2)
  val addresses = Seq(
    address("test post code 1"),
    address("test post code 2")
  )

  def address(postCode: String): Address = Address("address line 1", "address line 2", Some("test town"),
    Some("test county"), postcode = Some(postCode), country = "United Kingdom")

  def createView = () => companyDirectorAddressList(
    frontendAppConfig,
    form,
    NormalMode,
    firstIndex,
    directorName,
    addresses
  )(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => companyDirectorAddressList(
    frontendAppConfig,
    form,
    NormalMode,
    firstIndex,
    directorName,
    addresses
  )(fakeRequest, messages)

  "CompanyDirectorAddressList view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, directorName)

    "have link for enter address manually" in {
      createView must haveLink(
        controllers.register.company.routes.DirectorAddressController.onPageLoad(NormalMode, firstIndex).url,
        "manual-address-link"
      )
    }
  }

  "CompanyDirectorAddressList view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- addressIndices) {
          assertContainsRadioButton(doc, s"value-$option", "value", option.toString, false)
        }
      }
    }

    for(option <- addressIndices) {
      s"rendered with a value of '$option'" must {
        s"have the '$option' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"$option"))))
          assertContainsRadioButton(doc, s"value-$option", "value", option.toString, true)

          for(unselectedOption <- addressIndices.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-$unselectedOption", "value", unselectedOption.toString, false)
          }
        }
      }
    }
  }
}
