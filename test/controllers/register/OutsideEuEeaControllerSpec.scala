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

import controllers.ControllerSpecBase
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction}
import play.api.test.Helpers._
import views.html.register.outsideEuEea
import views.html.unauthorised

class OutsideEuEeaControllerSpec extends ControllerSpecBase {

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new OutsideEuEeaController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  val organisationName = "Test company name"
  val country = "Canada"

  "OutsideEuEea Controller" must {
    "return 200 for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)
      status(result) mustBe OK
    }

    "return the correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)
      contentAsString(result) mustBe outsideEuEea(frontendAppConfig, organisationName, country)(fakeRequest, messages).toString
    }
  }
}
