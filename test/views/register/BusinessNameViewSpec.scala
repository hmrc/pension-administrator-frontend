/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.data.Form
import play.twirl.api.Html
import viewmodels.Message
import views.behaviours.QuestionViewBehaviours
import views.html.register.businessName

class BusinessNameViewSpec extends QuestionViewBehaviours[String] {

  private val messageKeyPrefix = "businessName"
  private val businessType = "limited company"
  private val href = controllers.routes.IndexController.onPageLoad()

  val form = new BusinessNameFormProvider()()

  val view: businessName = app.injector.instanceOf[businessName]

  private def createView: () => Html = () => view(form, businessType, href)(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => Html = (form: Form[_]) => view(form, businessType, href)(fakeRequest, messages)

  "Company Name view" must {

    "display the correct browser title" in {
      val doc = asDocument(createView())
      assertEqualsMessage(doc, "title", messages(s"$messageKeyPrefix.title", businessType)  + " - " + messages("pension.scheme.administrator.title"))
    }

    "display the correct page header" in {
      val doc = asDocument(createView())
      assertPageTitleEqualsMessage(doc, Message(s"$messageKeyPrefix.heading", businessType))
    }

    behave like pageWithSubmitButton(createView)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      href.url,
      "value")

  }

}
