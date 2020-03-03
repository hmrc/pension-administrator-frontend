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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import forms.MoreThanTenFormProvider
import identifiers.TypedIdentifier
import identifiers.register.MoreThanTenDirectorsOrPartnersChangedId
import identifiers.register.company.MoreThanTenDirectorsId
import models.requests.DataRequest
import models.{NormalMode, PSAUser, UpdateMode, UserType}
import org.scalatest.OptionValues
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Call, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.MoreThanTenViewModel
import views.html.moreThanTen

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

class MoreThanTenControllerSpec extends ControllerSpecBase with OptionValues {

  val moreThanTenView: moreThanTen = app.injector.instanceOf[moreThanTen]

  private val form: Form[Boolean] = new MoreThanTenFormProvider()("moreThanTenDirectors.error.required")

  val testId: TypedIdentifier[Boolean] = new TypedIdentifier[Boolean] {}

  lazy val onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  "MoreThanTenController" must {

    "return OK and the correct view for a GET" in {
      val fixture = testFixture(this)

      val result = Future.successful(fixture.controller.get(viewModel(), NormalMode)(testRequest()))

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(this)
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val fixture = testFixture(this)
      val answers = UserAnswers().set(testId)(true).asOpt.value
      val request = testRequest(answers)

      val result = Future.successful(fixture.controller.get(viewModel(), NormalMode)(request))

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(this, form.fill(true))
    }

    "save the user answer" in {
      val fixture = testFixture(this)
      val request = testRequest(moreThanTen = Some(true.toString))

      fixture.controller.post(viewModel(), NormalMode)(request)
      fixture.dataCacheConnector.verify(testId, true)
    }

    "redirect to the next page when valid data is submitted" in {
      val fixture = testFixture(this)
      val request = testRequest(moreThanTen = Some(true.toString))

      val result = fixture.controller.post(viewModel(), NormalMode)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val fixture = testFixture(this)
      val request = testRequest(moreThanTen = Some(""))

      val result = fixture.controller.post(viewModel(), NormalMode)(request)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(this, form.bindFromRequest()(request))
    }

    "save the user answer and update the change flag if gone from no to yes" in {
      val before = Json.obj(
        MoreThanTenDirectorsId.toString -> "false"
      )
      val fixture = testFixture(this)
      val request = testRequest(answers = UserAnswers(before), moreThanTen = Some(true.toString))

      Await.result(fixture.controller.post(viewModel(MoreThanTenDirectorsId), UpdateMode)(request), Duration.Inf)
      fixture.dataCacheConnector.verify(MoreThanTenDirectorsId, true)
      fixture.dataCacheConnector.verify(MoreThanTenDirectorsOrPartnersChangedId, true)
    }

    "save the user answer and update the change flag if gone from yes to no" in {
      val before = Json.obj(
        MoreThanTenDirectorsId.toString -> true
      )
      val fixture = testFixture(this)
      val request = testRequest(answers = UserAnswers(before), moreThanTen = Some(false.toString))

      Await.result(fixture.controller.post(viewModel(MoreThanTenDirectorsId), UpdateMode)(request), Duration.Inf)
      fixture.dataCacheConnector.verify(MoreThanTenDirectorsId, false)
      fixture.dataCacheConnector.verify(MoreThanTenDirectorsOrPartnersChangedId, true)
    }

    "save the user answer and do not update the change flag if gone from yes to yes (i.e. no change)" in {
      val before = Json.obj(
        MoreThanTenDirectorsId.toString -> true
      )
      val fixture = testFixture(this)
      val request = testRequest(answers = UserAnswers(before), moreThanTen = Some(true.toString))

      Await.result(fixture.controller.post(viewModel(MoreThanTenDirectorsId), UpdateMode)(request), Duration.Inf)
      fixture.dataCacheConnector.verify(MoreThanTenDirectorsId, true)
      fixture.dataCacheConnector.verifyNot(MoreThanTenDirectorsOrPartnersChangedId)
    }
  }

  def testRequest(answers: UserAnswers = UserAnswers(), moreThanTen: Option[String] = None): DataRequest[AnyContent] = {
    val fakeRequest = FakeRequest("", "")

    val request = moreThanTen match {
      case Some(s) => fakeRequest.withFormUrlEncodedBody(("value", s))
      case _ => fakeRequest
    }

    DataRequest(
      request = request,
      externalId = "test-external-id",
      user = PSAUser(userType = UserType.Organisation, nino = None, isExistingPSA = false, existingPSAId = None),
      userAnswers = answers
    )
  }

  case class TestFixture(controller: MoreThanTenController, dataCacheConnector: FakeUserAnswersCacheConnector)

  def testFixture(base: SpecBase): TestFixture = {
    val connector = new FakeUserAnswersCacheConnector {}
    val controller: MoreThanTenController = new MoreThanTenController {
      override protected def appConfig: FrontendAppConfig = base.frontendAppConfig

      override protected def cacheConnector: UserAnswersCacheConnector = connector

      override protected def navigator: Navigator = new FakeNavigator(onwardRoute)

      override def messagesApi: MessagesApi = base.messagesApi

      override protected def view: moreThanTen = moreThanTenView

      implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.Implicits.global

      override protected def controllerComponents: MessagesControllerComponents = stubMessagesControllerComponents()
    }

    TestFixture(controller, connector)
  }



  def viewModel(id: TypedIdentifier[Boolean] = testId): MoreThanTenViewModel =
    MoreThanTenViewModel(
      title = "moreThanTenDirectors.title",
      heading = "moreThanTenDirectors.heading",
      hint = "moreThanTenDirectors.hint",
      postCall = Call("POST", "/"),
      id,
      None,
      errorKey = "moreThanTenDirectors.error.required"
    )

  def viewAsString(base: SpecBase, form: Form[_] = form): String =
    moreThanTenView(form, viewModel(), NormalMode)(base.fakeRequest, messagesApi.preferred(fakeRequest)).toString

}
