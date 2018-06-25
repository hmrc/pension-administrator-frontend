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
import views.behaviours.{PeopleListBehaviours, YesNoViewBehaviours}
import models.NormalMode
import models.register.company.directors.DirectorDetails
import views.html.register.company.addCompanyDirectors

class AddCompanyDirectorsViewSpec  extends YesNoViewBehaviours with PeopleListBehaviours {

  private val messageKeyPrefix = "addCompanyDirectors"

  val form = new AddCompanyDirectorsFormProvider()()

  private def createView(directors: Seq[DirectorDetails] = Nil)
      = () => addCompanyDirectors(frontendAppConfig, form, NormalMode, directors.map((_, true)), false)(fakeRequest, messages)

  private def createViewUsingForm(directors: Seq[DirectorDetails] = Nil)
      = (form: Form[_]) => addCompanyDirectors(frontendAppConfig, form, NormalMode, directors.map((_, true)), false)(fakeRequest, messages)

  // scalastyle:off magic.number
  private val johnDoe = DirectorDetails("John", None, "Doe", LocalDate.of(1862, 6, 9))
  private val joeBloggs = DirectorDetails("Joe", None, "Bloggs", LocalDate.of(1969, 7, 16))
  // scalastyle:on magic.number

  private val maxDirectors = frontendAppConfig.maxDirectors

  "AddCompanyDirectors view" must {

    behave like normalPage(createView(), messageKeyPrefix)

    behave like pageWithBackLink(createView())

    behave like pageWithSecondaryHeader(createView(), messages("site.secondaryHeader"))

    behave like yesNoPage(
      createViewUsingForm(Seq(johnDoe)),
      messageKeyPrefix,
      routes.AddCompanyDirectorsController.onSubmit(NormalMode).url,
      "addYesNo",
      Some("addADirector.hint")
    )

    val directors: Seq[DirectorDetails] = Seq(johnDoe, joeBloggs)

    behave like peopleList(createView(), createView(directors), directors)

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
      createView() must haveDynamicText("addCompanyDirectors.addADirector.hint")
    }

    "show the maximum number of directors message when there are 10 directors" in {
      val view = createView(Seq.fill(maxDirectors)(johnDoe))
      view must haveDynamicText("addCompanyDirectors.atMaximum")
      view must haveDynamicText("addCompanyDirectors.tellUsIfYouHaveMore")
    }

  }

}
