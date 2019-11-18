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

package controllers.register.partnership

import connectors.FakeUserAnswersCacheConnector
import connectors.cache.UserAnswersCacheConnector
import controllers.ControllerSpecBase
import controllers.actions._
import forms.register.partnership.ConfirmPartnershipDetailsFormProvider
import identifiers.register.partnership.{ConfirmPartnershipDetailsId, PartnershipRegisteredAddressId}
import identifiers.register.{BusinessNameId, BusinessTypeId, BusinessUTRId, RegistrationInfoId}
import models.register.BusinessType.BusinessPartnership
import models.{BusinessDetails, _}
import play.api.data.Form
import play.api.libs.json._
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import utils.countryOptions.CountryOptions
import utils.{FakeNavigator, UserAnswers}
import views.html.register.partnership.confirmPartnershipDetails

import scala.concurrent.{ExecutionContext, Future}

class ConfirmPartnershipDetailsControllerSpec extends ControllerSpecBase {

  import ConfirmPartnershipDetailsControllerSpec._

  "ConfirmPartnershipDetails Controller" must {

    "return OK and the correct view for a GET" in {
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "correctly map the Business Type to Organisation Type for the call to API4" in {

      val partnershipName = "MyPartnership"

      val data = Json.obj(
        BusinessTypeId.toString -> BusinessPartnership.toString,
        BusinessNameId.toString -> partnershipName,
        BusinessUTRId.toString -> validBusinessPartnershipUtr
      )
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(partnershipName, testBusinessPartnershipAddress)
    }

    "redirect to the next page when the UTR is invalid" in {
      val data = Json.obj(
        BusinessTypeId.toString -> BusinessPartnership.toString,
        BusinessNameId.toString -> "MyPartnership",
        BusinessUTRId.toString -> invalidUtr
      )
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad().url)
    }

    "data is saved on page load" in {
      val dataCacheConnector = FakeUserAnswersCacheConnector

      val expectedJson =
        UserAnswers(data)
          .set(PartnershipRegisteredAddressId)(testBusinessPartnershipAddress)
          .flatMap(_.set(RegistrationInfoId)(regInfo))
          .asOpt
          .value
          .json
      val result = controller(dataRetrievalAction, dataCacheConnector).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      dataCacheConnector.lastUpsert.value mustBe expectedJson
    }

    "valid data is submitted" when {
      "yes" which {
        "upsert address and organisation name from api response" in {
          val dataCacheConnector = FakeUserAnswersCacheConnector

          val expectedJson =
            UserAnswers(data)
              .set(PartnershipRegisteredAddressId)(testBusinessPartnershipAddress)
              .flatMap(_.set(RegistrationInfoId)(regInfo))
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
            BusinessTypeId.toString -> BusinessPartnership.toString
          )

          val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
          val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "no business type data is found" in {
          val data = Json.obj(
            BusinessNameId.toString -> "MyPartnership"
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
            BusinessTypeId.toString -> BusinessPartnership.toString
          )

          val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
          val result = controller(dataRetrievalAction).onSubmit(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "no business type data is found" in {
          val data = Json.obj(
            BusinessNameId.toString -> BusinessDetails("MyPartnership", Some(validBusinessPartnershipUtr))
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

object ConfirmPartnershipDetailsControllerSpec extends ControllerSpecBase {

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

  val partnershipName = "MyPartnership"
  val organisation = Organisation("MyOrganisation", OrganisationTypeEnum.Partnership)

  private val data = Json.obj(
    BusinessTypeId.toString -> BusinessPartnership.toString,
    BusinessNameId.toString -> partnershipName,
    BusinessUTRId.toString -> validBusinessPartnershipUtr
  )

  val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))

  val formProvider = new ConfirmPartnershipDetailsFormProvider

  val form: Form[Boolean] = formProvider()

  val countryOptions = new CountryOptions(environment, frontendAppConfig)

  val regInfo = RegistrationInfo(
    RegistrationLegalStatus.Partnership,
    sapNumber,
    noIdentifier = false,
    RegistrationCustomerType.UK,
    Some(RegistrationIdType.UTR),
    Some(validBusinessPartnershipUtr)
  )

  private def fakeRegistrationConnector = new FakeRegistrationConnector {
    override def registerWithIdOrganisation
    (utr: String, organisation: Organisation, legalStatus: RegistrationLegalStatus)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganizationRegistration] = {

      if (utr == validLimitedCompanyUtr && organisation.organisationType == OrganisationTypeEnum.CorporateBody) {
        Future.successful(OrganizationRegistration(OrganizationRegisterWithIdResponse(organisation, testLimitedCompanyAddress), regInfo))
      }
      else if (utr == validBusinessPartnershipUtr && organisation.organisationType == OrganisationTypeEnum.Partnership) {
        Future.successful(OrganizationRegistration(OrganizationRegisterWithIdResponse(organisation, testBusinessPartnershipAddress), regInfo))
      }
      else {
        Future.failed(new NotFoundException(s"Unknown UTR: $utr"))
      }
    }
  }

  private def controller(
    dataRetrievalAction: DataRetrievalAction = getEmptyData,
    dataCacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  ) =
    new ConfirmPartnershipDetailsController(
      frontendAppConfig,
      messagesApi,
      dataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      FakeAllowAccessProvider(),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeRegistrationConnector,
      formProvider,
      countryOptions
    )

  private def viewAsString(partnershipName: String = partnershipName, address: TolerantAddress = testBusinessPartnershipAddress): String =
    confirmPartnershipDetails(frontendAppConfig, form, partnershipName, address, countryOptions)(fakeRequest, messages).toString

}
