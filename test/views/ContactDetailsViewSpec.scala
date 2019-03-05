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

import forms.ContactDetailsFormProvider
import models.{ContactDetails, Mode, NormalMode, UpdateMode}
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.{ContactDetailsViewModel, Message}
import views.behaviours.QuestionViewBehaviours
import views.html.contactDetails

class ContactDetailsViewSpec extends QuestionViewBehaviours[ContactDetails] {

  val messageKeyPrefix = "contactDetails"

  override val form = new ContactDetailsFormProvider()()

  val viewmodel = ContactDetailsViewModel(
    postCall = Call("GET", "/"),
    title = Message("contactDetails.title"),
    heading = Message("contactDetails.heading"),
    subHeading = None,
    body = Some(Message("contactDetails.lede")),
    psaName = Some("test psa name")
  )

  def createView(mode: Mode = NormalMode): () => HtmlFormat.Appendable = () =>
    contactDetails(frontendAppConfig, form, viewmodel, mode)(fakeRequest, messages)

  def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    contactDetails(frontendAppConfig, form, viewmodel, NormalMode)(fakeRequest, messages)

  "ContactDetails view" must {
    appRunning()

    behave like normalPage(createView(), messageKeyPrefix)

    behave like pageWithReturnLink(createView(UpdateMode), controllers.routes.PsaDetailsController.onPageLoad().url)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.individual.routes.IndividualContactDetailsController.onSubmit(NormalMode).url,
      "emailAddress",
      "phoneNumber"
    )
  }
}
