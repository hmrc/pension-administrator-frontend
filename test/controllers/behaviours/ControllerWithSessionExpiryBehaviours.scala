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
import play.api.mvc._
import play.api.test.Helpers._

trait ControllerWithSessionExpiryBehaviours extends ControllerSpecBase with ScalaFutures {

  def controllerWithSessionExpiry[T](onPageLoadAction: Action[AnyContent],
                                        onSubmitAction: Action[AnyContent]
                                       ): Unit = {
    "calling onPageLoad" must {

      "redirect to session expired page when there is no data" in {

        val result = onPageLoadAction(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "calling onSubmit" must {

      "redirect to session expired page when there is no data" in {

        val result = onSubmitAction(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }
  }
}
