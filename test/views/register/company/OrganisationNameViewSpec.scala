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

package views.register.company

import forms.BusinessNameFormProvider
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.Html
import viewmodels.{Message, OrganisationNameViewModel}
import views.behaviours.QuestionViewBehaviours
import views.html.organisationName

class OrganisationNameViewSpec extends QuestionViewBehaviours[String] {

  private val messageKeyPrefix = "companyNameNonUk"

  private lazy val viewModel =
    OrganisationNameViewModel(
      title = "companyNameNonUk.title",
      heading = Message("companyNameNonUk.heading"),
      postCall = Call("POST", "http://www.test.com")
    )

  val form = new BusinessNameFormProvider()()

  val view: organisationName = app.injector.instanceOf[organisationName]


  private def createView: () => Html = () => view(form, viewModel)(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => Html = (form: Form[_]) => view(form, viewModel)(fakeRequest, messages)

  "Company Name view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithSubmitButton(createView)

    behave like pageWithTextFields(
      createViewUsingForm,
      messageKeyPrefix,
      "",
      "value")

    behave like pageWithLabel(createViewUsingForm, "value", messages("companyNameNonUk.heading"))


  }
  app.stop()
}
