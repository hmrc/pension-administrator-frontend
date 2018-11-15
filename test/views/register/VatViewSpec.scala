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

package views.register

import forms.register.VatFormProvider
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{Message, VatViewModel}
import views.behaviours.ViewBehaviours
import views.html.register.vat

class VatViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "partnershipVat"

  val form = new VatFormProvider()()

  val viewmodel = VatViewModel(
    postCall = Call("GET", "/"),
    title = Message("partnershipVat.title"),
    heading = Message("partnershipVat.heading"),
    hint = Message("partnershipVat.hint"),
    subHeading = None
  )

  def createView: () => HtmlFormat.Appendable = () =>
    vat(frontendAppConfig, form, viewmodel)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    vat(frontendAppConfig, form, viewmodel)(fakeRequest, messages)

  "Vat view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

  }


  "Vat view" when {
    "rendered" must {
      val vatOptions = Seq("true", "false")

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- vatOptions) {
          assertContainsRadioButton(doc, s"vat_hasVat-$option", "vat.hasVat", option, isChecked = false)
        }
      }


      for (option <- vatOptions) {
        s"rendered with a value of '$option'" must {
          s"have the '$option' radio button selected" in {
            val doc = asDocument(createViewUsingForm(form.bind(Map("vat.hasVat" -> s"$option"))))
            assertContainsRadioButton(doc, s"vat_hasVat-$option", "vat.hasVat", option, isChecked = true)

            for (unselectedOption <- vatOptions.filterNot(o => o == option)) {
              assertContainsRadioButton(doc, s"vat_hasVat-$unselectedOption", "vat.hasVat", unselectedOption, isChecked = false)
            }
          }
        }
      }
    }
  }
}
