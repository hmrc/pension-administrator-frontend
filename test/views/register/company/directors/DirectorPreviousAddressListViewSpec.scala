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

import forms.register.company.directors.DirectorPreviousAddressListFormProvider
import models.{Address, Index, NormalMode, TolerantAddress}
import play.api.data.Form
import views.behaviours.ViewBehaviours
import views.html.register.company.directors.directorPreviousAddressList

class DirectorPreviousAddressListViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "common.previousAddressList"

  val form = new DirectorPreviousAddressListFormProvider()(Seq.empty)

  val firstIndex = Index(0)
  val directorName = "fullName"

  val addressIndices: Seq[Int] = Seq.range(0, 2)
  val addresses = Seq(
    address("test post code 1"),
    address("test post code 2")
  )

  def address(postCode: String): TolerantAddress = TolerantAddress(
    Some("address line 1"),
    Some("address line 2"),
    Some("test town"),
    Some("test county"),
    Some(postCode),
    Some("United Kingdom")
  )

  def createView = () => directorPreviousAddressList(
    frontendAppConfig,
    form,
    NormalMode,
    firstIndex,
    directorName,
    addresses
  )(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => directorPreviousAddressList(
    frontendAppConfig,
    form,
    NormalMode,
    firstIndex,
    directorName,
    addresses
  )(fakeRequest, messages)

  "DirectorPreviousAddressList view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, directorName)

    "have link for enter address manually" in {
      createView must haveLink(
        controllers.register.company.directors.routes.DirectorPreviousAddressController.onPageLoad(NormalMode, firstIndex).url,
        "manual-address-link"
      )
    }
  }

  "DirectorPreviousAddressList view" when {
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
