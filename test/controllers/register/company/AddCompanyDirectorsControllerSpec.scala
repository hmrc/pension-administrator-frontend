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

package controllers.register.company

import java.time.LocalDate

import connectors.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.company.AddCompanyDirectorsFormProvider
import identifiers.register.company.AddCompanyDirectorsId
import identifiers.register.company.directors.{DirectorDetailsId, IsDirectorCompleteId}
import models.requests.DataRequest
import models.{NormalMode, PSAUser, PersonDetails, UserType}
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, UserAnswers}
import viewmodels.Person
import views.html.register.company.addCompanyDirectors

class AddCompanyDirectorsControllerSpec extends ControllerSpecBase {

  import AddCompanyDirectorsControllerSpec._

  "AddCompanyDirectors Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "not populate the view on a GET when the question has previously been answered" in {
      val directors = Seq(johnDoe)
      val getRelevantData = dataRetrievalAction(directors: _*)

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)
      contentAsString(result) mustBe viewAsString(form, Seq(johnDoePerson))
    }

    "redirect to the next page when no directors exist and the user submits" in {
      val result = controller().onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the next page when less than maximum directors exist and valid data is submitted" in {
      val getRelevantData = dataRetrievalAction(Seq.fill(maxDirectors - 1)(johnDoe): _*)

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller(getRelevantData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when less than maximum directors exist and invalid data is submitted" in {
      val directors = Seq.fill(maxDirectors - 1)(johnDoe)
      val directorAsPerson = Seq.fill(maxDirectors - 1)(johnDoePerson)
      val getRelevantData = dataRetrievalAction(directors: _*)

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(getRelevantData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm, directorAsPerson)
    }

    "not save the answer when directors exist and valid data is submitted" in {
      val getRelevantData = dataRetrievalAction(johnDoe)

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      controller(getRelevantData).onSubmit(NormalMode)(postRequest)
      FakeUserAnswersCacheConnector.verifyNot(AddCompanyDirectorsId)
    }

    "set the user answer when directors exist and valid data is submitted" in {
      val getRelevantData = dataRetrievalAction(johnDoe)
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val navigator = fakeNavigator()

      val result = controller(getRelevantData, navigator).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      navigator.lastUserAnswers.value.get(AddCompanyDirectorsId).value mustBe true
    }

    "redirect to the next page when maximum active directors exist and the user submits" in {
      val directorDetails = Seq.fill(maxDirectors)(johnDoe) ++ Seq(joeBloggs.copy(isDeleted = true))

      val getRelevantData = dataRetrievalAction(directorDetails: _*)

      val result = controller(getRelevantData).onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "populate the view with directors when they exist" in {
      val directors = Seq(johnDoe, joeBloggs)
      val directorsAsPerson = Seq(johnDoePerson, joeBloggsPerson)
      val getRelevantData = dataRetrievalAction(directors: _*)
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form, directorsAsPerson)
    }

    "exclude the deleted directors from the list" in {
      val directors = Seq(johnDoe, joeBloggs.copy(isDeleted = true))
      val getRelevantData = dataRetrievalAction(directors: _*)
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form, Seq(johnDoePerson))
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

}

object AddCompanyDirectorsControllerSpec extends AddCompanyDirectorsControllerSpec {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new AddCompanyDirectorsFormProvider()
  private val form = formProvider()

  protected def fakeNavigator() = new FakeNavigator(desiredRoute = onwardRoute)

  protected def controller(
                            dataRetrievalAction: DataRetrievalAction = getEmptyData,
                            navigator: FakeNavigator = fakeNavigator()
                          ) =
    new AddCompanyDirectorsController(
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      navigator,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider
    )

  val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "cacheId",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None), UserAnswers(Json.obj()))

  private def viewAsString(form: Form[_] = form, directors: Seq[Person] = Nil) =
    addCompanyDirectors(frontendAppConfig, form, NormalMode, directors)(request, messages).toString

  // scalastyle:off magic.number
  private val johnDoe = PersonDetails("John", None, "Doe", LocalDate.of(1862, 6, 9))
  private val joeBloggs = PersonDetails("Joe", None, "Bloggs", LocalDate.of(1969, 7, 16))
  // scalastyle:on magic.number

  private def deleteLink(index: Int) = controllers.register.company.directors.routes.ConfirmDeleteDirectorController.onPageLoad(NormalMode, index).url

  private def editLink(index: Int) = controllers.register.company.directors.routes.CheckYourAnswersController.onPageLoad(NormalMode, index).url

  // scalastyle:off magic.number
  private val johnDoePerson = Person(0, "John Doe", deleteLink(0), editLink(0), isDeleted = false, isComplete = true)
  private val joeBloggsPerson = Person(1, "Joe Bloggs", deleteLink(1), editLink(1), isDeleted = false, isComplete = true)

  private val maxDirectors = frontendAppConfig.maxDirectors

  private def dataRetrievalAction(directors: PersonDetails*): FakeDataRetrievalAction = {
    val validData = Json.obj("directors" ->
      directors.map(d => Json.obj(
        DirectorDetailsId.toString -> Json.toJson(d),
        IsDirectorCompleteId.toString -> true
      ))
    )
    new FakeDataRetrievalAction(Some(validData))
  }

}