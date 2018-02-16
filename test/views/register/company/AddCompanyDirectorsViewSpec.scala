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

import java.time.LocalDate

import play.api.data.Form
import controllers.register.company.routes
import forms.register.company.AddCompanyDirectorsFormProvider
import views.behaviours.YesNoViewBehaviours
import models.NormalMode
import models.register.company.CompanyDirector
import views.html.register.company.addCompanyDirectors

class AddCompanyDirectorsViewSpec extends YesNoViewBehaviours {

  val messageKeyPrefix = "addCompanyDirectors"

  val form = new AddCompanyDirectorsFormProvider()()

  private def createView = () => addCompanyDirectors(frontendAppConfig, form, NormalMode, Nil)(fakeRequest, messages)

  private def createViewUsingForm(directors: Seq[CompanyDirector] = Nil)
      = (form: Form[_]) => addCompanyDirectors(frontendAppConfig, form, NormalMode, directors)(fakeRequest, messages)

  private val johnDoe = CompanyDirector("John", "Doe", LocalDate.of(1862, 6, 9))
  private val maxDirectors = frontendAppConfig.maxDirectors

  "AddCompanyDirectors view" must {

    behave like normalPage(createView, messageKeyPrefix)

    behave like pageWithBackLink(createView)

    behave like pageWithSecondaryHeader(createView, messages("site.secondaryHeader"))

    behave like yesNoPage(
      createViewUsingForm(Seq(johnDoe)),
      messageKeyPrefix,
      routes.AddCompanyDirectorsController.onSubmit(NormalMode).url,
      "addYesNo",
      Some("addADirector.hint")
    )

    "not show the yes no inputs if there are no directors" in {
      val doc = asDocument(createViewUsingForm()(form))
      doc.select("legend > span").size() mustBe 0
    }

    "not show the yes no inputs if there are 10 or more directors" in {
      val doc = asDocument(createViewUsingForm(Seq.fill(maxDirectors)(johnDoe))(form))
      doc.select("legend > span").size() mustBe 0
    }

    "show the Continue button when there are directors" in {
      val doc = asDocument(createViewUsingForm(Seq(johnDoe))(form))
      val submit = doc.select("button#submit")
      submit.size() mustBe 1
      submit.first().text() mustBe messages("site.continue")
    }

    "show the Add a Director button when there are zero directors" in {
      val doc = asDocument(createViewUsingForm()(form))
      val submit = doc.select("button#submit")
      submit.size() mustBe 1
      submit.first().text() mustBe messages("addCompanyDirectors.addADirector")
    }

    "show the add director hint when there are zero directors" in {
      //createView must haveDynamicText("addCompanyDirectors.addADirector.hint")
    }

    // Maximum number of directors
    "show the maximum number of directors message when 10 directors" in {

    }

    // Directors list

    // Delete link

    // Edit link

  }

}
