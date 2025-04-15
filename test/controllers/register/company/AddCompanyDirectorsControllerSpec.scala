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

package controllers.register.company

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.company.AddCompanyDirectorsFormProvider
import identifiers.register.company.AddCompanyDirectorsId
import identifiers.register.company.directors.DirectorNameId
import models._
import models.requests.DataRequest
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.testhelpers.DataCompletionBuilder.DataCompletionUserAnswerOps
import utils.{FakeNavigator, UserAnswers}
import viewmodels.Person
import views.html.register.company.{addCompanyDirectors, addCompanyDirectorsv2}

class AddCompanyDirectorsControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {

  import AddCompanyDirectorsControllerSpec._

  override def fakeRequest: FakeRequest[AnyContent] = FakeRequest("", "/")
  "AddCompanyDirectors Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsStringV2()
    }

    "not populate the view on a GET when the question has previously been answered" in {
      val directors = Seq(Person(0, "first0 last0", deleteLink(0), editLink(0), isDeleted = false, isComplete = true))
      val getRelevantData = UserAnswers().completeDirector(index = 0).dataRetrievalAction

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)
      contentAsString(result) mustBe viewAsStringV2(form, directors)
    }

    "redirect to the next page when no directors exist and the user submits" in {
      val result = controller().onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to the task list page when less than maximum directors exist and valid data is submitted if PSA registration toggle is on" in {
      val getRelevantData = dataRetrievalAction(Seq.fill(maxDirectors - 1)(johnDoe)*)

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))

      val result = controller(getRelevantData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad().url)
    }

    "return a Bad Request and errors when less than maximum directors exist and invalid data is submitted" in {
      val getRelevantData = UserAnswers().completeDirector(index = 0).completeDirector(1).dataRetrievalAction
      val directorAsPerson = Seq(person(0), person(1))

      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(getRelevantData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsStringV2(boundForm, directorAsPerson)
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
      val navUrl = Some("/register-as-pension-scheme-administrator/register/company/directors/2/name")

      val result = controller(getRelevantData, navigator).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe navUrl
    }

    "redirect to the next page when maximum active directors exist and the user submits" in {
      val directorDetails = Seq.fill(maxDirectors)(johnDoe) ++ Seq(joeBloggs.copy(isDeleted = true))

      val getRelevantData = dataRetrievalAction(directorDetails*)

      val result = controller(getRelevantData).onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "populate the view with directors when they exist" in {
      val directorsAsPerson = Seq(person(0), person(1))
      val getRelevantData = UserAnswers().completeDirector(index = 0).completeDirector(index = 1).dataRetrievalAction
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsStringV2(form, directorsAsPerson)
    }

    "exclude the deleted directors from the list" in {
      val getRelevantData = UserAnswers().completeDirector(0).completeDirector(1, isDeleted = true).dataRetrievalAction
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsStringV2(form, Seq(person(0)))
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

object AddCompanyDirectorsControllerSpec extends AddCompanyDirectorsControllerSpec {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad

  private val formProvider = new AddCompanyDirectorsFormProvider()
  private val form = formProvider()
  val view: addCompanyDirectors = app.injector.instanceOf[addCompanyDirectors]
  val viewv2: addCompanyDirectorsv2 = app.injector.instanceOf[addCompanyDirectorsv2]

  protected def fakeNavigator() = new FakeNavigator(desiredRoute = onwardRoute)

  protected def controller(
                            dataRetrievalAction: DataRetrievalAction = getEmptyData,
                            navigator: FakeNavigator = fakeNavigator()
                          ) =
    new AddCompanyDirectorsController(
      frontendAppConfig,
      navigator,
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      controllerComponents,
      view,
      viewv2
    )

  val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "cacheId",
    PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None), UserAnswers(Json.obj()))

  private def viewAsStringV2(form: Form[?] = form, directorsComplete: Seq[Person] = Nil, directorsInComplete: Seq[Person] = Nil) =
    viewv2(form, NormalMode, directorsComplete,directorsInComplete, None)(request, messages).toString

  // scalastyle:off magic.number
  private def person(index: Int, isDeleted: Boolean = false) = Person(index,
    s"first$index last$index", deleteLink(index), editLink(index), isDeleted = isDeleted, isComplete = true)

  private val johnDoe = PersonName("John", "Doe")
  private val joeBloggs = PersonName("Joe", "Bloggs")
  // scalastyle:on magic.number

  private def deleteLink(index: Int) = controllers.register.company.directors.routes.ConfirmDeleteDirectorController.onPageLoad(NormalMode, index).url

  private def editLink(index: Int) = controllers.register.company.directors.routes.CheckYourAnswersController.onPageLoad(NormalMode, index).url

  // scalastyle:off magic.number
  private val maxDirectors = frontendAppConfig.maxDirectors

  private def dataRetrievalAction(directors: PersonName*): FakeDataRetrievalAction = {
    val validData = Json.obj("directors" ->
      directors.map(d => Json.obj(
        DirectorNameId.toString -> Json.toJson(d)
      ))
    )
    new FakeDataRetrievalAction(Some(validData))
  }

}