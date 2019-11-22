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

import config.FrontendAppConfig
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register._
import models._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeNavigator
import views.html.register.declarationFitAndProper

class DeclarationFitAndProperControllerSpec extends ControllerSpecBase with MockitoSugar {

  import DeclarationFitAndProperControllerSpec._

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "DeclarationFitAndProperController" when {

    "calling onPageLoad" must {

      "return OK and the correct view" in {
        val result = controller().onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe OK
        contentAsString(result) mustBe viewAsString
      }

      "redirect to Session Expired if no cached data is found" in {
        val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
      }
    }

    "calling onSubmit" must {

      "redirect to Session Expired" when {
        "no cached data is found" in {
          val result = controller(dontGetAnyData).onClickAgree(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }

      }

      "save the answer and PSA Subscription response on a valid request" in {
        val result = controller().onClickAgree(NormalMode)(validRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
        FakeUserAnswersCacheConnector.verify(DeclarationFitAndProperId, value = true)
      }

    }
  }
}

object DeclarationFitAndProperControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val onwardRoute = controllers.routes.IndexController.onPageLoad()
  private val fakeNavigator = new FakeNavigator(desiredRoute = onwardRoute)
  private val validRequest = fakeRequest.withFormUrlEncodedBody("agree" -> "agreed")
  val businessDetails = BusinessDetails("MyCompany", Some("1234567890"))
  val email = "test@test.com"
  val businessName = "MyCompany"

  private val appConfig = app.injector.instanceOf[FrontendAppConfig]

  val view: declarationFitAndProper = app.injector.instanceOf[declarationFitAndProper]

  private def controller(
                          dataRetrievalAction: DataRetrievalAction = getEmptyData,
                          fakeUserAnswersCacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
                        ) =
    new DeclarationFitAndProperController(
      appConfig,
      messagesApi,
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeNavigator,
      fakeUserAnswersCacheConnector,
      stubMessagesControllerComponents(),
      view
    )

  private def viewAsString = view()(fakeRequest, messages).toString

}
