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

package controllers.behaviours

import controllers.ControllerSpecBase
import controllers.actions._
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._

trait ControllerWithCommonBehaviour extends ControllerSpecBase with ScalaFutures {
  def onwardRoute = Call("GET", "/foo")

  def controllerWithCommonFunctions[T](onPageLoadAction: DataRetrievalAction => Action[AnyContent],
                                       onSubmitAction: DataRetrievalAction => Action[AnyContent],
                                       validData: DataRetrievalAction,
                                       viewAsString: Form[T] => String,
                                       form: Form[T],
                                       request: FakeRequest[AnyContentAsFormUrlEncoded]
                                     ): Unit = {
    "calling onPageLoad" must {
      "return OK and the correct view" in {
        val result = onPageLoadAction(validData)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString(form)
      }

      "redirect to session expired page when there is no data" in {
        val result = onPageLoadAction(dontGetAnyData)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "calling onSubmit" must {

      "redirect to the next page when valid data is submitted" in {
        val result = onSubmitAction(validData)(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

      "redirect to session expired page when there is no data" in {

        val result = onSubmitAction(dontGetAnyData)(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }
}
