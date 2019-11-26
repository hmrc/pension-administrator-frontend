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

package views.address

import forms.address.PostCodeLookupFormProvider
import models.{Mode, NormalMode, UpdateMode}
import play.api.data.Form
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import viewmodels.address.PostcodeLookupViewModel
import views.behaviours.StringViewBehaviours
import views.html.address.postcodeLookup

class PostcodeLookupViewSpec extends StringViewBehaviours {

  private val messageKeyPrefix = "test"
  val form = new PostCodeLookupFormProvider()()

  private val viewModel = PostcodeLookupViewModel(
    Call("GET", "www.example.com"),
    Call("POST", "www.example.com"),
    "test.title",
    "test.heading",
    "test.enter-postcode",
    Some("test.enter-postcode-link"),
    "test.form-label",
    psaName = Some("test-psa")
  )

  val view: postcodeLookup = app.injector.instanceOf[postcodeLookup]

  private def createView(mode: Mode = NormalMode): () => HtmlFormat.Appendable = () =>
    view(
      form,
      viewModel,
      mode
    )(fakeRequest, messages)

  private def createViewUsingForm: Form[_] => HtmlFormat.Appendable = (form: Form[_]) =>
    view(
      form,
      viewModel,
      NormalMode
    )(fakeRequest, messages)

  "PostcodeLookup view" must {
    appRunning()
    behave like normalPage(createView(), messageKeyPrefix)

    behave like stringPage(createViewUsingForm, messageKeyPrefix, "www.example.com", None, "form-label")

    behave like pageWithSubmitButton(createView())
    behave like pageWithReturnLink(createView(mode = UpdateMode), controllers.routes.PsaDetailsController.onPageLoad().url)
  }
  app.stop()
}
