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

import forms.register.AreYouInUKFormProvider
import models.{CheckMode, Mode, NormalMode}
import play.api.data.Form
import views.behaviours.{ViewBehaviours, YesNoViewBehaviours}
import views.html.register.areYouInUK

class AreYouInUKViewSpec extends ViewBehaviours with YesNoViewBehaviours {

  private val messageKeyPrefix = "areYouInUK"


  val formProvider = new AreYouInUKFormProvider

  val form: Form[Boolean] = formProvider()

  private def createView(mode: Mode = NormalMode) =
    () => areYouInUK(
      frontendAppConfig,
      form,
      mode
    )(fakeRequest, messages)

  private def createViewUsingForm =
    (form: Form[_]) => areYouInUK(
      frontendAppConfig,
      form,
      NormalMode
    )(fakeRequest, messages)

  "Are you in UK view" must {
    behave like normalPage(createView(CheckMode), messageKeyPrefix,
      "check.selectedUkAddress", "check.provideNonUkAddress")

    behave like pageWithBackLink(createView())

    behave like pageWithSubmitButton(createView())

    behave like yesNoPage(createViewUsingForm, messageKeyPrefix, "/", s"$messageKeyPrefix.title")

    "not display dynamic content of CheckMode in NormalMode" in {
      createView() mustNot haveDynamicText("areYouInUK.check.selectedUkAddress")
      createView() mustNot haveDynamicText("areYouInUK.check.provideNonUkAddress")
    }
  }

}