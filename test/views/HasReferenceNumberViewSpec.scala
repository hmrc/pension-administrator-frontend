/*
 * Copyright 2020 HM Revenue & Customs
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

import forms.mappings.Mappings
import javax.inject.Inject
import models.{Mode, NormalMode, UpdateMode}
import play.api.data.Form
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.behaviours.YesNoViewBehaviours
import views.html.hasReferenceNumber

class HasReferenceNumberViewSpec extends YesNoViewBehaviours {
  private val messageKeyPrefix = "testPrefix"

  private def pageTitle = Message(s"$messageKeyPrefix.title")

  private val companyName = "Test Company Name"

  private def viewModel(mode: Mode) = CommonFormWithHintViewModel(
    postCall = controllers.register.company.routes.HasCompanyCRNController.onSubmit(NormalMode),
    title = pageTitle,
    heading = Message(s"$messageKeyPrefix.heading"),
    hint = Some(Message(s"$messageKeyPrefix.hint")),
    mode = mode,
    entityName = companyName
  )

  private class HasXFormProvider @Inject() extends Mappings {

    def apply(errorKey: String, name: String)(implicit messages: Messages): Form[Boolean] =
      Form(
        "value" -> boolean(Message(errorKey, name).resolve)
      )
  }

  val form = new HasXFormProvider()("required", "name")

  val view: hasReferenceNumber = app.injector.instanceOf[hasReferenceNumber]

  private def createView(mode: Mode = NormalMode): () => HtmlFormat.Appendable = () =>
    view(form, viewModel(mode))(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(form, viewModel(NormalMode))(fakeRequest, messages)

  "HasReferenceNumber view" must {

    behave like normalPage(createView(), messageKeyPrefix)

    behave like yesNoPage(
      createView = createViewUsingForm,
      messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = viewModel(NormalMode).postCall.url,
      messageKey = s"$messageKeyPrefix.heading"
    )

    behave like pageWithSubmitButton(createView())

    behave like pageWithReturnLink(createView(UpdateMode), controllers.routes.PsaDetailsController.onPageLoad().url)

  }

}
