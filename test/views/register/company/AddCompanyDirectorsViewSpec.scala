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

import controllers.register.company.routes
import forms.register.company.AddCompanyDirectorsFormProvider
import models.requests.DataRequest
import models._
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers
import viewmodels.Person
import views.behaviours.{PeopleListBehaviours, YesNoViewBehaviours}
import views.html.register.company.addCompanyDirectors

class AddCompanyDirectorsViewSpec extends YesNoViewBehaviours with PeopleListBehaviours {

  import AddCompanyDirectorsViewSpec._

  private val maxDirectors = frontendAppConfig.maxDirectors

  private val messageKeyPrefix = "addCompanyDirectors"

  private def createView(directors: Seq[Person] = Nil, mode: Mode = NormalMode)
  = () => addCompanyDirectors(frontendAppConfig, form, mode, directors, Some("test psa"))(request, messages)

  private def createViewUsingForm(directors: Seq[Person] = Nil)
  = (form: Form[_]) => addCompanyDirectors(frontendAppConfig, form, NormalMode, directors, None)(request, messages)

  val form = new AddCompanyDirectorsFormProvider()()

  "AddCompanyDirectors view" must {

    behave like normalPage(createView(), messageKeyPrefix)

    behave like yesNoPage(
      createViewUsingForm(Seq(johnDoe)),
      messageKeyPrefix,
      routes.AddCompanyDirectorsController.onSubmit(NormalMode).url,
      s"$messageKeyPrefix.addYesNo",
      Some(s"$messageKeyPrefix.addADirector.hint")
    )

    val directors: Seq[Person] = Seq(johnDoe, joeBloggs)

    behave like peopleList(createView(),
      createView(directors),
      createView(Seq(johnDoe, joeBloggs.copy(isComplete = false)), UpdateMode),
      directors)

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

    behave like pageWithReturnLink(createView(mode = UpdateMode), controllers.routes.PsaDetailsController.onPageLoad().url)
  }
}

object AddCompanyDirectorsViewSpec {

  val request: DataRequest[AnyContent] = DataRequest(
    FakeRequest(),
    "cacheId",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None),
    UserAnswers(Json.obj())
  )

  private def deleteLink(index: Int) = controllers.register.company.directors.routes.ConfirmDeleteDirectorController.onPageLoad(NormalMode, index).url

  private def editLink(index: Int) = controllers.register.company.directors.routes.DirectorDetailsController.onPageLoad(NormalMode, index).url

  // scalastyle:off magic.number
  private val johnDoe = Person(0, "John Doe", deleteLink(0), editLink(0), isDeleted = false, isComplete = true)
  private val joeBloggs = Person(1, "Joe Bloggs", deleteLink(1), editLink(1), isDeleted = false, isComplete = true)
  // scalastyle:on magic.number

}
