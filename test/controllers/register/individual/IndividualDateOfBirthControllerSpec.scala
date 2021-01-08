/*
 * Copyright 2021 HM Revenue & Customs
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

import java.time.LocalDate

import connectors.cache.FakeUserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.individual.IndividualDateOfBirthFormProvider
import identifiers.register.AreYouInUKId
import identifiers.register.individual.{IndividualAddressId, IndividualDateOfBirthId, IndividualDetailsId}
import models._
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc.Call
import play.api.test.Helpers._
import services.RegistrationService

import utils.{DateHelper, FakeNavigator}
import views.html.register.individual.individualDateOfBirth

import scala.concurrent.Future

class IndividualDateOfBirthControllerSpec extends ControllerSpecBase with MockitoSugar {


  private val formProvider = new IndividualDateOfBirthFormProvider()
  private val form = formProvider()

  val registrationService: RegistrationService = mock[RegistrationService]

  val sapNumber = "test-sap-number"

  val registrationInfo = RegistrationInfo(
    RegistrationLegalStatus.Individual,
    sapNumber,
    noIdentifier = false,
    RegistrationCustomerType.NonUK,
    None,
    None
  )

  val view: individualDateOfBirth = app.injector.instanceOf[individualDateOfBirth]

  private val testAnswer = LocalDate.now()

  "IndividualDateOfBirth Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(IndividualDateOfBirthId.toString -> testAnswer)
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(testAnswer))
    }

    "redirect to the next page when valid data is submitted for UK" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("dateOfBirth.day", "9"),
        ("dateOfBirth.month", "6"),
        ("dateOfBirth.year", "1902"))

      val result = controller(
        dataRetrievalAction = getRequiredDataForRegistration(isUk = true)).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      verify(registrationService, never()).registerWithNoIdIndividual(any(), any(), any(), any())(any(), any())
    }

    "call the registration and redirect to the next page when valid data is submitted for nonUK" in {
      when(registrationService.registerWithNoIdIndividual(
        any(), any(), any(), any())(any(), any())).thenReturn(Future.successful(registrationInfo))
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("dateOfBirth.day", "9"),
        ("dateOfBirth.month", "6"),
        ("dateOfBirth.year", "2002"))

      val result = controller(dataRetrievalAction = getRequiredDataForRegistration()).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
      verify(registrationService, atLeastOnce()).registerWithNoIdIndividual(any(), any(), any(), any())(any(), any())
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("dateOfBirth", ""))
      val boundForm = form.bind(Map("dateOfBirth" -> ""))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired for a GET if no existing data is found" in {
      val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a POST if no existing data is found" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("dateOfBirth", DateHelper.formatDate(testAnswer)))
      val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  def getRequiredDataForRegistration(isUk : Boolean = false): FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      AreYouInUKId.toString -> isUk,
      IndividualDetailsId.toString ->
          TolerantIndividual(Some("TestFirstName"), None, Some("TestLastName")),
      IndividualAddressId.toString ->
          Address("value 1", "value 2", None, None, None, "IN").toTolerantAddress
    )))

  def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new IndividualDateOfBirthController(frontendAppConfig,
      FakeUserAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      registrationService,
      controllerComponents,
      view
    )


  def viewAsString(form: Form[_] = form): String = view(form, NormalMode)(fakeRequest, messages).toString
}
