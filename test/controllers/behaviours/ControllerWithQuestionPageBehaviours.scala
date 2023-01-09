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

package controllers.behaviours

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.TypedIdentifier
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Format
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.FakeNavigator

trait ControllerWithQuestionPageBehaviours extends ControllerSpecBase with ScalaFutures {

  val navigator = new FakeNavigator(onwardRoute)
  val requiredDataAction = new DataRequiredActionImpl

  def onwardRoute = Call("GET", "/foo")

  def controllerWithOnPageLoadMethod[T](onPageLoadAction: (DataRetrievalAction, AuthAction) => Action[AnyContent],
                                        emptyData: DataRetrievalAction,
                                        validData: DataRetrievalAction,
                                        emptyForm: Form[T],
                                        preparedForm: Form[T],
                                        viewAsString: Form[T] => String): Unit = {

    "calling onPageLoad" must {

      "return OK and the correct view for a GET" in {

        val result = onPageLoadAction(emptyData, FakeAuthAction)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(emptyForm)
      }

      "populate the view correctly on a GET when the question has previously been answered" in {

        val result = onPageLoadAction(validData, FakeAuthAction)(fakeRequest)

        contentAsString(result) mustBe viewAsString(preparedForm)
      }

      "return 303 if user action is not authenticated" in {

        val result = onPageLoadAction(emptyData, FakeUnAuthorisedAction())(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad.url)
      }
    }

  }

  def controllerWithOnSubmitMethod[T](onSubmitAction: (DataRetrievalAction, AuthAction) => Action[AnyContent],
    validData: DataRetrievalAction,
    formWithErrors: Form[T],
    viewAsString: Form[T] => String,
    postRequest: FakeRequest[AnyContentAsFormUrlEncoded]): Unit = {

    "calling onSubmit" must {

      "redirect to the next page when valid data is submitted" in {

        val result = onSubmitAction(validData, FakeAuthAction)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "return a Bad Request and errors when invalid data is submitted" in {

        val result = onSubmitAction(validData, FakeAuthAction)(fakeRequest)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe viewAsString(formWithErrors)
      }
    }
  }

  def controllerWithOnPageLoadMethodMissingRequiredData(onPageLoadAction: (DataRetrievalAction, AuthAction) => Action[AnyContent],
                                                        validData: DataRetrievalAction): Unit = {

    "when required data is not present" must {

      "onPageLoad redirect to Session Expired if no existing data is found" in {

        val result = onPageLoadAction(validData, FakeAuthAction)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
      }
    }

  }

  def controllerWithOnSubmitMethodMissingRequiredData(onSubmitAction: (DataRetrievalAction, AuthAction) => Action[AnyContent],
                                                        validDate: DataRetrievalAction): Unit = {

    "when required data is not present" must {

      "onSubmit redirect to Session Expired if no existing data is found" in {

        val result = onSubmitAction(validDate, FakeAuthAction)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
      }
    }

  }

  def controllerThatSavesUserAnswers[A, I <: TypedIdentifier[A]](
    saveAction: UserAnswersCacheConnector => Action[AnyContent],
    validRequest: FakeRequest[AnyContentAsFormUrlEncoded],
    id: I,
    value: A
  )(implicit fmt: Format[A]): Unit = {

    "save user answers to cache" in {
      val cache = new FakeUserAnswersCacheConnector() {}
      val result = saveAction(cache)(validRequest)

      whenReady(result) {
        _ =>
         cache.verify(id, value)
      }

    }

  }

}
