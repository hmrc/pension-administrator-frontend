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

package views.register.company.directors

import forms.UniqueTaxReferenceFormProvider
import models.{Index, Mode, NormalMode, UpdateMode}
import play.api.data.Form
import views.behaviours.ViewBehaviours
import views.html.register.company.directors.directorUniqueTaxReference

class DirectorUniqueTaxReferenceViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "directorUniqueTaxReference"
  val index = Index(1)
  val form = new UniqueTaxReferenceFormProvider().apply("directorUniqueTaxReference.error.required", "directorUniqueTaxReference.error.reason.required")
  val directorName = "test director name"

  def createView(mode: Mode = NormalMode) = () =>
    directorUniqueTaxReference(frontendAppConfig, form, mode, index, Some("test psa"))(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) =>
    directorUniqueTaxReference(frontendAppConfig, form, NormalMode, index, None)(fakeRequest, messages)

  val utrOptions = Seq("true", "false")

  "DirectorUniqueTaxReference view" must {
    behave like normalPage(createView(), messageKeyPrefix)
    behave like pageWithReturnLink(createView(mode = UpdateMode), controllers.routes.PsaDetailsController.onPageLoad().url)
  }

  "DirectorUniqueTaxReference view" when {
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
