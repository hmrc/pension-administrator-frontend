/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers.register.administratorPartnership

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.AddEntityFormProvider
import identifiers.register.company.AddCompanyDirectorsId
import identifiers.register.partnership.AddPartnersId
import identifiers.register.partnership.partners.PartnerNameId
import models._
import models.requests.DataRequest
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.AnyContent
import utils.FakeRequest
import play.api.test.Helpers._
import utils.testhelpers.DataCompletionBuilder.DataCompletionUserAnswerOps
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{EntityViewModel, Message, Person}
import views.html.register.addToListEntity

class AddPartnerControllerSpec extends ControllerSpecBase {

  import AddPartnerControllerSpec._

  override def fakeRequest: FakeRequest[AnyContent] = FakeRequest("", "/")
  "AddPartner Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "not populate the view on a GET when the question has previously been answered" in {
      val partners = Seq(Person(0, "first0 last0", deleteLink(0), editLink(0), isDeleted = false, isComplete = true))
      val getRelevantData = UserAnswers().completePartner(index = 0).dataRetrievalAction

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)
      contentAsString(result) mustBe viewAsString(form, partners)
    }

    "redirect to the next page when no partners exist and the user submits" in {
      val result = controller().onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the next page when less than maximum partners exist and valid data is submitted" in {
      val getRelevantData = dataRetrievalAction(Seq.fill(maxPartners - 1)(johnDoe): _*)

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller(getRelevantData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when less than maximum partners exist and invalid data is submitted" in {
      val getRelevantData = UserAnswers().completePartner(index = 0).completePartner(1).dataRetrievalAction
      val partnerAsPerson = Seq(person(0), person(1))

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(getRelevantData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm, partnerAsPerson)
    }

    "not save the answer when partners exist and valid data is submitted" in {
      val getRelevantData = dataRetrievalAction(johnDoe)

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      controller(getRelevantData).onSubmit(NormalMode)(postRequest)
      FakeUserAnswersCacheConnector.verifyNot(AddCompanyDirectorsId)
    }

    "set the user answer when partners exist and valid data is submitted" in {
      val getRelevantData = dataRetrievalAction(johnDoe)
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val navigator = fakeNavigator()

      val result = controller(getRelevantData, navigator).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      navigator.lastUserAnswers.value.get(AddPartnersId).value mustBe true
    }

    "redirect to the next page when maximum active partners exist and the user submits" in {
      val partnerDetails = Seq.fill(maxPartners)(johnDoe) ++ Seq(joeBloggs.copy(isDeleted = true))

      val getRelevantData = dataRetrievalAction(partnerDetails: _*)

      val result = controller(getRelevantData).onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
    "populate the view with partners when they exist" in {
      val partnersAsPerson = Seq(person(0), person(1))
      val getRelevantData = UserAnswers().completePartner(index = 0).completePartner(index = 1).dataRetrievalAction
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form, partnersAsPerson)
    }

    "exclude the deleted partners from the list" in {
      val getRelevantData = UserAnswers().completePartner(0).completePartner(1, isDeleted = true).dataRetrievalAction
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form, Seq(person(0)))
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }

}

object AddPartnerControllerSpec extends AddPartnerControllerSpec {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad

  private val formProvider = new AddEntityFormProvider()
  private val form = formProvider()

  val view: addToListEntity = app.injector.instanceOf[addToListEntity]

  protected def fakeNavigator() = new FakeNavigator(desiredRoute = onwardRoute)

  protected def controller(
                            dataRetrievalAction: DataRetrievalAction = getEmptyData,
                            navigator: FakeNavigator = fakeNavigator()
                          ) =
    new AddPartnerController(
      frontendAppConfig,
      FakeUserAnswersCacheConnector,
      navigator,
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view
    )

  private def viewmodel(partners: Seq[Person]) = EntityViewModel(
    postCall = routes.AddPartnerController.onSubmit(NormalMode),
    title = Message("addPartners.title"),
    heading = Message("addPartners.heading"),
    entities = partners,
    maxLimit = maxPartners,
    entityType = Message("addPartners.entityType"),
    insetText = Some(Message("addPartner.insetText")),
    returnLink = Some(routes.PartnershipRegistrationTaskListController.onPageLoad().url)
  )

  val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "cacheId",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), UserAnswers(Json.obj()))

  private def viewAsString(form: Form[_] = form, partners: Seq[Person] = Nil) =
    view(form, viewmodel(partners), NormalMode)(request, messages).toString

  private def person(index: Int, isDeleted: Boolean = false) = Person(index,
    s"first$index last$index", deleteLink(index), editLink(index), isDeleted = isDeleted, isComplete = true)

  // scalastyle:off magic.number
  private val johnDoe = PersonName("John", "Doe")
  private val joeBloggs = PersonName("Joe", "Bloggs")
  // scalastyle:on magic.number

  private def deleteLink(index: Int) = controllers.register.administratorPartnership.partners
    .routes.ConfirmDeletePartnerController.onPageLoad(index, NormalMode).url

  private def editLink(index: Int) = controllers.register.administratorPartnership.partners.routes.CheckYourAnswersController.onPageLoad(index, NormalMode).url

  // scalastyle:off magic.number
  private val maxPartners = frontendAppConfig.maxPartners

  private def dataRetrievalAction(partners: PersonName*): FakeDataRetrievalAction = {
    val validData = Json.obj("partners" ->
      partners.map(d => Json.obj(
        PartnerNameId.toString -> Json.toJson(d) /*,
        IsPartnerCompleteId.toString -> true*/
      ))
    )
    new FakeDataRetrievalAction(Some(validData))
  }

}