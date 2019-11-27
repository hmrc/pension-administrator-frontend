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

import base.SpecBase
import connectors.cache.FakeUserAnswersCacheConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.register.IsRegisteredNameController
import forms.register.IsRegisteredNameFormProvider
import identifiers.TypedIdentifier
import identifiers.register.IsRegisteredNameId
import models.requests.DataRequest
import models.{PSAUser, UserType}
import play.api.data.Form
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.CommonFormViewModel
import views.html.register.isRegisteredName

trait IsRegisteredNameControllerBehaviour {
  this: ControllerSpecBase =>

  import IsRegisteredNameControllerBehaviour._

  // scalastyle:off method.length

  def isRegisteredNameController[I <: TypedIdentifier[Boolean]](viewModel: CommonFormViewModel,
                                                                createController: (UserAnswersCacheConnector, Navigator) => IsRegisteredNameController,
                                                                id: I = IsRegisteredNameId
                                                               ): Unit = {

    "return OK and the correct view for a GET request" in {
      val fixture = testFixture(createController)

      val result = fixture.controller.get(viewModel)(testRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(this, testForm(), viewModel)
    }

    "populate the view correctly for a GET request with existing data" in {
      val fixture = testFixture(createController)
      val data = UserAnswers().set(id)(true).asOpt.value
      val request = testRequest(data)

      val result = fixture.controller.get(viewModel)(request)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(this, testForm().fill(true), viewModel)
    }

    "redirect to the next page when valid data is submitted" in {
      val fixture = testFixture(createController)
      val request = testRequest()

      val result = fixture.controller.post(viewModel)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      fixture.dataCacheConnector.verify(id, false)
    }

    "return Bad Request and errors when invalid data is submitted" in {
      val fixture = testFixture(createController)
      val request = testRequest(booleanValue = "invalid")
      val formWithErrors = testForm().bindFromRequest()(request)

      val result = fixture.controller.post(viewModel)(request)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(this, formWithErrors, viewModel)
    }

  }

}

object IsRegisteredNameControllerBehaviour extends SpecBase {

  lazy val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  val view: isRegisteredName = app.injector.instanceOf[isRegisteredName]

  case class TestFixture(dataCacheConnector: FakeUserAnswersCacheConnector, controller: IsRegisteredNameController)

  def testFixture(createController: (UserAnswersCacheConnector, Navigator) => IsRegisteredNameController): TestFixture = {

    val connector = new FakeUserAnswersCacheConnector {}
    val navigator = new FakeNavigator(onwardRoute)

    TestFixture(
      dataCacheConnector = connector,
      controller = createController(connector, navigator)
    )

  }

  def testRequest(answers: UserAnswers = UserAnswers(), booleanValue: String = "false"): DataRequest[AnyContent] = {

    val fakeRequest = FakeRequest("", "")

    val request =
      fakeRequest.withFormUrlEncodedBody(
        "value" -> booleanValue
      )

    DataRequest(
      request = request,
      externalId = "test-external-id",
      user = PSAUser(userType = UserType.Individual, nino = None, isExistingPSA = false, existingPSAId = None),
      userAnswers = answers
    )

  }

  val requiredKey = "isRegisteredName.company.error"

  def testForm(): Form[Boolean] =
    new IsRegisteredNameFormProvider()(requiredKey)

  def viewAsString(base: ControllerSpecBase, form: Form[_], viewModel: CommonFormViewModel): String =
    view(form, viewModel)(base.fakeRequest, base.messages).toString()

}
