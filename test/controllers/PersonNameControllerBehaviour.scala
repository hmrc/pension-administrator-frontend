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

package controllers

import base.SpecBase
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import forms.PersonNameFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{NormalMode, PSAUser, PersonName, UserType}
import play.api.data.Form
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.CommonFormWithHintViewModel
import views.html.personName

import scala.concurrent.Future

trait PersonNameControllerBehaviour extends ControllerSpecBase {

  val personNameView: personName = app.injector.instanceOf[personName]

  lazy val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val testPersonName: PersonName =
    PersonName(
      firstName = "test-first-name",
      lastName = "test-last-name"
    )

  val invalidPersonName: PersonName =
    PersonName(
      firstName = "",
      lastName = ""
    )


  def personNameController[I <: TypedIdentifier[PersonName]](viewModel: CommonFormWithHintViewModel,
                                                             id: I,
                                                             createController: (UserAnswersCacheConnector, Navigator) => PersonNameController
                                                            ): Unit = {

    "return OK and the correct view for a GET request" in {
      val fixture = testFixture(createController)

      val result = Future(fixture.controller.get(id, viewModel, NormalMode)(testRequest(), messages))

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(testForm(), viewModel)
    }

    "populate the view correctly for a GET request with existing data" in {
      val fixture = testFixture(createController)
      val data = UserAnswers().set(id)(testPersonName).asOpt.value
      val request = testRequest(data)

      val result = Future(fixture.controller.get(id, viewModel, NormalMode)(request, messages))

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(testForm().fill(testPersonName), viewModel)
    }

    "redirect to the next page when valid data is submitted" in {
      val fixture = testFixture(createController)
      val request = testRequest(personDetails = Some(testPersonName))

      val result = fixture.controller.post(id, viewModel, NormalMode)(request, messages)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "save the user answer when valid data is submitted" in {
      val fixture = testFixture(createController)
      val request = testRequest(personDetails = Some(testPersonName))

      fixture.controller.post(id, viewModel, NormalMode)(request, messages)

      fixture.dataCacheConnector.verify(id, testPersonName)
    }

    "return Bad Request and errors when invalid data is submitted" in {
      val fixture = testFixture(createController)
      val request = testRequest(personDetails = Some(invalidPersonName))
      val formWithErrors = testForm().bindFromRequest()(request)

      val result = fixture.controller.post(id, viewModel, NormalMode)(request, messages)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(formWithErrors, viewModel)
    }

  }

  case class TestFixture(dataCacheConnector: FakeUserAnswersCacheConnector, controller: PersonNameController)

  def testFixture(createController: (UserAnswersCacheConnector, Navigator) => PersonNameController): TestFixture = {

    val connector = new FakeUserAnswersCacheConnector {}
    val navigator = new FakeNavigator(onwardRoute)

    TestFixture(
      dataCacheConnector = connector,
      controller = createController(connector, navigator)
    )

  }

  def testRequest(answers: UserAnswers = UserAnswers(), personDetails: Option[PersonName] = None): DataRequest[AnyContent] = {

    val fakeRequest = FakeRequest("", "")

    val request = personDetails.map {
      details =>
        fakeRequest.withFormUrlEncodedBody(
          "firstName" -> details.firstName,
          "lastName" -> details.lastName
        )
    } getOrElse fakeRequest

    DataRequest(
      request = request,
      externalId = "test-external-id",
      user = PSAUser(userType = UserType.Individual, nino = None, isExistingPSA = false, existingPSAId = None),
      userAnswers = answers
    )

  }

  def testForm(): Form[PersonName] =
    new PersonNameFormProvider()()

  def viewAsString(form: Form[_], viewModel: CommonFormWithHintViewModel): String =
    personNameView(form, viewModel, NormalMode)(fakeRequest, messages).toString()

}
