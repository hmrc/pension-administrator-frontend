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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions.FakeAllowAccessProvider
import forms.DOBFormProvider
import identifiers.TypedIdentifier
import models.requests.DataRequest
import models.{NormalMode, PSAUser, UserType}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{AnyContent, Call, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, redirectLocation, status, _}
import utils.{FakeNavigator, Navigator, UserAnswers}
import viewmodels.{CommonFormWithHintViewModel, Message}
import views.html.dob

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class DOBControllerSpec extends ControllerSpecBase {
  private val dobView: dob = app.injector.instanceOf[dob]

  private def viewAsString(form: Form[_] = form): String =
    dobView(
      form,
      viewModel
    )(fakeRequest, messagesApi.preferred(fakeRequest)).toString

  private val form = new DOBFormProvider()()
  private val date: LocalDate = LocalDate.now
  private val postCall: Call = Call("POST", "http://www.test.com")
  private lazy val viewModel: CommonFormWithHintViewModel =
    CommonFormWithHintViewModel(
      postCall = postCall,
      title = Message("directorDob.title"),
      heading = Message("dob.heading"),
      hint = None,
      formFieldName = None,
      mode = NormalMode,
      entityName = "psa-name"
    )

  val id: TypedIdentifier[LocalDate] = new TypedIdentifier[LocalDate] {}

  val userAnswers: UserAnswers = UserAnswers().businessName().directorName()

  "DOBController" must {

    "return OK and the correct view for a GET request" in {

      val result = Future(controller(this).get(id, viewModel)(testRequest()))

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly for a GET request with existing data" in {
      val data = userAnswers.set(id)(date).asOpt.value
      val request = testRequest(data)

      val result = Future(controller(this).get(id, viewModel)(request))

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(form.fill(LocalDate.now))
    }

    "redirect to the next page when valid data is submitted" in {
      val request = testRequest(date = Some(date))

      val result = controller(this).post(id, viewModel)(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(postCall.url)
      FakeUserAnswersCacheConnector.verify(id, date)
    }

    "return Bad Request and errors when invalid data is submitted" in {
      val request = fakeRequest.withFormUrlEncodedBody(
        "value.day" -> "30",
        "value.month" -> "2",
        "value.year" -> "2019"
      )

      val dataRequest: DataRequest[AnyContent] = DataRequest(
        request = request,
        externalId = "test-external-id",
        user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
        userAnswers = userAnswers
      )

      val formWithErrors = form.bind(
        Map(
          "value.day" -> "30",
          "value.month" -> "2",
          "value.year" -> "2019"
        )
      )

      val result = controller(this).post(id, viewModel)(dataRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(formWithErrors)
    }

  }


  def testRequest(answers: UserAnswers = userAnswers, date: Option[LocalDate] = None): DataRequest[AnyContent] = {

    val fakeRequest = FakeRequest("", "")

    val request = date.map {
      dob =>
        fakeRequest.withFormUrlEncodedBody(
          "value.day" -> dob.getDayOfMonth.toString,
          "value.month" -> dob.getMonthValue.toString,
          "value.year" -> dob.getYear.toString
        )
    } getOrElse fakeRequest

    DataRequest(
      request = request,
      externalId = "test-external-id",
      user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
      userAnswers = answers
    )

  }

  def controller(base: ControllerSpecBase): DOBController = {
    new DOBController {
      override def appConfig: FrontendAppConfig = base.frontendAppConfig

      override def cacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector

      override def navigator: Navigator = new FakeNavigator(postCall)

      override def messagesApi: MessagesApi = base.messagesApi

      override val allowAccess = FakeAllowAccessProvider(config = base.frontendAppConfig)

      override protected def controllerComponents: MessagesControllerComponents = SpecBase.controllerComponents

      implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

      val view: dob = dobView
    }
  }

}


