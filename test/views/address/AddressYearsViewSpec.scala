/*
 * Copyright 2020 HM Revenue & Customs
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

package views.address

import forms.address.AddressYearsFormProvider
import models.{AddressYears, Mode, NormalMode, UpdateMode}
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.Message
import viewmodels.address.AddressYearsViewModel
import views.behaviours.ViewBehaviours
import views.html.address.addressYears

class AddressYearsViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "addressYears"

  val form = new AddressYearsFormProvider()("error")
  val name = "Name"
  val title = Message("addressYears.heading", Message("theCompany"))
  val heading = Message("addressYears.heading", name)
  val viewmodel = AddressYearsViewModel(
    postCall = Call("GET", "www.example.com"),
    title = title,
    heading = heading,
    legend = heading,
    psaName = Some("test psa")
  )

  val view: addressYears = app.injector.instanceOf[addressYears]

  def createView(mode: Mode = NormalMode): () => HtmlFormat.Appendable = () => view(form, viewmodel, mode)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, viewmodel, NormalMode)(fakeRequest, messages)

  "AddressYears view" must {
    behave like normalPageWithTitle(createView(), messageKeyPrefix, title, heading)
    behave like pageWithReturnLink(createView(mode = UpdateMode), controllers.routes.PsaDetailsController.onPageLoad().url)
  }

  "AddressYears view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- viewmodel.inputs) {
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, isChecked = false)
        }
      }
    }

    for (option <- viewmodel.inputs) {
      s"rendered with a value of '${option.value}'" must {
        s"have the '${option.value}' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("value" -> s"${option.value}"))))
          assertContainsRadioButton(doc, s"value-${option.value}", "value", option.value, isChecked = true)

          for (unselectedOption <- AddressYears.options.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"value-${unselectedOption.value}", "value", unselectedOption.value, isChecked = false)
          }
        }
      }
    }
  }

}

