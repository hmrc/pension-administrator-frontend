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

package controllers.deregister


import connectors.{DeregistrationConnector, FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.deregister.ConfirmStopBeingPsaFormProvider
import identifiers.deregister.ConfirmStopBeingPsaId
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.UserAnswers
import utils.countryOptions.CountryOptions
import views.html.deregister.confirmStopBeingPsa

import scala.concurrent.{ExecutionContext, Future}

class ConfirmStopBeingPsaControllerSpec extends ControllerSpecBase {

  import ConfirmStopBeingPsaControllerSpec._

  "ConfirmStopBeingPsaController" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad()(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "redirect to the next page on a successful POST" in {
      val result = controller().onSubmit()(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }
  }

}

object ConfirmStopBeingPsaControllerSpec extends ControllerSpecBase with MockitoSugar {

  private def onwardRoute = controllers.routes.PsaDetailsController.onPageLoad()

  val dataRetrievalAction = new FakeDataRetrievalAction(None)

  val formProvider = new ConfirmStopBeingPsaFormProvider
  val form: Form[Boolean] = formProvider()

  private val postRequest: FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest().withFormUrlEncodedBody(("value", "true"))

  val countryOptions = new CountryOptions(environment, frontendAppConfig)

  private def testData() = Json.obj(ConfirmStopBeingPsaId.toString -> true)

  private def fakeRegistrationConnector = new DeregistrationConnector {
    override def stopBeingPSA(utr: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsValue] = {


      Future.successful(Json.obj())
    }
  }

  private def controller(
                          dataRetrievalAction: DataRetrievalAction = getEmptyData,
                          dataCacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
                        ) =
    new ConfirmStopBeingPsaController(
      frontendAppConfig,
      FakeAuthAction,
      messagesApi,
      formProvider,
      dataCacheConnector,
      dataRetrievalAction,
      new DataRequiredActionImpl
    )

  private def viewAsString(): String =
    confirmStopBeingPsa(frontendAppConfig, form, "")(fakeRequest, messages).toString

}


