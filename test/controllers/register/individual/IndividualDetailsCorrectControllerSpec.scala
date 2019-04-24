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

package controllers.register.individual

import connectors.{FakeUserAnswersCacheConnector, RegistrationConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.individual.IndividualDetailsCorrectFormProvider
import identifiers.register.RegistrationInfoId
import identifiers.register.individual.{IndividualAddressId, IndividualDetailsCorrectId, IndividualDetailsId}
import models.RegistrationCustomerType.UK
import models.RegistrationLegalStatus.Individual
import models.requests.AuthenticatedRequest
import models.{RegistrationInfo, _}
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.{Request, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.FakeNavigator
import utils.countryOptions.CountryOptions
import views.html.register.individual.individualDetailsCorrect

import scala.concurrent.{ExecutionContext, Future}

class IndividualDetailsCorrectControllerSpec extends ControllerSpecBase with MockitoSugar {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val formProvider = new IndividualDetailsCorrectFormProvider()
  private val form = formProvider()

  private val nino = "test-nino"
  private val sapNumber = "test-sap-number"

  private val fakeAuthAction = new AuthAction {
    override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] =
      block(AuthenticatedRequest(request, "id", PSAUser(UserType.Individual, Some(nino), isExistingPSA = false, None)))
  }

  private val registrationInfo = RegistrationInfo(
    RegistrationLegalStatus.Individual,
    sapNumber,
    noIdentifier = false,
    RegistrationCustomerType.UK,
    Some(RegistrationIdType.Nino),
    Some(nino)
  )

  private val individual = TolerantIndividual(
    Some("John"),
    Some("T"),
    Some("Doe")
  )

  private val address = TolerantAddress(
    Some("Building Name"),
    Some("1 Main Street"),
    Some("Some Village"),
    Some("Some Town"),
    Some("GB"),
    Some("ZZ1 1ZZ")
  )

  private object FakeRegistrationConnector extends FakeRegistrationConnector {
    override def registerWithIdIndividual
    (nino: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[IndividualRegistration] = {

      Future.successful(IndividualRegistration(IndividualRegisterWithIdResponse(individual, address), registrationInfo))
    }
  }

  private object ExceptionThrowingRegistrationConnector extends FakeRegistrationConnector {
    override def registerWithIdIndividual
    (nino: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[IndividualRegistration] = {
      throw new Exception("registerWithIdIndividual should not be called in this test")
    }
  }

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData, registrationConnector: RegistrationConnector = FakeRegistrationConnector) =
    new IndividualDetailsCorrectController(
      new FakeNavigator(desiredRoute = onwardRoute),
      frontendAppConfig,
      messagesApi,
      FakeUserAnswersCacheConnector,
      fakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      registrationConnector,
      new CountryOptions(environment, frontendAppConfig)
    )

  private def viewAsString(form: Form[_] = form) =
    individualDetailsCorrect(
      frontendAppConfig,
      form,
      NormalMode,
      individual,
      address,
      new CountryOptions(environment, frontendAppConfig)
    )(fakeRequest, messages).toString

  "IndividualDetailsCorrect Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "save the individual and address details on a GET and individual name to PSA Name cache" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK

      FakeUserAnswersCacheConnector.verify(IndividualDetailsId, individual)
      FakeUserAnswersCacheConnector.verify(IndividualAddressId, address)
      FakeUserAnswersCacheConnector.verify(RegistrationInfoId, registrationInfo)
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(IndividualDetailsCorrectId.toString -> true)
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(true))
    }

    "use existing individual details, address data and registrationInfo if present" in {
      val registrationInfo = RegistrationInfo(Individual, "", noIdentifier=false, UK, Some(RegistrationIdType.Nino), Some("AB121212C"))
      val data = Json.obj(
        IndividualDetailsId.toString -> individual,
        IndividualAddressId.toString -> address,
        RegistrationInfoId.toString -> registrationInfo
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(data))

      val result = controller(getRelevantData, ExceptionThrowingRegistrationConnector).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
    }

    "re-register if existing registrationInfo not present" in {
      FakeUserAnswersCacheConnector.reset()
      val data = Json.obj(
        IndividualDetailsId.toString -> individual,
        IndividualAddressId.toString -> address
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(data))
      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)
      status(result) mustBe OK
      FakeUserAnswersCacheConnector.verify(IndividualDetailsId, individual)
      FakeUserAnswersCacheConnector.verify(IndividualAddressId, address)
      FakeUserAnswersCacheConnector.verify(RegistrationInfoId, registrationInfo)
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "save the user answer when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      FakeUserAnswersCacheConnector.verify(IndividualDetailsCorrectId, true)
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val data = Json.obj(
        IndividualDetailsId.toString -> individual,
        IndividualAddressId.toString -> address
      )
      val getRelevantData = new FakeDataRetrievalAction(Some(data))
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller(getRelevantData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST when invalid data is submitted and no individual details or address are found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid"))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

  }

}
