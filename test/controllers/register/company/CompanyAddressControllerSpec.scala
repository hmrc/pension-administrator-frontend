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

import connectors.{FakeDataCacheConnector, RegistrationConnector}
import controllers.ControllerSpecBase
import controllers.actions._
import forms.company.CompanyAddressFormProvider
import identifiers.register.BusinessTypeId
import identifiers.register.company.{CompanyAddressId, CompanyDetailsId, CompanyUniqueTaxReferenceId}
import models.register.company.CompanyDetails
import models._
import models.register.BusinessType.{BusinessPartnership, LimitedCompany}
import play.api.data.Form
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import utils.FakeNavigator
import views.html.register.company.companyAddress

import scala.concurrent.{ExecutionContext, Future}
import identifiers.register.BusinessTypeId
import models.register.BusinessType.{BusinessPartnership, LimitedCompany}

class CompanyAddressControllerSpec extends ControllerSpecBase {

  import CompanyAddressControllerSpec._

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
        CompanyDetailsId.toString -> CompanyDetails(companyName, None, None),
        CompanyUniqueTaxReferenceId.toString -> validBusinessPartnershipUtr
      )
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(companyName, testBusinessPartnershipAddress)
    }

    "redirect to the next page when the UTR is invalid" in {
      val data = Json.obj(
        BusinessTypeId.toString -> LimitedCompany.toString,
        CompanyDetailsId.toString -> CompanyDetails("MyCo", None, None),
        CompanyUniqueTaxReferenceId.toString -> invalidUtr
      )
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }

    "valid data is submitted" when {
        "yes" which {
          "upsert address and organisation name from api response" in {
            val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "true"))

            val result = controller(dataRetrievalAction).onSubmit(NormalMode)(postRequest)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(onwardRoute.url)
          }
        }
        "no" in {
          val postRequest = fakeRequest.withFormUrlEncodedBody(("value", "false"))

          val result = controller(dataRetrievalAction).onSubmit(NormalMode)(postRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(onwardRoute.url)
        }
      }

    "redirect to Session Expired" when {
      "GET" when {
        "no company details data is found" in {
          val data = Json.obj(
            CompanyUniqueTaxReferenceId.toString -> invalidUtr
          )

          val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
          val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "no UTR data is found" in {
          val data = Json.obj(
            CompanyDetailsId.toString -> CompanyDetails("MyCo", None, None)
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
        "no company details data is found" in {
          val data = Json.obj(
            CompanyUniqueTaxReferenceId.toString -> invalidUtr
          )

          val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
          val result = controller(dataRetrievalAction).onSubmit(NormalMode)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
        }
        "no UTR data is found" in {
          val data = Json.obj(
            CompanyDetailsId.toString -> CompanyDetails("MyCo", None, None)
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

object CompanyAddressControllerSpec extends ControllerSpecBase {

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

  val companyDetails = CompanyDetails("MyCompany", None, None)
  val organisation = Organisation("MyOrganisation", OrganisationTypeEnum.CorporateBody)

  val data = Json.obj(
    BusinessTypeId.toString -> LimitedCompany.toString,
    CompanyDetailsId.toString -> companyDetails,
    CompanyUniqueTaxReferenceId.toString -> validLimitedCompanyUtr
  )

  val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))

  val formProvider = new CompanyAddressFormProvider

  val form: Form[Boolean] = formProvider()

  private def fakeRegistrationConnector = new RegistrationConnector {
    override def registerWithIdOrganisation
    (utr: String, organisation: Organisation)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganizationRegisterWithIdResponse] = {

      if (utr == validLimitedCompanyUtr && organisation.organisationType == OrganisationTypeEnum.CorporateBody) {
        Future.successful(OrganizationRegisterWithIdResponse(testLimitedCompanyAddress, organisation))
      }
      else if (utr == validBusinessPartnershipUtr && organisation.organisationType == OrganisationTypeEnum.Partnership) {
        Future.successful(OrganizationRegisterWithIdResponse(testBusinessPartnershipAddress, organisation))
      }
      else {
        Future.failed(new NotFoundException(s"Unnown UTR: $utr"))
      }
    }

    //noinspection NotImplementedCode
    def registerWithIdIndividual()
                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[IndividualRegisterWithIdResponse] = ???
  }

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new CompanyAddressController(
      frontendAppConfig,
      messagesApi,
      FakeDataCacheConnector,
      new FakeNavigator(desiredRoute = onwardRoute),
      FakeAuthAction,
      dataRetrievalAction,
      new DataRequiredActionImpl,
      fakeRegistrationConnector,
      formProvider
    )

  private def viewAsString(companyName: String = companyDetails.companyName, address: TolerantAddress = testLimitedCompanyAddress): String =
    companyAddress(frontendAppConfig, form, address, companyName)(fakeRequest, messages).toString

}
