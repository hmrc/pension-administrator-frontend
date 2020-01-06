/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers.register

import connectors.cache.FakeUserAnswersCacheConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.ControllerSpecBase
import forms.UTRFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{NormalMode, PSAUser, UserType}
import play.api.data.Form
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FakeNavigator, Navigator, UserAnswers}
import views.html.register.utr

trait UTRControllerBehaviour extends ControllerSpecBase {

  lazy val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()
  val entity: String = "limited company"

  val testUTR = "1234567890"

  val invalidUTR = "abcd123456"

  val view: utr = app.injector.instanceOf[utr]

  def utrController[I <: TypedIdentifier[String]](id: I,
                                                  createController: (UserAnswersCacheConnector, Navigator) => UTRController
                                                                  ): Unit = {

    "return OK and the correct view for a GET request" in {
      val fixture = testFixture(createController)

      val result = fixture.controller.get(id, entity, onwardRoute)(testRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(testForm(), entity, onwardRoute)
    }

    "populate the view correctly for a GET request with existing data" in {
      val fixture = testFixture(createController)
      val data = UserAnswers().set(id)(testUTR).asOpt.value
      val request = testRequest(data)

      val result = fixture.controller.get(id, entity, onwardRoute)(request)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(testForm().fill(testUTR), entity, onwardRoute)
    }

    "redirect to the next page when valid data is submitted" in {
      val fixture = testFixture(createController)
      val request = testRequest(utr = Some(testUTR))

      val result = fixture.controller.post(id, entity, onwardRoute, NormalMode)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      fixture.dataCacheConnector.verify(id, testUTR)
    }

    "return Bad Request and errors when invalid data is submitted" in {
      val fixture = testFixture(createController)
      val request = testRequest(utr = Some(invalidUTR))
      val formWithErrors = testForm().bindFromRequest()(request)

      val result = fixture.controller.post(id, entity, onwardRoute, NormalMode)(request)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(formWithErrors, entity, onwardRoute)
    }

  }

  case class TestFixture(dataCacheConnector: FakeUserAnswersCacheConnector, controller: UTRController)

  def testFixture(
                   createController: (UserAnswersCacheConnector, Navigator) => UTRController
                 ): TestFixture = {

    val connector = new FakeUserAnswersCacheConnector {}
    val navigator = new FakeNavigator(onwardRoute)

    TestFixture(
      dataCacheConnector = connector,
      controller = createController(connector, navigator)
    )

  }

  def testRequest(answers: UserAnswers = UserAnswers(), utr: Option[String] = None): DataRequest[AnyContent] = {

    val fakeRequest = FakeRequest("", "")

    val request = utr.map {
      utrValue =>
        fakeRequest.withFormUrlEncodedBody(
          "value" -> utrValue
        )
    } getOrElse fakeRequest

    DataRequest(
      request = request,
      externalId = "test-external-id",
      user = PSAUser(UserType.Individual, None, isExistingPSA = false, None),
      userAnswers = answers
    )

  }

  def testForm(): Form[String] =
    new UTRFormProvider()()

  def viewAsString(form: Form[_], entity: String, href: Call): String =
    view(form, entity, href)(fakeRequest, messages).toString()

}
