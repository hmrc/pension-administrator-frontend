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

package views.register

import base.SpecBase
import forms.register.RegisterAsBusinessFormProvider
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.register.registerAsBusiness

class RegisterAsBusinessViewSpec extends YesNoViewBehaviours {

  import RegisterAsBusinessViewSpec._

  override val form: Form[Boolean] = new RegisterAsBusinessFormProvider().apply()

  "Register as Business view" must {

    behave like normalPage(createView(this, form), messageKeyPrefix)

    behave like yesNoPage(
      createViewUsingForm(this),
      messageKeyPrefix,
      controllers.register.routes.RegisterAsBusinessController.onSubmit().url,
      s"$messageKeyPrefix.heading"
    )

    "display the correct label for no" in {
      createView(this, form) must haveLabel("value-no", messages("registerAsBusiness.no.label"))
    }

    behave like pageWithSubmitButton(createView(this, form))

  }

}

object RegisterAsBusinessViewSpec {

  val messageKeyPrefix: String = "registerAsBusiness"

  def createView(base: SpecBase, form: Form[Boolean]): () => HtmlFormat.Appendable =
    () =>
      registerAsBusiness(
        base.frontendAppConfig,
        form
      )(base.fakeRequest, base.messages)

  def createViewUsingForm(base: SpecBase): Form[Boolean] => HtmlFormat.Appendable =
    form =>
      createView(base, form).apply()

}
