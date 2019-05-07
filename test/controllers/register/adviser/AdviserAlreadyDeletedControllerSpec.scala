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

package controllers.register.adviser

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAllowAccessProvider, FakeAuthAction}
import models.UpdateMode
import play.api.test.Helpers._
import views.html.alreadyDeletedAdviser

class AdviserAlreadyDeletedControllerSpec extends ControllerSpecBase {

  val onwardRoute: String = controllers.routes.PsaDetailsController.onPageLoad().url

  def controller(dataRetrievalAction: DataRetrievalAction = getDirector) =
    new AdviserAlreadyDeletedController(
      frontendAppConfig,
      messagesApi,
      FakeAllowAccessProvider(),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  def viewAsString(): String = alreadyDeletedAdviser(
    frontendAppConfig,
    onwardRoute
  )(fakeRequest, messages).toString

  "AlreadyDeleted Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(UpdateMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

  }
}
