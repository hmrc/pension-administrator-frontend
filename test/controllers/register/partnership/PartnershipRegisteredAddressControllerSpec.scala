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

package controllers.register.partnership

import audit.testdoubles.StubSuccessfulAuditService
import connectors.cache.FakeUserAnswersCacheConnector
import controllers.actions._
import controllers.address.NonUKAddressControllerDataMocks
import forms.address.NonUKAddressFormProvider
import identifiers.register.BusinessNameId
import identifiers.register.partnership.PartnershipRegisteredAddressId
import models._
import org.scalatest.concurrent.ScalaFutures
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import utils.FakeNavigator
import viewmodels.Message
import viewmodels.address.ManualAddressViewModel
import views.html.address.nonukAddress

import scala.concurrent.{ExecutionContext, Future}

class PartnershipRegisteredAddressControllerSpec extends NonUKAddressControllerDataMocks with ScalaFutures {

  val formProvider = new NonUKAddressFormProvider(countryOptions)
  val form: Form[Address] = formProvider("error.country.invalid")
  val fakeAuditService = new StubSuccessfulAuditService()
  private val partnershipName = "Test Partnership Name"

  val view: nonukAddress = app.injector.instanceOf[nonukAddress]

  def controller(dataRetrievalAction: DataRetrievalAction = getPartnership) =
    new PartnershipRegisteredAddressController(
      frontendAppConfig,
      FakeUserAnswersCacheConnector,
      fakeRegistrationConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      formProvider,
      countryOptions,
      controllerComponents,
      view
    )

  private def viewModel = ManualAddressViewModel(
    controllers.register.partnership.routes.PartnershipRegisteredAddressController.onSubmit(),
    countryOptions.options,
    Message("partnershipRegisteredNonUKAddress.title"),
    Message("partnershipRegisteredNonUKAddress.heading", partnershipName),
    None,
    Some(Message("partnershipRegisteredNonUKAddress.hintText"))
  )

  private def viewAsString(form: Form[?] = form) : String =
    view(
      form,
      viewModel
    )(fakeRequest, messages).toString()

  override val registrationInfo = RegistrationInfo(
    RegistrationLegalStatus.Partnership,
    sapNumber,
    noIdentifier = false,
    RegistrationCustomerType.NonUK,
    None,
    None
  )

  override def fakeRegistrationConnector: FakeRegistrationConnector = new FakeRegistrationConnector {
    override def registerWithNoIdOrganisation
    (name: String, address: Address, legalStatus: RegistrationLegalStatus)
        (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[RegistrationInfo] = Future.successful(registrationInfo)
  }


  "Partnership Registered Address Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller().onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "populate the view correctly on a GET when the question has previously been answered" in {
      val validData = Json.obj(
        BusinessNameId.toString -> "Test Partnership Name",
        PartnershipRegisteredAddressId.toString -> Address("value 1", "value 2", None, None, None, "IN").toTolerantAddress)
      val getRelevantData = new FakeDataRetrievalAction(Some(validData))

      val result = controller(getRelevantData).onPageLoad(NormalMode)(fakeRequest)

      contentAsString(result) mustBe viewAsString(form.fill(Address("value 1", "value 2", None, None, None, "IN")))
    }

    "redirect to the next page when valid data is submitted" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(
        ("addressLine1", "value 1"),
        ("addressLine2", "value 2"),
        "country" -> "IN"
      )

      val result = controller().onSubmit(NormalMode)(postRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
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
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
        "POST" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody()
          val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
      }
    }

  }
}
