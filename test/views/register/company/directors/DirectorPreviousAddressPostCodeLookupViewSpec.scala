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

package views.register.company.directors

import controllers.register.company.directors.routes
import forms.register.company.directors.DirectorPreviousAddressPostCodeLookupFormProvider
import models.{Index, NormalMode}
import play.api.data.Form
import views.behaviours.StringViewBehaviours
import views.html.register.company.directors.directorPreviousAddressPostCodeLookup

class DirectorPreviousAddressPostCodeLookupViewSpec extends StringViewBehaviours {

  val messageKeyPrefix = "directorPreviousAddressPostCodeLookup"

  val form = new DirectorPreviousAddressPostCodeLookupFormProvider()()
  val index = Index(0)
  val directorName = "test first name test middle name test last name"

  def createView = () => directorPreviousAddressPostCodeLookup(frontendAppConfig, form,
    NormalMode, index, directorName)(fakeRequest, messages)

  def createViewUsingForm = (form: Form[String]) => directorPreviousAddressPostCodeLookup(frontendAppConfig,
    form, NormalMode, index, directorName)(fakeRequest, messages)

  "DirectorPreviousAddressPostCodeLookup view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, directorName)

    behave like stringPage(createViewUsingForm, messageKeyPrefix,
      controllers.register.company.directors.routes.DirectorPreviousAddressPostCodeLookupController.onSubmit(NormalMode, index).url,
      Some(s"$messageKeyPrefix.input.hint"), "input.text")
  }

  "display body text" in {
    createView must haveDynamicText(s"$messageKeyPrefix.text")
  }

  "display enter address manually link" in {
    createView must haveLink(routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(NormalMode, index).url, "manual-address-link")
  }
}
