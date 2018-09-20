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

package controllers.register.partnership

import java.time.LocalDate

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.AddEntityFormProvider
import identifiers.register.company.AddCompanyDirectorsId
import identifiers.register.partnership.AddPartnersId
import identifiers.register.partnership.partners.{IsPartnerCompleteId, PartnerDetailsId}
import models.requests.DataRequest
import models.{NormalMode, PSAUser, PersonDetails, UserType}
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import viewmodels.{EntityViewModel, Message, Person}
import views.html.register.addEntity

class AddPartnerControllerSpec extends ControllerSpecBase {

  import AddPartnerControllerSpec._

  "AddPartner Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "not populate the view on a GET when the question has previously been answered" in {
      val partners = Seq(johnDoe)
      val getRelevantData = dataRetrievalAction(partners: _*)

      val result = controller(getRelevantData).onPageLoad()(fakeRequest)
      contentAsString(result) mustBe viewAsString(form, Seq(johnDoePerson))
    }

    "redirect to the next page when no partners exist and the user submits" in {
      val result = controller().onSubmit()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the next page when less than maximum partners exist and valid data is submitted" in {
      val getRelevantData = dataRetrievalAction(Seq.fill(maxPartners - 1)(johnDoe): _*)

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller(getRelevantData).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when less than maximum partners exist and invalid data is submitted" in {
      val partners = Seq.fill(maxPartners - 1)(johnDoe)
      val partnerAsPerson = Seq.fill(maxPartners - 1)(johnDoePerson)
      val getRelevantData = dataRetrievalAction(partners: _*)

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(getRelevantData).onSubmit()(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm, partnerAsPerson)
    }

    "not save the answer when partners exist and valid data is submitted" in {
      val getRelevantData = dataRetrievalAction(johnDoe)

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      controller(getRelevantData).onSubmit()(postRequest)
      FakeUserAnswersCacheConnector.verifyNot(AddCompanyDirectorsId)
    }

    "set the user answer when partners exist and valid data is submitted" in {
      val getRelevantData = dataRetrievalAction(johnDoe)
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val navigator = fakeNavigator()

      val result = controller(getRelevantData, navigator).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      navigator.lastUserAnswers.value.get(AddPartnersId).value mustBe true
    }

    "redirect to the next page when maximum active partners exist and the user submits" in {
      val partnerDetails = Seq.fill(maxPartners)(johnDoe) ++ Seq(joeBloggs.copy(isDeleted = true))

      val getRelevantData = dataRetrievalAction(partnerDetails: _*)

      val result = controller(getRelevantData).onSubmit()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "populate the view with partners when they exist" in {
      val partners = Seq(johnDoe, joeBloggs)
      val partnersAsPerson = Seq(johnDoePerson, joeBloggsPerson)
      val getRelevantData = dataRetrievalAction(partners: _*)
      val result = controller(getRelevantData).onPageLoad()(fakeRequest)

      contentAsString(result) mustBe viewAsString(form, partnersAsPerson)
    }

    "exclude the deleted partners from the list" in {
      val partners = Seq(johnDoe, joeBloggs.copy(isDeleted = true))
      val getRelevantData = dataRetrievalAction(partners: _*)
      val result = controller(getRelevantData).onPageLoad()(fakeRequest)

      contentAsString(result) mustBe viewAsString(form, Seq(johnDoePerson))
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(dontGetAnyData).onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

}

object AddPartnerControllerSpec extends AddPartnerControllerSpec {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new AddEntityFormProvider()
  private val form = formProvider()

  protected def fakeNavigator() = new FakeNavigator(desiredRoute = onwardRoute)

  protected def controller(
                            dataRetrievalAction: DataRetrievalAction = getEmptyData,
                            navigator: FakeNavigator = fakeNavigator()
                          ) =
    new AddPartnerController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      navigator,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  private def viewmodel(partners: Seq[Person]) = EntityViewModel(
    postCall = routes.AddPartnerController.onSubmit(),
    title = Message("addPartners.title"),
    heading = Message("addPartners.heading"),
    entities = partners,
    maxLimit = maxPartners,
    entityType = Message("addPartners.entityType"),
    subHeading = Some(Message("site.secondaryHeader"))
  )

  val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "cacheId",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None), UserAnswers(Json.obj()))

  private def viewAsString(form: Form[_] = form, partners: Seq[Person] = Nil) =
    addEntity(frontendAppConfig, form, viewmodel(partners))(request, messages).toString

  // scalastyle:off magic.number
  private val johnDoe = PersonDetails("John", None, "Doe", LocalDate.of(1862, 6, 9))
  private val joeBloggs = PersonDetails("Joe", None, "Bloggs", LocalDate.of(1969, 7, 16))
  // scalastyle:on magic.number

  private def deleteLink(index: Int) = controllers.register.partnership.partners.routes.ConfirmDeletePartnerController.onPageLoad(index).url

  private def editLink(index: Int) = controllers.register.partnership.partners.routes.PartnerDetailsController.onPageLoad(NormalMode, index).url

  // scalastyle:off magic.number
  private val johnDoePerson = Person(0, "John Doe", deleteLink(0), editLink(0), isDeleted = false, isComplete = true)
  private val joeBloggsPerson = Person(1, "Joe Bloggs", deleteLink(1), editLink(1), isDeleted = false, isComplete = true)

  private val maxPartners = frontendAppConfig.maxPartners

  private def dataRetrievalAction(partners: PersonDetails*): FakeDataRetrievalAction = {
    val validData = Json.obj("partners" ->
      partners.map(d => Json.obj(
        PartnerDetailsId.toString -> Json.toJson(d),
        IsPartnerCompleteId.toString -> true
      ))
    )
    new FakeDataRetrievalAction(Some(validData))
  }

}