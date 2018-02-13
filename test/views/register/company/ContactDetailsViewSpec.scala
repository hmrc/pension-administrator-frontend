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

package views.register.company

import forms.register.company.ContactDetailsFormProvider
import models.NormalMode
import models.register.company.ContactDetails
import org.jsoup.Jsoup
import play.api.data.Form
import views.behaviours.QuestionViewBehaviours
import views.html.register.company.contactDetails

class ContactDetailsViewSpec extends QuestionViewBehaviours[ContactDetails] {

  val messageKeyPrefix = "contactDetails"

  override val form = new ContactDetailsFormProvider()()

  def createView = () => contactDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[_]) => contactDetails(frontendAppConfig, form, NormalMode)(fakeRequest, messages)

  "ContactDetails view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, messages("site.secondaryHeader") )

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.company.routes.ContactDetailsController.onSubmit(NormalMode).url,
      "email",
      "phone"
    )

    "display correct body text" in {
      assertContainsText(Jsoup.parse(createView().toString()), messages(s"$messageKeyPrefix.body"))
    }
  }
}
