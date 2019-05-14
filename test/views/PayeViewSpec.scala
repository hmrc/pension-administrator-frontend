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

package views

import controllers.register.partnership.routes
import forms.PayeFormProvider
import models.NormalMode
import play.api.data.Form
import viewmodels.{Message, PayeViewModel}
import views.behaviours.ViewBehaviours
import views.html.paye

class PayeViewSpec extends ViewBehaviours {

  val messageKeyPrefix = "partnershipPaye"
  val form = new PayeFormProvider()()

  def viewmodel = PayeViewModel(
    postCall = routes.PartnershipPayeController.onSubmit(NormalMode),
    title = Message("partnershipPaye.title"),
    heading = Message("partnershipPaye.heading"),
    hint = Some("common.paye.hint")
  )

  private def createView = () => paye(frontendAppConfig, form, viewmodel)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) => paye(frontendAppConfig, form, viewmodel)(fakeRequest, messages)

  val payeOptions = Seq("true", "false")

  "Paye view" must {
    behave like normalPage(createView, messageKeyPrefix)
  }

  "Paye view" when {
    "rendered" must {
      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- payeOptions) {
          assertContainsRadioButton(doc, s"paye_hasPaye-$option", "paye.hasPaye", option, isChecked = false)
        }
      }
    }

    for (option <- payeOptions) {
      s"rendered with a value of '$option'" must {
        s"have the '$option' radio button selected" in {
          val doc = asDocument(createViewUsingForm(form.bind(Map("paye.hasPaye" -> s"$option"))))
          assertContainsRadioButton(doc, s"paye_hasPaye-$option", "paye.hasPaye", option, isChecked = true)

          for (unselectedOption <- payeOptions.filterNot(o => o == option)) {
            assertContainsRadioButton(doc, s"paye_hasPaye-$unselectedOption", "paye.hasPaye", unselectedOption, isChecked = false)
          }
        }
      }
    }
  }
}
