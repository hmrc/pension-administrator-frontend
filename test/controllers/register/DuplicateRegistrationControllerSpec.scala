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

package controllers.register

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.company.BusinessDetailsId
import models.{BusinessDetails, NormalMode}
import play.api.libs.json.Json
import play.api.test.Helpers._
import views.html.register.duplicateRegistration

class DuplicateRegistrationControllerSpec extends ControllerSpecBase {

  private val companyName = "test company name"

  def controller() = new DuplicateRegistrationController(
    frontendAppConfig, messagesApi, FakeAuthAction, FakeAllowAccessProvider(), getEmptyData)

  private def viewAsString() = duplicateRegistration(frontendAppConfig)(fakeRequest, messages).toString

  private def dataRetrievalAction(fields: (String, Json.JsValueWrapper)*): DataRetrievalAction = {
    val data = Json.obj(fields: _*)
    new FakeDataRetrievalAction(Some(data))
  }

  "DuplicateRegistration Controller" must {
    "return OK and the correct view for a GET with the correct company name displayed" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }
  }
}
