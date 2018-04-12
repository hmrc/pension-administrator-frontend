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

package controllers.register.company

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.company.CompanyAddressId
import models.TolerantAddress
import play.api.libs.json.Json
import play.api.test.Helpers._
import views.html.register.company.companyAddress

class CompanyAddressControllerSpec extends ControllerSpecBase {

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new CompanyAddressController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  def viewAsString() = companyAddress(frontendAppConfig)(fakeRequest, messages).toString

  "CompanyAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(
        new FakeDataRetrievalAction(Some(Json.obj(
          CompanyAddressId.toString -> TolerantAddress(Some("AddressLine1"), Some("Add2"), Some("Add3"), None, Some("NE11NE"), Some("GB"))
        )))
      ).onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to Session Expired" when {
      "companyAddress cannot be retrieved" in {
        val result = controller(getEmptyData).onPageLoad(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)

      }
    }
  }
}
