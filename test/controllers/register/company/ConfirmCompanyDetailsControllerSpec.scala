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

package controllers.register.company

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.AddressFormProvider
import forms.register.company.CompanyAddressFormProvider
import identifiers.register.company._
import identifiers.register.{BusinessNameId, BusinessTypeId, BusinessUTRId, RegistrationInfoId}
import models._
import models.register.BusinessType.{BusinessPartnership, LimitedCompany}
import org.scalatest.BeforeAndAfterEach
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import utils.countryOptions.CountryOptions
import utils.{AddressHelper, UserAnswers}
import views.html.register.company.confirmCompanyDetails

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class ConfirmCompanyDetailsControllerSpec extends ControllerSpecBase with BeforeAndAfterEach {

  override protected def beforeEach(): Unit = {
    FakeUserAnswersCacheConnector.reset()
  }

  val view: confirmCompanyDetails = app.injector.instanceOf[confirmCompanyDetails]

  private val validLimitedCompanyUtr = "1234567890"
  private val validBusinessPartnershipUtr = "0987654321"
  private val invalidUtr = "INVALID"
  private val sapNumber = "test-sap-number"

  val regInfo: RegistrationInfo = RegistrationInfo(
    RegistrationLegalStatus.LimitedCompany,
    sapNumber,
    noIdentifier = false,
    RegistrationCustomerType.UK,
    Some(RegistrationIdType.UTR),
    Some(validLimitedCompanyUtr)
  )

  private val testLimitedCompanyAddress = TolerantAddress(
    Some("Some Building"),
    Some("1 Some Street"),
    Some("Some Village"),
    Some("Some Town"),
    Some("ZZ1 1ZZ"),
    Some("GB")
  )

  private val testBusinessPartnershipAddress = TolerantAddress(
    Some("Some Other Building"),
    Some("2 Some Street"),
    Some("Some Village"),
    Some("Some Town"),
    Some("ZZ1 1ZZ"),
    Some("GB")
  )

  val organisation = Organisation("MyOrganisation", OrganisationType.CorporateBody)

  private val data = Json.obj(
    BusinessTypeId.toString -> LimitedCompany.toString,
    BusinessNameId.toString -> companyName,
    BusinessUTRId.toString -> validLimitedCompanyUtr
  )

  private val dataForPost = Json.obj(
    BusinessTypeId.toString -> LimitedCompany.toString,
    BusinessNameId.toString -> companyName,
    ConfirmCompanyAddressId.toString -> testLimitedCompanyAddress,
    RegistrationInfoId.toString -> regInfo
  )

  val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
  val dataRetrievalActionForPost = new FakeDataRetrievalAction(Some(dataForPost))

  val formProvider = new CompanyAddressFormProvider

  val form: Form[Boolean] = formProvider()

  val countryOptions = new CountryOptions(environment, frontendAppConfig)

  val addressFormProvider = new AddressFormProvider(countryOptions)
  val addressForm: Form[Address] = addressFormProvider()
  val addressHelper: AddressHelper = inject[AddressHelper]

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
        BusinessNameId.toString -> companyName,
        BusinessUTRId.toString -> validBusinessPartnershipUtr
      )
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(companyName, testBusinessPartnershipAddress)
    }

    "redirect to the next page when the UTR is invalid" in {
      val data = Json.obj(
        BusinessTypeId.toString -> LimitedCompany.toString,
        BusinessNameId.toString -> companyName,
        BusinessUTRId.toString -> invalidUtr
      )
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.register.company.routes.CompanyNotFoundController.onPageLoad().url)
    }

    "data is saved on page load" in {
      val dataCacheConnector = FakeUserAnswersCacheConnector

      val expectedJson =
        UserAnswers(data)
          .set(ConfirmCompanyAddressId)(testLimitedCompanyAddress)
          .flatMap(_.set(RegistrationInfoId)(regInfo))
          .asOpt
          .value
          .json
      val result = controller(dataRetrievalAction, dataCacheConnector).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      dataCacheConnector.lastUpsert.value mustBe expectedJson
    }

    "valid data is submitted" when {
      "yes must redirect to next page" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

          val result = controller(dataRetrievalActionForPost).onSubmit()(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)

        }
      "yes must redirect to the address page when a field is missing" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

        val testLimitedCompanyAddress = TolerantAddress(
          Some("Some Building"),
          Some("1 Some Street"),
          Some("Some Village"),
          Some("Some Town"),
          Some("ZZ1 1ZZ"),
          Some("")
        )

        val dataForPost = Json.obj(
          BusinessTypeId.toString -> LimitedCompany.toString,
          BusinessNameId.toString -> companyName,
          ConfirmCompanyAddressId.toString -> testLimitedCompanyAddress,
          RegistrationInfoId.toString -> regInfo
        )

        val dataRetrievalAction = new FakeDataRetrievalAction(Some(dataForPost))

        val result = controller(dataRetrievalAction).onSubmit()(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.company.routes.AddressController.onPageLoad().toString)
      }

      "no must remove saved data from address and registration info ids" in {
        val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))

        val result = controller(dataRetrievalActionForPost).onSubmit()(postRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.register.company.routes.CompanyUpdateDetailsController.onPageLoad().url)
        FakeUserAnswersCacheConnector.verifyNot(ConfirmCompanyAddressId)
        FakeUserAnswersCacheConnector.verifyNot(RegistrationInfoId)
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
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
        "no business type data is found" in {
          val data = Json.obj(
            BusinessNameId.toString -> "MyCo",
            BusinessUTRId.toString -> validBusinessPartnershipUtr
          )

          val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
          val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }

        "no existing data is found" in {
          val result = controller(dontGetAnyData).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
      }
      "POST" when {
        "no business details data is found" in {
          val data = Json.obj(
            BusinessTypeId.toString -> LimitedCompany.toString
          )

          val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
          val result = controller(dataRetrievalAction).onSubmit()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
        "no business type data is found" in {
          val data = Json.obj(
            BusinessNameId.toString -> "MyCo",
            BusinessUTRId.toString -> validLimitedCompanyUtr
          )

          val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
          val result = controller(dataRetrievalAction).onSubmit()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }

        "no existing data is found" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))
          val result = controller(dontGetAnyData).onSubmit()(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad.url)
        }
      }
    }

  }

  private def onwardRoute: Call = controllers.register.company.routes.CompanyRegistrationTaskListController.onPageLoad()



  private def fakeRegistrationConnector = new FakeRegistrationConnector {
    override def registerWithIdOrganisation
    (utr: String, organisation: Organisation, legalStatus: RegistrationLegalStatus)
    (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[OrganizationRegistrationStatus] = {

      val info = RegistrationInfo(
        RegistrationLegalStatus.LimitedCompany,
        sapNumber,
        noIdentifier = false,
        RegistrationCustomerType.UK,
        Some(RegistrationIdType.UTR),
        Some(utr)
      )

      if (utr == validLimitedCompanyUtr && organisation.organisationType == OrganisationType.CorporateBody) {
        Future.successful(OrganizationRegistration(OrganizationRegisterWithIdResponse(organisation, testLimitedCompanyAddress), info))
      }
      else if (utr == validBusinessPartnershipUtr && organisation.organisationType == OrganisationType.Partnership) {
        Future.successful(OrganizationRegistration(OrganizationRegisterWithIdResponse(organisation, testBusinessPartnershipAddress), info))
      }
      else {
        Future.successful(OrganisationNotFound)
      }
    }

    override def registerWithNoIdIndividual(firstName: String, lastName: String, address: Address, dateOfBirth: LocalDate
                                           )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RegistrationInfo] =
      Future.failed(new NotFoundException(s"Unknown UTR"))
  }


  private def controller(
    dataRetrievalAction: DataRetrievalAction,
    dataCacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  ) =
    new ConfirmCompanyDetailsController(
      dataCacheConnector,
      FakeAuthAction,
      FakeAllowAccessProvider(config = frontendAppConfig),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeRegistrationConnector,
      formProvider,
      addressFormProvider,
      countryOptions,
      controllerComponents,
      view,
      addressHelper
    )

  private def viewAsString(companyName: String = companyName, address: TolerantAddress = testLimitedCompanyAddress): String =
    view(form, address, companyName, countryOptions)(fakeRequest, messages).toString

}
