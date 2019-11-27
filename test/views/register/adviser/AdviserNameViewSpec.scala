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

import forms.register.adviser.AdviserNameFormProvider
import models.{CheckMode, NormalMode}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.QuestionViewBehaviours
import views.html.register.adviser.adviserName

class AdviserNameViewSpec extends QuestionViewBehaviours[String] {

  val messageKeyPrefix = "adviserName"

  override val form: Form[String] = new AdviserNameFormProvider().apply()

  val view: adviserName = app.injector.instanceOf[adviserName]

  def createView: () => HtmlFormat.Appendable = () => view(form, NormalMode, Some("Mark Wright"))(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) => view(form, CheckMode, Some("Mark Wright"))(fakeRequest, messages)

  "Adviser Details view" must {

    behave like normalPage(createView, messageKeyPrefix, "p1")

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.adviser.routes.AdviserNameController.onSubmit(NormalMode).url,
      "adviserName"
    )
  }

}
