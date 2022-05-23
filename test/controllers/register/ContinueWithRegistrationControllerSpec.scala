/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions.{AuthAction, DataRetrievalAction, FakeAuthAction, FakeUnAuthorisedAction}
import controllers.behaviours.ControllerWithQuestionPageBehaviours
import forms.register.YesNoFormProvider
import models.NormalMode
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.UserAnswers
import views.html.register.continueWithRegistration

class ContinueWithRegistrationControllerSpec extends ControllerWithQuestionPageBehaviours {

  private val form: Form[Boolean] = new YesNoFormProvider().apply()
  private val view: continueWithRegistration = app.injector.instanceOf[continueWithRegistration]
  private val validData: UserAnswers = UserAnswers()
  private val postRequestTrue: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(("value", true.toString))
  private val postRequestFalse: FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest().withFormUrlEncodedBody(("value", false.toString))

  "ContinueWithRegistrationController" must {

    "onPageLoad" must {

      "return OK and the correct view for a GET" in {

        val result = onPageLoadAction(getEmptyData, FakeAuthAction)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(form)
      }

      "return 303 if user action is not authenticated" in {

        val result = onPageLoadAction(getEmptyData, FakeUnAuthorisedAction())(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "onSubmit" must {
      "redirect to 'company registration task list' page when form value is true" in {
        val result = controller(validData.dataRetrievalAction, FakeAuthAction, FakeUserAnswersCacheConnector)
          .onSubmit()(postRequestTrue)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(company.routes.CompanyRegistrationTaskListController.onPageLoad().url)
      }

      "redirect to company 'before you begin' page when form value is false" in {
        val result = controller(validData.dataRetrievalAction, FakeAuthAction, FakeUserAnswersCacheConnector)
          .onSubmit()(postRequestFalse)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.WhatYouWillNeedController.onPageLoad(NormalMode).url)
      }
    }
  }

  private def controller(
                          dataRetrievalAction: DataRetrievalAction,
                          authAction: AuthAction,
                          cache: UserAnswersCacheConnector = FakeUserAnswersCacheConnector,
                        ): ContinueWithRegistrationController =
    new ContinueWithRegistrationController(
      controllerComponents,
      authAction,
      dataRetrievalAction,
      view,
      new YesNoFormProvider(),
      cache
    )

  private def onPageLoadAction(dataRetrievalAction: DataRetrievalAction, authAction: AuthAction): Action[AnyContent] =
    controller(dataRetrievalAction, authAction).onPageLoad()

  private def viewAsString(form: Form[_]): String =
    view(form)(fakeRequest, messagesApi.preferred(fakeRequest)).toString()
}
