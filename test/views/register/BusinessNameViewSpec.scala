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

import forms.BusinessNameFormProvider
import models.NormalMode
import play.api.data.Form
import viewmodels.Message
import views.behaviours.QuestionViewBehaviours
import views.html.register.businessName

class BusinessNameViewSpec extends QuestionViewBehaviours[String] {

  private val messageKeyPrefix = "businessName"
  private val businessType = "limited company"

  val form = new BusinessNameFormProvider()()

  private def createView = () => businessName(frontendAppConfig, form, NormalMode, businessType)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) => businessName(frontendAppConfig, form, NormalMode, businessType)(fakeRequest, messages)

  "Company Name view" must {

    "display the correct browser title" in {
      val doc = asDocument(createView())
      assertEqualsMessage(doc, "title", messagesApi(s"$messageKeyPrefix.title", businessType)  + " - " + messagesApi("pension.scheme.administrator.title"))
    }

    "display the correct page header" in {
      val doc = asDocument(createView())
      assertPageTitleEqualsMessage(doc, Message(s"$messageKeyPrefix.heading", businessType))
    }

    behave like pageWithSubmitButton(createView)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.routes.BusinessNameController.onSubmit(NormalMode).url,
      "value")

  }

}
