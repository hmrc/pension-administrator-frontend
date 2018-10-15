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

package views

import forms.CompanyNameFormProvider
import play.api.data.Form
import play.api.mvc.Call
import viewmodels.{Message, CompanyNameViewModel}
import views.behaviours.StringViewBehaviours
import views.html.companyName

class CompanyNameViewSpec extends StringViewBehaviours {

  private val messageKeyPrefix = "companyName"

  val form = new CompanyNameFormProvider()()

  private lazy val viewModel =
    CompanyNameViewModel(
      title = "companyName.title",
      heading = Message("companyName.heading"),
      postCall = Call("POST", "http://www.test.com")
    )

  private def createView = () => companyName(frontendAppConfig, form, viewModel)(fakeRequest, messages)

  private def createViewUsingForm = (form: Form[_]) => companyName(frontendAppConfig, form, viewModel)(fakeRequest, messages)

  "Company Name view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSubmitButton(createView)

    behave like stringPage(
      createViewUsingForm,
      messageKeyPrefix,
      "",
      None
    )
  }

}
