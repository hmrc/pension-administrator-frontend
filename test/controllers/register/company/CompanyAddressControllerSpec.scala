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
import models.{Address, NormalMode, TolerantAddress}
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import utils.FakeNavigator
import views.html.register.company.companyAddress

class CompanyAddressControllerSpec extends ControllerSpecBase {

  val onwardRoute = Call("GET", "/")

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new CompanyAddressController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      new FakeNavigator(desiredRoute = onwardRoute),
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  val address = TolerantAddress(
    Some("add1"), Some("add2"),
    None, None,
    Some("NE11NE"), Some("GB")
  )

  val validData = new FakeDataRetrievalAction(Some(Json.obj(CompanyAddressId.toString -> address)))

  def viewAsString() = companyAddress(frontendAppConfig, address)(fakeRequest, messages).toString

  "CompanyAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(validData).onPageLoad(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page valid data is submitted" in {
      val result = controller(validData).onSubmit(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
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
