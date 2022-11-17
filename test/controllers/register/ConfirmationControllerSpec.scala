/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.cache.UserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import identifiers.register.{PsaSubscriptionResponseId, RegisterAsBusinessId}
import models._
import models.register.PsaSubscriptionResponse
import models.requests.DataRequest
import org.mockito.ArgumentMatchers._
import org.mockito.MockitoSugar
import play.api.mvc.Results._
import play.api.test.Helpers._
import utils.UserAnswers
import views.html.register.confirmation

import scala.concurrent.Future

class ConfirmationControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val psaId: String = "A1234567"
  private val psaName: String = "psa name"
  private val psaEmail: String = "test@test.com"
  private val fakeUserAnswersCacheConnector: UserAnswersCacheConnector = mock[UserAnswersCacheConnector]
  private def onwardRoute = controllers.routes.LogoutController.onPageLoad
  private val psaUser = PSAUser(UserType.Individual, None, isExistingPSA = false, None, None, "")

  val view: confirmation = app.injector.instanceOf[confirmation]

  "Confirmation Controller" must {

    "return OK and the correct view for a GET" in {
      val data = UserAnswers().set(PsaSubscriptionResponseId)(PsaSubscriptionResponse(psaId)).asOpt.value
          .set(RegisterAsBusinessId)(true).asOpt.value
          .registrationInfo(RegistrationInfo(RegistrationLegalStatus.LimitedCompany, "", false, RegistrationCustomerType.UK, None, None))
          .businessName(psaName)
          .companyEmail(psaEmail)

      when(fakeUserAnswersCacheConnector.removeAll(any())(any(), any())) thenReturn Future.successful(Ok)
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data.json))

      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
      verify(fakeUserAnswersCacheConnector, times(1)).removeAll(any())(any(), any())
    }

    "return OK and the correct view for a GET for Individual" in {
      val data = UserAnswers().set(PsaSubscriptionResponseId)(PsaSubscriptionResponse(psaId)).asOpt.value
        .registrationInfo(RegistrationInfo(RegistrationLegalStatus.Individual, "", false, RegistrationCustomerType.UK, None, None))
        .individualDetails(TolerantIndividual(Some("psa"),None,Some("name")))
        .individualEmail(psaEmail)
       reset(fakeUserAnswersCacheConnector)
      when(fakeUserAnswersCacheConnector.removeAll(any())(any(), any())) thenReturn Future.successful(Ok)
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data.json))

      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsStringOther()
      verify(fakeUserAnswersCacheConnector, times(1)).removeAll(any())(any(), any())
    }

    "redirect to Session Expired on a GET when no data exists" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
    }

    "redirect to the next page on a successful POST" in {
      val result = controller().onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new ConfirmationController(
      messagesApi,
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeUserAnswersCacheConnector,
      controllerComponents,
      view
    )

  private def viewAsString() =
    view(psaId, psaName, psaEmail,true)(DataRequest(fakeRequest, "cacheId", psaUser, UserAnswers()), messagesApi.preferred(fakeRequest)).toString

  private def viewAsStringOther() =
    view(psaId, psaName, psaEmail,false)(DataRequest(fakeRequest, "cacheId", psaUser, UserAnswers()), messagesApi.preferred(fakeRequest)).toString
}
