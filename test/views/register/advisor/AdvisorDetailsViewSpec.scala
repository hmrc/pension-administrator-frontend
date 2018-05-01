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

package views.register.advisor

import play.api.data.Form
import controllers.register.advisor.routes
import forms.register.advisor.AdvisorDetailsFormProvider
import models.NormalMode
import models.register.advisor.AdvisorDetails
import views.behaviours.QuestionViewBehaviours
import views.html.register.advisor.advisorDetails

class AdvisorDetailsViewSpec extends QuestionViewBehaviours[AdvisorDetails] {

  val messageKeyPrefix = "advisorDetails"

  override val form = new AdvisorDetailsFormProvider()()

  def createView = () => advisorDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => advisorDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "AdvisorDetails view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, messages("advisorDetails.secondary.heading"))

    behave like pageWithTextFields(
      createViewUsingForm, messageKeyPrefix, controllers.register.advisor.routes.AdvisorDetailsController.onSubmit(NormalMode).url, "name", "email", "phoneNumber")
  }
}
