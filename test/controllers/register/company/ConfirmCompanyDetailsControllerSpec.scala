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

import connectors.{UserAnswersCacheConnector, FakeUserAnswersCacheConnector, RegistrationConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.company.CompanyAddressFormProvider
import identifiers.register.company._
import identifiers.register.{BusinessTypeId, RegistrationInfoId}
import models.register.BusinessType.{BusinessPartnership, LimitedCompany}
import models.{BusinessDetails, _}
import org.scalatest.mockito.MockitoSugar
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import utils.{FakeNavigator, UserAnswers}
import views.html.register.company.confirmCompanyDetails

import scala.concurrent.{ExecutionContext, Future}

class ConfirmCompanyDetailsControllerSpec extends ControllerSpecBase {

  import ConfirmCompanyDetailsControllerSpec._

  "CompanyAddress Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "correctly map the Business Type to Organisation Type for the call to API4" in {

      val companyName = "MyPartnership"

      val data = Json.obj(
        BusinessTypeId.toString -> BusinessPartnership.toString,
        BusinessDetailsId.toString -> BusinessDetails(companyName, Some(validBusinessPartnershipUtr))
      )
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(companyName, testBusinessPartnershipAddress)
    }

    "redirect to the next page when the UTR is invalid" in {
      val data = Json.obj(
        BusinessTypeId.toString -> LimitedCompany.toString,
        BusinessDetailsId.toString -> BusinessDetails("MyCo", Some(invalidUtr))
      )
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.register.company.routes.CompanyNotFoundController.onPageLoad().url)
    }

    "data is removed on page load" in {
      val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))

      controller(dataRetrievalAction).onPageLoad(NormalMode)(postRequest)

      FakeUserAnswersCacheConnector.verifyRemoved(ConfirmCompanyAddressId)
    }

    "valid data is submitted" when {
      "yes" which {
        "upsert address, organisation name from api response and save company name to PSA Name cache" in {
          val dataCacheConnector = FakeUserAnswersCacheConnector

          val info = RegistrationInfo(
            RegistrationLegalStatus.LimitedCompany,
            sapNumber,
            false,
            RegistrationCustomerType.UK,
            RegistrationIdType.UTR,
            validLimitedCompanyUtr
          )

          val expectedJson =
            UserAnswers(data)
              .set(ConfirmCompanyAddressId)(testLimitedCompanyAddress)
              .flatMap(_.set(RegistrationInfoId)(info))
              .asOpt
              .value
              .json

          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

          val result = controller(dataRetrievalAction, dataCacheConnector).onSubmit(NormalMode)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
          dataCacheConnector.lastUpsert.value mustBe expectedJson
        }
      }
      "no" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))

        val result = controller(dataRetrievalAction).onSubmit(NormalMode)(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.company.routes.CompanyUpdateDetailsController.onPageLoad().url)
      }
    }

    "redirect to Session Expired" when {
      "GET" when {
        "no business details data is found" in {
          val data = Json.obj(
            BusinessTypeId.toString -> LimitedCompany.toString
          )

          val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
          val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "no business type data is found" in {
          val data = Json.obj(
            BusinessDetailsId.toString -> BusinessDetails("MyCo", Some(validBusinessPartnershipUtr))
          )

          val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
          val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }

        "no existing data is found" in {
          val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
      "POST" when {
        "no business details data is found" in {
          val data = Json.obj(
            BusinessTypeId.toString -> LimitedCompany.toString
          )

          val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
          val result = controller(dataRetrievalAction).onSubmit(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "no business type data is found" in {
          val data = Json.obj(
            BusinessDetailsId.toString -> BusinessDetails("MyCo", Some(validLimitedCompanyUtr))
          )

          val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
          val result = controller(dataRetrievalAction).onSubmit(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }

        "no existing data is found" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
          val result = controller(dontGetAnyData).onSubmit(NormalMode)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
      }
    }

  }

}

object ConfirmCompanyDetailsControllerSpec extends ControllerSpecBase with MockitoSugar {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val testLimitedCompanyAddress = TolerantAddress(
    Some("Some Building"),
    Some("1 Some Street"),
    Some("Some Village"),
    Some("Some Town"),
    Some("ZZ1 1ZZ"),
    Some("UK")
  )

  private val testBusinessPartnershipAddress = TolerantAddress(
    Some("Some Other Building"),
    Some("2 Some Street"),
    Some("Some Village"),
    Some("Some Town"),
    Some("ZZ1 1ZZ"),
    Some("UK")
  )

  private val validLimitedCompanyUtr = "1234567890"
  private val validBusinessPartnershipUtr = "0987654321"
  private val invalidUtr = "INVALID"
  private val sapNumber = "test-sap-number"

  val companyDetails = BusinessDetails("MyCompany", Some(validLimitedCompanyUtr))
  val organisation = Organisation("MyOrganisation", OrganisationTypeEnum.CorporateBody)

  private val data = Json.obj(
    BusinessTypeId.toString -> LimitedCompany.toString,
    BusinessDetailsId.toString -> companyDetails
  )

  val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))

  val formProvider = new CompanyAddressFormProvider

  val form: Form[Boolean] = formProvider()

  private def fakeRegistrationConnector = new RegistrationConnector {
    override def registerWithIdOrganisation
    (utr: String, organisation: Organisation, legalStatus: RegistrationLegalStatus)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganizationRegistration] = {

      val info = RegistrationInfo(
        RegistrationLegalStatus.LimitedCompany,
        sapNumber,
        false,
        RegistrationCustomerType.UK,
        RegistrationIdType.UTR,
        utr
      )

      if (utr == validLimitedCompanyUtr && organisation.organisationType == OrganisationTypeEnum.CorporateBody) {
        Future.successful(OrganizationRegistration(OrganizationRegisterWithIdResponse(organisation, testLimitedCompanyAddress), info))
      }
      else if (utr == validBusinessPartnershipUtr && organisation.organisationType == OrganisationTypeEnum.Partnership) {
        Future.successful(OrganizationRegistration(OrganizationRegisterWithIdResponse(organisation, testBusinessPartnershipAddress), info))
      }
      else {
        Future.failed(new NotFoundException(s"Unknown UTR: $utr"))
      }
    }

    //noinspection NotImplementedCode
    def registerWithIdIndividual
    (nino: String)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[IndividualRegistration] = ???
  }
  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData, dataCacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector) =
    new ConfirmCompanyDetailsController(
      frontendAppConfig,
      messagesApi,
      dataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeRegistrationConnector,
      formProvider
    )

  private def viewAsString(companyName: String = companyDetails.companyName, address: TolerantAddress = testLimitedCompanyAddress): String =
    confirmCompanyDetails(frontendAppConfig, form, address, companyName)(fakeRequest, messages).toString

}
