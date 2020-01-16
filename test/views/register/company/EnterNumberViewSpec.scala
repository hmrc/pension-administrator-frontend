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

package views.register.company

import controllers.register.company.routes._
import forms.register.company.CompanyRegistrationNumberFormProvider
import models.{Mode, NormalMode}
import play.api.data.Form
import play.twirl.api.Html
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.behaviours.StringViewBehaviours
import views.html.register.company.enterNumber

class EnterNumberViewSpec extends StringViewBehaviours {

  private val companyName = "name"

  private val messageKeyPrefix = "companyRegistrationNumber"

  val form = new CompanyRegistrationNumberFormProvider()()

  private def viewModel(mode: Mode) = CommonFormWithHintViewModel(
    postCall = CompanyRegistrationNumberController.onSubmit(NormalMode),
    title = Message(s"$messageKeyPrefix.heading"),
    heading = Message(s"$messageKeyPrefix.heading", companyName),
    mode = mode,
    hint = Some(Message(s"$messageKeyPrefix.hint")),
    entityName = companyName

  )

  val view: enterNumber = app.injector.instanceOf[enterNumber]

  private def createView: () => Html = () => view(form, viewModel(NormalMode))(fakeRequest, messages)

  private def createViewUsingForm: Form[String] => Html = (form: Form[String]) => view(form, viewModel(NormalMode))(fakeRequest, messages)

  "CompanyRegistrationNumber view" must {
    normalPageWithNoPageTitleCheck(createView, messageKeyPrefix)

    "behave like a normal page" when {
      "rendered" must {
        "display the correct page title" in {
          val doc = asDocument(createView())
          assertPageTitleEqualsMessage(doc, s"$messageKeyPrefix.heading", companyName)
        }
      }
    }

    behave like stringPage(
      createView = createViewUsingForm,
      messageKeyPrefix = messageKeyPrefix,
      expectedFormAction = controllers.register.company.routes.CompanyRegistrationNumberController.onSubmit(NormalMode).url,
      expectedHintKey = Some(s"$messageKeyPrefix.hint"),
      labelArgs = Some(companyName)
    )
  }

}
