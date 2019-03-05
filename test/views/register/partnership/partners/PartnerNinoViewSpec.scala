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

package views.register.partnership.partners

import forms.register.partnership.partners.PartnerNinoFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.register.partnership.partners.partnerNino

class PartnerNinoViewSpec extends ViewBehaviours {

  val index = Index(1)
  val partnerName = "test name"
  val messageKeyPrefix = "partnerNino"

  val form = new PartnerNinoFormProvider()()

  def createView: () => HtmlFormat.Appendable = () =>
    partnerNino(frontendAppConfig, form, NormalMode, index, partnerName)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    partnerNino(frontendAppConfig, form, NormalMode, index, partnerName)(fakeRequest, messages)

  "PartnerNino view" must {
    behave like normalPage(createView, messageKeyPrefix)
  }

  "PartnerNino view" when {
    "rendered" must {
      val ninoOptions = Seq("true", "false")

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- ninoOptions) {
          assertContainsRadioButton(doc, s"nino_hasNino-$option", "nino.hasNino", option, isChecked = false)
        }
      }


      for (option <- ninoOptions) {
        s"rendered with a value of '$option'" must {
          s"have the '$option' radio button selected" in {
            val doc = asDocument(createViewUsingForm(form.bind(Map("nino.hasNino" -> s"$option"))))
            assertContainsRadioButton(doc, s"nino_hasNino-$option", "nino.hasNino", option, isChecked = true)

            for (unselectedOption <- ninoOptions.filterNot(o => o == option)) {
              assertContainsRadioButton(doc, s"nino_hasNino-$unselectedOption", "nino.hasNino", unselectedOption, isChecked = false)
            }
          }
        }
      }
    }
  }
}
