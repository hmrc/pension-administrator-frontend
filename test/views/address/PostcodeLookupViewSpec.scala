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

package views.address

import forms.address.PostCodeLookupFormProvider
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.address.PostcodeLookupViewModel
import views.behaviours.StringViewBehaviours
import views.html.address.postcodeLookup

class PostcodeLookupViewSpec extends StringViewBehaviours {

  private val messageKeyPrifix = "test"
  val form = new PostCodeLookupFormProvider()()

  private val viewModel = PostcodeLookupViewModel(
    Call("GET", "www.example.com"),
    Call("POST", "www.example.com"),
    "test.title",
    "test.heading",
    Some("test.sub-heading"),
    "test.hint",
    "test.enter-postcode",
    "test.form-label"
  )

  private def createView: () => HtmlFormat.Appendable =
    () => postcodeLookup(
      frontendAppConfig,
      form,
      viewModel
    )(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => HtmlFormat.Appendable =
    (form: Form[_]) => postcodeLookup(
      frontendAppConfig,
      form,
      viewModel
    )(fakeRequest, messages)

  "PostcodeLookup view" must {
    behave like normalPage(createView, messageKeyPrifix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, viewModel.subHeading.value)

    "render the hint" in {
      val doc = asDocument(createView())
      assertContainsText(doc, viewModel.hint.resolve)
    }

    behave like stringPage(createViewUsingForm, messageKeyPrifix, "", Some("test.form-hint"), "form-label")

    behave like pageWithSubmitButton(createView)
  }

}
