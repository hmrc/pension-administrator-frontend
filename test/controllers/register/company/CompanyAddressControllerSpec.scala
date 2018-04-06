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
import identifiers.register.company.{BusinessDetailsId, CompanyAddressId, CompanyDetailsId}
import models.register.company.{BusinessDetails, CompanyDetails}
import models._
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import utils.FakeNavigator
import views.html.register.company.companyAddress

import scala.concurrent.{ExecutionContext, Future}

class CompanyAddressControllerSpec extends ControllerSpecBase {

  private def onwardRoute = controllers.routes.IndexController.onPageLoad()

  private val testAddress = TolerantAddress(
    Some("Some Building"),
    Some("1 Some Street"),
    Some("Some Village"),
    Some("Some Town"),
    Some("ZZ1 1ZZ"),
    Some("UK")
  )

  private val validUtr = "1234567890"
  private val invalidUtr = "INVALID"

  private def fakeRegistrationConnector = new RegistrationConnector {
    override def registerWithIdOrganisation
        (utr: String, organisation: Organisation)
        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganizationRegisterWithIdResponse] = {

        if (utr == validUtr) {
          Future.successful(OrganizationRegisterWithIdResponse(testAddress))
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

  private def viewAsString(): String = companyAddress(frontendAppConfig, testAddress)(fakeRequest, messages).toString

  "CompanyAddress Controller" must {

    "return OK and the correct view for a GET when UTR is valid" in {
      val data = Json.obj(
        CompanyDetailsId.toString -> CompanyDetails("MyCo", None, None),
        BusinessDetailsId.toString -> BusinessDetails("name", validUtr)
      )
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) mustBe viewAsString()
    }

    "save the address to user answers when UTR is valid" in {
      val data = Json.obj(
        CompanyDetailsId.toString -> CompanyDetails("MyCo", None, None),
        BusinessDetailsId.toString -> BusinessDetails("name", validUtr)
      )
      val dataRetrievalAction = new FakeDataRetrievalAction(Some(data))
      val result = controller(dataRetrievalAction).onPageLoad(NormalMode)(fakeRequest)

      status(result) mustBe OK
      FakeDataCacheConnector.verify(CompanyAddressId, testAddress)
    }

    "redirect to the next page when the UTR is invalid" in {
      val data = Json.obj(
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
        BusinessDetailsId.toString -> BusinessDetails("name", validUtr)
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
