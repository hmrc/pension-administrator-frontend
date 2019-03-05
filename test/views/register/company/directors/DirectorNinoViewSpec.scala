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

import forms.register.company.directors.DirectorNinoFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import views.behaviours.ViewBehaviours
import views.html.register.company.directors.directorNino

class DirectorNinoViewSpec extends ViewBehaviours {

  val index = Index(1)
  val directorName = "test name"
  val messageKeyPrefix = "directorNino"

  val form = new DirectorNinoFormProvider()()

  def createView = () => directorNino(frontendAppConfig, form, NormalMode, index, directorName)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => directorNino(frontendAppConfig, form, NormalMode, index, directorName)(fakeRequest, messages)

  "DirectorNino view" must {
    behave like normalPage(createView, messageKeyPrefix)
  }

  "DirectorNino view" when {
    "rendered" must {
      val ninoOptions = Seq("true", "false")

      "contain radio buttons for the value" in {
        val doc = asDocument(createViewUsingForm(form))
        for (option <- ninoOptions) {
          assertContainsRadioButton(doc, s"nino_hasNino-$option", "nino.hasNino", option, false)
        }
      }


      for (option <- ninoOptions) {
        s"rendered with a value of '$option'" must {
          s"have the '$option' radio button selected" in {
            val doc = asDocument(createViewUsingForm(form.bind(Map("nino.hasNino" -> s"$option"))))
            assertContainsRadioButton(doc, s"nino_hasNino-$option", "nino.hasNino", option, true)

            for (unselectedOption <- ninoOptions.filterNot(o => o == option)) {
              assertContainsRadioButton(doc, s"nino_hasNino-$unselectedOption", "nino.hasNino", unselectedOption, false)
            }
          }
        }
      }
    }
  }
}
