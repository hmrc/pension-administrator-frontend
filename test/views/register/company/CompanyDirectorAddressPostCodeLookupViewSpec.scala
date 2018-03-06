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

package views.register.company

import play.api.data.Form
import controllers.register.company.routes
import forms.register.company.CompanyDirectorAddressPostCodeLookupFormProvider
import models.{Index, NormalMode}
import play.twirl.api.HtmlFormat
import views.behaviours.StringViewBehaviours
import views.html.register.company.companyDirectorAddressPostCodeLookup

class CompanyDirectorAddressPostCodeLookupViewSpec extends StringViewBehaviours {

  val index = Index(1)
  val directorName = "test name"
  val messageKeyPrefix = "companyDirectorAddressPostCodeLookup"

  val form = new CompanyDirectorAddressPostCodeLookupFormProvider()()

  def createView: () => HtmlFormat.Appendable =
    () => companyDirectorAddressPostCodeLookup(
      frontendAppConfig,
      form,
      NormalMode,
      index,
      directorName)(fakeRequest, messages)

  def createViewUsingForm: Form[String] => HtmlFormat.Appendable =
    (form: Form[String]) => companyDirectorAddressPostCodeLookup(
      frontendAppConfig,
      form,
      NormalMode,
      index,
      directorName)(fakeRequest, messages)

  "CompanyPreviousAddressPostCodeLookup view" must {
    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, messages(directorName))

    behave like stringPage(
      createViewUsingForm,
      messageKeyPrefix,
      controllers.register.company.routes.CompanyDirectorAddressPostCodeLookupController.onSubmit(NormalMode, index).url,
      Some(s"$messageKeyPrefix.postcode.hint"),
      "postcode"
    )

    "display body text" in {
      createView must haveDynamicText(s"$messageKeyPrefix.body")
    }

    "display enter address manually link" ignore {
      createView must haveLink(routes.DirectorAddressController.onPageLoad(NormalMode, index).url, "manual-address-link")
    }
  }
}
