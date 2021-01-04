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

import audit.testdoubles.StubSuccessfulAuditService
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.address.NonUKAddressFormProvider
import identifiers.register.RegistrationInfoId
import identifiers.register.individual.{IndividualAddressId, IndividualDetailsId}
import models._
import org.mockito.Matchers
import org.mockito.Matchers._
import org.mockito.Mockito.{atLeastOnce, verify, when}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, FakeNavigator}
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.nonukAddress

import scala.concurrent.Future

class IndividualRegisteredAddressControllerSpec extends ControllerSpecBase with ScalaFutures with BeforeAndAfter{

  def onwardRoute: Call = controllers.routes.IndexController.onPageLoad()

  def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val formProvider = new NonUKAddressFormProvider(countryOptions)
  val form: Form[Address] = formProvider("error.country.invalid")
  val fakeAuditService = new StubSuccessfulAuditService()
  val individualName = "TestFirstName TestLastName"
  val sapNumber = "test-sap-number"
  val registrationInfo = RegistrationInfo(
    legalStatus = RegistrationLegalStatus.Individual,
    sapNumber = sapNumber,
    noIdentifier = false,
    customerType = RegistrationCustomerType.NonUK,
    idType = None,
    idNumber = None
  )


  def controller(dataRetrievalAction: DataRetrievalAction = getIndividual,
                 userAnswersCacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector) =
    new IndividualRegisteredAddressController(
      frontendAppConfig,
      userAnswersCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      countryOptions,
      stubMessagesControllerComponents(),
      view
    )

  val view: nonukAddress = app.injector.instanceOf[nonukAddress]

  private def viewModel = ManualAddressViewModel(
    routes.IndividualRegisteredAddressController.onSubmit(NormalMode),
    countryOptions.options,
    Message("individualRegisteredNonUKAddress.title"),
    Message("individualRegisteredNonUKAddress.heading", individualName),
    None,
    Some(Message("individualRegisteredNonUKAddress.hintText"))
  )

  private def viewAsString(form: Form[_] = form) =
    view(
      form,
      viewModel
    )(fakeRequest, messages).toString()

  "IndividualRegisteredAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(
        IndividualDetailsId.toString -> TolerantIndividual(Some("fName"), Some("mName"), Some("fName")),
        IndividualAddressId.toString -> Address("value 1", "value 2", None, None, None, "IN").toTolerantAddress)
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(Address("value 1", "value 2", None, None, None, "IN")))
    }

    "redirect to the next page when valid data with non uk country is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        "country" -> "IN"
      )

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)

      FakeUserAnswersCacheConnector.verify(IndividualAddressId, Address("value 1", "value 2", None, None, None, "IN").toTolerantAddress)
    }

    "redirect to the next page when valid data with uk as country is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        "country" -> "GB"
      )

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)

      FakeUserAnswersCacheConnector.verify(IndividualAddressId, Address("value 1", "value 2", None, None, None, "GB").toTolerantAddress)
    }


    "remove registration info when non eea country is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        "country" -> "IN"
      )

      val validConnectorCallResult = Json.obj("test" -> "test")
      val userAnswersCacheConnector = mock[UserAnswersCacheConnector]
      when(userAnswersCacheConnector.remove(any(),any())(any(),any())).thenReturn(Future.successful(validConnectorCallResult))
      when(userAnswersCacheConnector.save(any(),any(), any())(any(),any(), any())).thenReturn(Future.successful(validConnectorCallResult))

      val result = controller(userAnswersCacheConnector = userAnswersCacheConnector).onSubmit(NormalMode)(postRequest)
      whenReady(result) {_=>
        verify(userAnswersCacheConnector, atLeastOnce()).remove(any(),Matchers.eq(RegistrationInfoId))(any(),any())
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "invalid value"))
      val boundForm = form.bind(Map("value" -> "invalid value"))

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe BAD_REQUEST
      contentAsString(result) mustBe viewAsString(boundForm)
    }

    "redirect to Session Expired" when {
      "no existing data is found" when {
        "GET" in {
          val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody()
          val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

  }
}
