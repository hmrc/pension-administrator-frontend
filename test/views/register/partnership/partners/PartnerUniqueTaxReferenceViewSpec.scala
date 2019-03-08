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

import forms.UniqueTaxReferenceFormProvider
import models.{Index, Mode, NormalMode, UpdateMode}
import play.api.data.Form
import views.behaviours.ViewBehaviours
import views.html.register.partnership.partners.partnerUniqueTaxReference

class PartnerUniqueTaxReferenceViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "partnerUniqueTaxReference"

  val form = new UniqueTaxReferenceFormProvider().apply("partnerUniqueTaxReference.error.required", "partnerUniqueTaxReference.error.reason.required")
  val partnerName = "test partner name"

  def createView(mode: Mode = NormalMode) = () => partnerUniqueTaxReference(frontendAppConfig, form, mode, Index(1), Some("test psa"))(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => partnerUniqueTaxReference(frontendAppConfig, form, NormalMode, Index(1), None)(fakeRequest, messages)

  val utrOptions = Seq("true", "false")

  "PartnerUniqueTaxReference view" must {
    behave like normalPage(createView(), messageKeyPrefix)
    behave like pageWithReturnLink(createView(mode = UpdateMode), controllers.routes.PsaDetailsController.onPageLoad().url)
  }

  "PartnerUniqueTaxReference view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- utrOptions) {
          assertContainsRadioButton(doc, s"utr_hasUtr-$option", "utr.hasUtr", option, false)
        }
      }
    }

    for (option <- utrOptions) {
      s"rendered with a value of '$option'" must {
        s"have the '$option' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("utr.hasUtr" -> s"$option"))))
          assertContainsRadioButton(doc, s"utr_hasUtr-$option", "utr.hasUtr", option, true)

          for (unselectedOption <- utrOptions.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"utr_hasUtr-$unselectedOption", "utr.hasUtr", unselectedOption, false)
          }
        }
      }
    }
  }
}
