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

package controllers.register

import connectors.FakeDataCacheConnector
import controllers.JourneyType.JourneyType
import controllers.{ControllerSpecBase, Journey, JourneyType}
import controllers.actions._
import forms.register.DeclarationFormProvider
import identifiers.register.DeclarationId
import models.requests.DataRequest
import play.api.data.Form
import play.api.mvc.{Call, Result}
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.declaration

import scala.concurrent.{ExecutionContext, Future}

class DeclarationControllerSpec extends ControllerSpecBase {

  import DeclarationControllerSpec._

  "Declaration Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired on a GET request if no cached data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page on a valid POST request" in {
      val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
      val result = controller().onSubmit(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "save the answer on a valid POST request" in {
      val request = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
      val result = controller().onSubmit(request)

      status(result) mustBe SEE_OTHER
      FakeDataCacheConnector.verify(DeclarationId, true)
    }

    "reject an invalid POST request and display errors" in {
      val formWithErrors = form.withError("agree", messages("declaration.invalid"))
      val result = controller().onSubmit(fakeRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(formWithErrors)
    }

    "redirect to Session Expired on a POST request if no cached data is found" in {
      val result = controller(dontGetAnyData).onSubmit(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "set cancel link correctly to Individual What You Will Need page on a GET request" in {
      val result = controller(journey = individualJourney).onPageLoad()(fakeRequest)

      contentAsString(result) mustBe viewAsString(cancelCall = individualCancelCall)
    }

    "set cancel link correctly to Company What You Will Need page on a GET request" in {
      val result = controller(journey = companyJourney).onPageLoad()(fakeRequest)

      contentAsString(result) mustBe viewAsString(cancelCall = companyCancelCall)
    }

    "set cancel link correctly to Individual What You Will Need page on a POST request" in {
      val formWithErrors = form.withError("agree", messages("declaration.invalid"))
      val result = controller(journey = individualJourney).onSubmit()(fakeRequest)

      contentAsString(result) mustBe viewAsString(formWithErrors, individualCancelCall)
    }

    "set cancel link correctly to Company What You Will Need page on a POST request" in {
      val formWithErrors = form.withError("agree", messages("declaration.invalid"))
      val result = controller(journey = companyJourney).onSubmit()(fakeRequest)

      contentAsString(result) mustBe viewAsString(formWithErrors, companyCancelCall)
    }
  }

}

object DeclarationControllerSpec extends ControllerSpecBase {

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val form: Form[_] = new DeclarationFormProvider()()

  private val companyCancelCall = controllers.register.company.routes.WhatYouWillNeedController.onPageLoad()
  private val companyJourney = new Journey {
    override def withJourneyType(f: JourneyType => Future[Result])(implicit request: DataRequest[_], ec: ExecutionContext): Future[Result] = {
      f(JourneyType.Company)
    }
  }

  private val individualCancelCall = controllers.register.individual.routes.WhatYouWillNeedController.onPageLoad()
  private val individualJourney = new Journey {
    override def withJourneyType(f: JourneyType => Future[Result])(implicit request: DataRequest[_], ec: ExecutionContext): Future[Result] = {
      f(JourneyType.Individual)
    }
  }

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData, journey: Journey = companyJourney) =
    new DeclarationController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeNavigator,
      new DeclarationFormProvider(),
      FakeDataCacheConnector,
      journey
    )

  private def viewAsString(form: Form[_] = form, cancelCall: Call = companyCancelCall) =
    declaration(
      frontendAppConfig,
      form,
      cancelCall
    )(fakeRequest, messages).toString

}
