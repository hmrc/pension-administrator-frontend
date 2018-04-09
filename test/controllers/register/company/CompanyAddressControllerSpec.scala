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
import identifiers.register.BusinessTypeId
import identifiers.register.company.{BusinessDetailsId, CompanyAddressId, CompanyDetailsId}
import models._
import models.register.BusinessType.{BusinessPartnership, LimitedCompany}
import models.register.company.{BusinessDetails, CompanyDetails}
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import utils.FakeNavigator
import views.html.register.company.companyAddress

import scala.concurrent.{ExecutionContext, Future}

class CompanyAddressControllerSpec extends ControllerSpecBase {

  import CompanyAddressControllerSpec._

  "CompanyAddress Controller" must {

    "return OK and the correct view for a GET when UTR is valid" in {
      val data = Json.obj(
        BusinessTypeId.toString -> LimitedCompany.toString,
        CompanyDetailsId.toString -> CompanyDetails("MyCo", None, None),
        BusinessDetailsId.toString -> BusinessDetails("MyCo", validLimitedCompanyUtr)
      )
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "correctly map the Business Type to Organisation Type for the call to API4" in {
      val data = Json.obj(
        BusinessTypeId.toString -> BusinessPartnership.toString,
        CompanyDetailsId.toString -> CompanyDetails("MyPartnership", None, None),
        BusinessDetailsId.toString -> BusinessDetails("MyPartnership", validBusinessPartnershipUtr)
      )
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString(testBusinessPartnershipAddress)
    }

    "save the address to user answers when UTR is valid" in {
      val data = Json.obj(
        BusinessTypeId.toString -> LimitedCompany.toString,
        CompanyDetailsId.toString -> CompanyDetails("MyCo", None, None),
        BusinessDetailsId.toString -> BusinessDetails("MyCo", validLimitedCompanyUtr)
      )
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      FakeDataCacheConnector.verify(CompanyAddressId, testLimitedCompanyAddress)
    }

    "redirect to the next page when the UTR is invalid" in {
      val data = Json.obj(
        BusinessTypeId.toString -> LimitedCompany.toString,
        CompanyDetailsId.toString -> CompanyDetails("MyCo", None, None),
        BusinessDetailsId.toString -> BusinessDetails("name", invalidUtr)
      )
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
    }

    "redirect to Session Expired for a GET if no company details data is found" in {
      val data = Json.obj(
        BusinessDetailsId.toString -> BusinessDetails("name", validLimitedCompanyUtr)
      )

      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to Session Expired for a GET if no UTR data is found" in {
      val data = Json.obj(
        CompanyDetailsId.toString -> CompanyDetails("MyCo", None, None)
      )

      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }

    "redirect to the next page on a POST" in {
      val result = controller().onSubmit(NormalMode)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(onwardRoute.url)
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

  private def fakeRegistrationConnector = new RegistrationConnector {
    override def registerWithIdOrganisation
    (utr: String, organisation: Organisation)
    (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganizationRegisterWithIdResponse] = {

      if (utr == validLimitedCompanyUtr && organisation.organisationType == OrganisationTypeEnum.CorporateBody) {
        Future.successful(OrganizationRegisterWithIdResponse(testLimitedCompanyAddress))
      }
      else if (utr == validBusinessPartnershipUtr && organisation.organisationType == OrganisationTypeEnum.Partnership) {
        Future.successful(OrganizationRegisterWithIdResponse(testBusinessPartnershipAddress))
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
    new CompanyAddressController(frontendAppConfig, messagesApi, FakeDataCacheConnector, new FakeNavigator(desiredRoute = onwardRoute), FakeAuthAction,
      dataRetrievalAction, new DataRequiredActionImpl, fakeRegistrationConnector)

  private def viewAsString(address: TolerantAddress = testLimitedCompanyAddress): String =
      companyAddress(frontendAppConfig, address)(fakeRequest, messages).toString

}
