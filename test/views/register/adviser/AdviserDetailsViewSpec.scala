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

package views.register.adviser

import forms.register.adviser.AdviserDetailsFormProvider
import models.{Mode, NormalMode, UpdateMode}
import models.register.adviser.AdviserDetails
import play.api.data.Form
import views.behaviours.QuestionViewBehaviours
import views.html.register.adviser.adviserDetails

class AdviserDetailsViewSpec extends QuestionViewBehaviours[AdviserDetails] {

  val messageKeyPrefix = "adviserDetails"
  val psaName = "test psa"

  override val form = new AdviserDetailsFormProvider()()

  def createView(mode: Mode = NormalMode) = () => adviserDetails(frontendAppConfig, form, mode, Some(psaName))(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) =>
    adviserDetails(frontendAppConfig, form, NormalMode, Some(psaName))(fakeRequest, messages)

  "AdviserDetails view" must {
    appRunning()

    behave like normalPage(createView(), messageKeyPrefix)

    behave like pageWithReturnLink(createView(UpdateMode), controllers.routes.PsaDetailsController.onPageLoad().url)

    behave like pageWithTextFields(
      createViewUsingForm, messageKeyPrefix, controllers.register.adviser.routes.AdviserDetailsController.onSubmit(NormalMode).url, "name", "email", "phone")
  }
}
