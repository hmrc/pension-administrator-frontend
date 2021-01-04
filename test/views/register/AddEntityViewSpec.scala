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

import forms.register.AddEntityFormProvider
import models._
import models.requests.DataRequest
import org.jsoup.select.Elements
import org.jsoup.Jsoup
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import play.twirl.api.Html
import utils.UserAnswers
import viewmodels.{EntityViewModel, Message, Person}
import views.behaviours.{PeopleListBehaviours, YesNoViewBehaviours}
import views.html.register.addEntity

class AddEntityViewSpec extends YesNoViewBehaviours with PeopleListBehaviours {

  import AddEntityViewSpec._

  private val maxPartners = frontendAppConfig.maxPartners

  private val messageKeyPrefix = "addPartners"
  private val entityType = "partners"
  private val entityTypeSingular = "partner"

  def viewModel(entities: Seq[Person] = Seq.empty) = EntityViewModel(
    postCall = Call("GET", "/"),
    title = Message("addPartners.title"),
    heading = Message("addPartners.heading"),
    entities = entities,
    maxLimit = maxPartners,
    entityType = entityType,
    psaName = Some("test psa")
  )

  val view: addEntity = app.injector.instanceOf[addEntity]

  private def createView(entities: Seq[Person] = Nil, mode: Mode = NormalMode): () => Html = () =>
    view(form, viewModel(entities), mode)(request, messages)

  private def createViewUsingForm(entities: Seq[Person] = Nil, mode: Mode = NormalMode): Form[_] => Html = (form: Form[_]) =>
    view(form, viewModel(entities), mode)(request, messages)

  val form = new AddEntityFormProvider()()

  "AddPartners view" must {

    behave like normalPage(createView(), messageKeyPrefix)

    behave like pageWithReturnLink(createView(mode = UpdateMode), controllers.routes.PsaDetailsController.onPageLoad().url)

    behave like yesNoPageWithoutHint(
      createViewUsingForm(Seq(johnDoe)),
      messageKeyPrefix,
      viewModel(Seq(johnDoe)).postCall.url,
      messages("addEntity.addYesNo", entityTypeSingular)
    )

    val partners: Seq[Person] = Seq(johnDoe, joeBloggs)

    behave like peopleList(createView(),
      createView(partners),
      createView(Seq(johnDoe, joeBloggs.copy(isComplete = false, isNew = true)), UpdateMode),
      partners
    )

    "not show the yes no inputs if there are no partners" in {
      val doc = asDocument(createViewUsingForm()(form))
      doc.select("legend > span").size() mustBe 0
    }

    "not show the yes no inputs if there are 10 or more partners" in {
      val doc = asDocument(createViewUsingForm(Seq.fill(maxPartners)(johnDoe))(form))
      doc.select("legend > span").size() mustBe 0
    }

    "not show the indented text if there 2 or more partners" in {
      val doc = asDocument(createViewUsingForm(Seq.fill(2)(johnDoe))(form))
      assertNotRenderedById(doc, "too-few-partners-hint")
    }

    "show the indented text if there are less than 2 partners" in {
      val doc = asDocument(createViewUsingForm(Seq.fill(1)(johnDoe))(form))
      assertRenderedById(doc, "too-few-partners-hint")
    }

    "show the Continue button when there are partners" in {
      val doc = asDocument(createViewUsingForm(Seq(johnDoe))(form))
      val submit = doc.select("button#submit")
      submit.size() mustBe 1
      submit.first().text() mustBe messages("site.continue")
    }

    "show the Add a Partner button when there are zero partners" in {
      val doc = asDocument(createViewUsingForm()(form))
      val submit = doc.select("button#submit")
      submit.size() mustBe 1
      submit.first().text() mustBe messages("addEntity.addAnEntity", entityTypeSingular)
    }

    "show the add partner hint when there are zero partners" in {
      createView() must haveDynamicText("addEntity.addAnEntity.hint", entityType)
    }

    "have aria label for edit and remove links" in {
      val doc = asDocument(createViewUsingForm(Seq(johnDoe))(form))
      doc.select(s"a[aria-label='Change ${johnDoe.name}']").size() mustBe 1
      doc.select(s"a[aria-label='Remove ${johnDoe.name}']").size() mustBe 1
    }

    "show the maximum number of partners message when there are 10 partners" in {
      val view = createView(Seq.fill(maxPartners)(johnDoe))
      view must haveDynamicText("addEntity.atMaximum", entityType)
      view must haveDynamicText("addEntity.tellUsIfYouHaveMore")
    }

    "not show the remove link in UpdateMode if only one partner is present" in {
      createView(Seq(johnUpdateMode), UpdateMode) mustNot haveLink(
        controllers.register.partnership.partners.routes.ConfirmDeletePartnerController.onPageLoad(0, UpdateMode).url, "person-0-delete")
    }

    "show the edit link always in NormalMode" in {
      val view = createView(Seq(johnDoe, joeBloggs), NormalMode)
      view must haveLink(
        controllers.register.partnership.partners.routes.PartnerNameController.onPageLoad(NormalMode, 0).url, "person-0-edit")
      view must haveLink(
        controllers.register.partnership.partners.routes.PartnerNameController.onPageLoad(NormalMode, 1).url, "person-1-edit")
    }

    "show the edit link in UpdateMode only for newly added partners" in {
      val view = createView(Seq(johnUpdateMode.copy(isComplete = false), joeUpdateMode), UpdateMode)
      view mustNot haveLink(
        controllers.register.partnership.partners.routes.PartnerNameController.onPageLoad(UpdateMode, 0).url, "person-0-edit")
      view must haveLink(
        controllers.register.partnership.partners.routes.PartnerNameController.onPageLoad(UpdateMode, 1).url, "person-1-edit")
    }

    "disable submission in Normal Mode when any partner is incomplete" in {
      val view = createView(Seq(johnDoe, joeBloggs.copy(isComplete = false)), NormalMode)
      Jsoup.parse(view().toString()).getElementById("submit").hasAttr("disabled") mustBe true
    }

    "not disable submission in UpdateMode when existing partner is incomplete" in {
      val view = createView(Seq(johnUpdateMode.copy(isComplete = false), joeUpdateMode), UpdateMode)
      Jsoup.parse(view().toString()).getElementById("submit").hasAttr("disabled") mustBe false
    }

    "disable submission in UpdateMode only when newly added partner is incomplete" in {
      val view = createView(Seq(johnUpdateMode, joeUpdateMode.copy(isComplete = false)), UpdateMode)
      Jsoup.parse(view().toString()).getElementById("submit").hasAttr("disabled") mustBe true
    }
  }

}

object AddEntityViewSpec {

  val request: DataRequest[AnyContent] = DataRequest(
    FakeRequest(),
    "cacheId",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
    UserAnswers(Json.obj())
  )

  private def deleteLink(index: Int, mode: Mode = NormalMode) =
    controllers.register.partnership.partners.routes.ConfirmDeletePartnerController.onPageLoad(index, mode).url

  private def editLink(index: Int, mode: Mode = NormalMode) =
    controllers.register.partnership.partners.routes.PartnerNameController.onPageLoad(mode, index).url

  // scalastyle:off magic.number
  private val johnDoe = Person(0, "John Doe", deleteLink(0), editLink(0), isDeleted = false, isComplete = true)
  private val joeBloggs = Person(1, "Joe Bloggs", deleteLink(1), editLink(1), isDeleted = false, isComplete = true)


  private val johnUpdateMode = johnDoe.copy(deleteLink = deleteLink(0, UpdateMode), editLink = editLink(0, UpdateMode))
  private val joeUpdateMode = joeBloggs.copy(deleteLink = deleteLink(1, UpdateMode), editLink = editLink(1, UpdateMode), isNew = true)

}
