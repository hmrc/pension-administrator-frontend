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

package controllers

import java.time.LocalDate

import connectors.{UserAnswersCacheConnector, FakeUserAnswersCacheConnector}
import forms.PersonDetailsFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{NormalMode, PSAUser, PersonDetails, UserType}
import play.api.data.Form
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.PersonDetailsViewModel
import views.html.personDetails
import scala.concurrent.Future

trait PersonDetailsControllerBehaviour {
  this: ControllerSpecBase =>

  import PersonDetailsControllerBehaviour._

  // scalastyle:off method.length

  def personDetailsController[I <: TypedIdentifier[PersonDetails]](
                                                                    viewModel: PersonDetailsViewModel,
                                                                    id: I,
                                                                    createController: (UserAnswersCacheConnector, Navigator) => PersonDetailsController
                                                                  ): Unit = {

    "return OK and the correct view for a GET request" in {
      val fixture = testFixture(createController)

      val result = Future(fixture.controller.get(id, viewModel)(testRequest()))

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(this, testForm(), viewModel)
    }

    "populate the view correctly for a GET request with existing data" in {
      val fixture = testFixture(createController)
      val data = UserAnswers().set(id)(testPersonDetails).asOpt.value
      val request = testRequest(data)

      val result = Future(fixture.controller.get(id, viewModel)(request))

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(this, testForm().fill(testPersonDetails), viewModel)
    }

    "redirect to the next page when valid data is submitted" in {
      val fixture = testFixture(createController)
      val request = testRequest(personDetails = Some(testPersonDetails))

      val result = fixture.controller.post(id, viewModel, NormalMode)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "save the user answer when valid data is submitted" in {
      val fixture = testFixture(createController)
      val request = testRequest(personDetails = Some(testPersonDetails))

      fixture.controller.post(id, viewModel, NormalMode)(request)

      fixture.dataCacheConnector.verify(id, testPersonDetails)
    }

    "return Bad Request and errors when invalid data is submitted" in {
      val fixture = testFixture(createController)
      val request = testRequest(personDetails = Some(invalidPersonDetails))
      val formWithErrors = testForm().bindFromRequest()(request)

      val result = fixture.controller.post(id, viewModel, NormalMode)(request)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(this, formWithErrors, viewModel)
    }

  }

  // scalastyle:on method.length

}

// scalastyle:off magic.number

object PersonDetailsControllerBehaviour {

  lazy val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val testPersonDetails =
    PersonDetails(
      firstName = "test-first-name",
      middleName = Some("test-middle-name"),
      lastName = "test-last-name",
      dateOfBirth = LocalDate.of(1969, 7, 20)
    )

  val invalidPersonDetails =
    PersonDetails(
      firstName = "",
      middleName = None,
      lastName = "",
      dateOfBirth = LocalDate.now().plusDays(1)
    )

  case class TestFixture(dataCacheConnector: FakeUserAnswersCacheConnector, controller: PersonDetailsController)

  def testFixture(
                   createController: (UserAnswersCacheConnector, Navigator) => PersonDetailsController
                 ): TestFixture = {

    val connector = new FakeUserAnswersCacheConnector {}
    val navigator = new FakeNavigator(onwardRoute)

    TestFixture(
      dataCacheConnector = connector,
      controller = createController(connector, navigator)
    )

  }

  def testRequest(answers: UserAnswers = UserAnswers(), personDetails: Option[PersonDetails] = None): DataRequest[AnyContent] = {

    val fakeRequest = FakeRequest("", "")

    val request = personDetails.map {
      details =>
        fakeRequest.withFormUrlEncodedBody(
          "firstName" -> details.firstName,
          "middleName" -> details.middleName.getOrElse(""),
          "lastName" -> details.lastName,
          "dateOfBirth.day" -> details.dateOfBirth.getDayOfMonth.toString,
          "dateOfBirth.month" -> details.dateOfBirth.getMonthValue.toString,
          "dateOfBirth.year" -> details.dateOfBirth.getYear.toString
        )
    } getOrElse fakeRequest

    DataRequest(
      request = request,
      externalId = "test-external-id",
      user = PSAUser(UserType.Individual, "userId", None, false, None),
      userAnswers = answers
    )

  }

  def testForm(): Form[PersonDetails] =
    new PersonDetailsFormProvider()()

  def viewAsString(base: ControllerSpecBase, form: Form[_], viewModel: PersonDetailsViewModel): String =
    personDetails(base.frontendAppConfig, form, viewModel)(base.fakeRequest, base.messages).toString()

}
