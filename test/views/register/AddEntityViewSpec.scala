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

package views.register

import controllers.register.partnership.routes
import forms.register.AddEntityFormProvider
import models.requests.DataRequest
import models.{CheckMode, NormalMode, PSAUser, UserType}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import utils.UserAnswers
import viewmodels.{EntityViewModel, Message, Person}
import views.behaviours.{PeopleListBehaviours, YesNoViewBehaviours}
import views.html.register.addEntity

class AddEntityViewSpec extends YesNoViewBehaviours with PeopleListBehaviours {

  import AddEntityViewSpec._

  private val maxPartners = frontendAppConfig.maxPartners

  private val messageKeyPrefix = "addPartners"
  private val entityType = "partners"
  private val entityTypeSinglular = "partner"

  def viewmodel(entities: Seq[Person] = Seq.empty) = EntityViewModel(
    postCall = Call("GET", "/"),
    title = Message("addPartners.title"),
    heading = Message("addPartners.heading"),
    entities = entities,
    maxLimit = maxPartners,
    entityType = entityType,
    subHeading = None
  )

  private def createView(entities: Seq[Person] = Nil)
  = () => addEntity(frontendAppConfig, form, viewmodel(entities))(request, messages)

  private def createViewUsingForm(entities: Seq[Person] = Nil)
  = (form: Form[_]) => addEntity(frontendAppConfig, form, viewmodel(entities))(request, messages)

  val form = new AddEntityFormProvider()()

  "AddPartners view" must {

    behave like normalPage(createView(), messageKeyPrefix)

    behave like pageWithBackLink(createView())

    behave like yesNoPage(
      createViewUsingForm(Seq(johnDoe)),
      messageKeyPrefix,
      routes.AddPartnerController.onSubmit(NormalMode).url,
      Message("addEntity.addYesNo", entityTypeSinglular).resolve,
      Some(Message("addEntity.addAnEntity.hint", entityType).resolve)
    )

    val partners: Seq[Person] = Seq(johnDoe, joeBloggs)

    behave like peopleList(createView(), createView(partners), partners)

    "not show the yes no inputs if there are no partners" in {
      val doc = asDocument(createViewUsingForm()(form))
      doc.select("legend > span").size() mustBe 0
    }

    "not show the yes no inputs if there are 10 or more partners" in {
      val doc = asDocument(createViewUsingForm(Seq.fill(maxPartners)(johnDoe))(form))
      doc.select("legend > span").size() mustBe 0
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
      submit.first().text() mustBe Message("addEntity.addAnEntity", entityTypeSinglular).resolve
    }

    "show the add partner hint when there are zero partners" in {
      createView() must haveDynamicText("addEntity.addAnEntity.hint", entityType)
    }

    "show the maximum number of partners message when there are 10 partners" in {
      val view = createView(Seq.fill(maxPartners)(johnDoe))
      view must haveDynamicText("addEntity.atMaximum", entityType)
      view must haveDynamicText("addEntity.tellUsIfYouHaveMore")
    }

  }

}

object AddEntityViewSpec {

  val request: DataRequest[AnyContent] = DataRequest(
    FakeRequest(),
    "cacheId",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None),
    UserAnswers(Json.obj())
  )

  private def deleteLink(index: Int) = controllers.register.partnership.partners.routes.PartnerDetailsController.onPageLoad(NormalMode, index).url

  private def editLink(index: Int) = controllers.register.partnership.partners.routes.PartnerDetailsController.onPageLoad(NormalMode, index).url

  // scalastyle:off magic.number
  private val johnDoe = Person(0, "John Doe", deleteLink(0), editLink(0), isDeleted = false, isComplete = true)
  private val joeBloggs = Person(1, "Joe Bloggs", deleteLink(1), editLink(1), isDeleted = false, isComplete = true)
  // scalastyle:on magic.number

}
