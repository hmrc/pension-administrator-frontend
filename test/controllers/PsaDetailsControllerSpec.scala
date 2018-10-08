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

package controllers

import connectors.SubscriptionConnector
import controllers.actions.{DataRequiredActionImpl, DataRetrievalAction, FakeAuthAction, FakeDataRetrievalAction}
import identifiers.PsaId
import org.mockito.Matchers._
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers.{contentAsString, redirectLocation, status}
import utils.FakeCountryOptions
import utils.countryOptions.CountryOptions
import viewmodels.SuperSection
import views.html.psa_details
import utils.testhelpers.PsaSubscriptionBuilder._
import play.api.test.Helpers._

import scala.concurrent.Future

class PsaDetailsControllerSpec extends ControllerSpecBase with MockitoSugar {
  val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  val name = "testName"

  def call: Call = controllers.routes.CheckYourAnswersController.onSubmit()
  private val subscriptionConnector = mock[SubscriptionConnector]

  val validData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      PsaId.toString ->
        "S1234567890"
        )
      )
    )

  def controller(dataRetrievalAction: DataRetrievalAction = validData) =
    new PsaDetailsController(
      frontendAppConfig,
      messagesApi,
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      subscriptionConnector,
      countryOptions
    )

  "Psa details Controller" must {
    "return 200 and the correct view for a GET" in {

      when(subscriptionConnector.getSubscriptionDetails(any())(any(), any()))
        .thenReturn(Future.successful(psaSubscriptionMinimum))
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK

      val expectedViewContent = psa_details(frontendAppConfig, Seq(SuperSection(None, Seq())), name)(fakeRequest, messages).toString

      contentAsString(result) mustBe expectedViewContent
    }

    "redirect to Session Expired for a GET if not existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.SessionExpiredController.onPageLoad().url)
    }
  }
}
