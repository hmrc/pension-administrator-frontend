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

package controllers.register.individual

import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.individual.IndividualAddressId
import models.{Address, NormalMode}
import play.api.libs.json.Json
import play.api.test.Helpers._
import utils.countryOptions.CountryOptions
import views.html.register.individual.outsideEuEea

class OutsideEuEeaControllerSpec extends ControllerSpecBase {

  private val countryOptions: CountryOptions = new CountryOptions(environment, frontendAppConfig)

  def controller(dataRetrievalAction: DataRetrievalAction = validData) =
    new OutsideEuEeaController(
      frontendAppConfig,
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      countryOptions,
      controllerComponents,
      view
    )

  val view: outsideEuEea = app.injector.instanceOf[outsideEuEea]

  def validData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      IndividualAddressId.toString -> Address(
        "value 1",
        "value 2",
        None,
        None,
        Some("NE1 1NE"),
        "AF"
      ).toTolerantAddress
    )))

  val country = "Afghanistan"

  "OutsideEuEea Controller" must {
    "return 200 and correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)
      status(result) mustBe OK
      contentAsString(result) mustBe view(country)(fakeRequest, messages).toString
    }

    "redirect to Session Expired on a GET request if no cached data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }
  }
}
