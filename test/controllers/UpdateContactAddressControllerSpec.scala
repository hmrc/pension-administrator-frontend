/*
 * Copyright 2020 HM Revenue & Customs
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

package controllers

import controllers.actions._
import identifiers.register.BusinessNameId
import identifiers.register.company.CompanyContactAddressId
import identifiers.register.individual.IndividualContactAddressId
import identifiers.register.individual.IndividualDetailsId
import identifiers.register.partnership.PartnershipContactAddressId
import models._
import models.requests.DataRequest
import org.scalatest.BeforeAndAfter
import play.api.libs.json.Json
import play.api.test.Helpers._
import services.PsaDetailsService
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.FakeCountryOptions
import utils.UserAnswers
import utils.countryOptions.CountryOptions
import views.html.updateContactAddress
import org.mockito.Matchers._
import org.mockito.Mockito.when

import scala.concurrent.Future

class UpdateContactAddressControllerSpec extends ControllerSpecBase with BeforeAndAfter {

  import UpdateContactAddressControllerSpec._

  "UpdateContactAddressController" must {

    "return OK and the correct view for a GET for an individual" in {
      when(mockPsaDetailsService.getUserAnswers(any(), any())(any(), any()))
        .thenReturn(Future.successful(individual))
      val result = controller(dataRetrievalAction).onPageLoad()(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe
        viewAsString(individual, controllers.register.individual.routes.IndividualContactAddressPostCodeLookupController.onPageLoad(UpdateMode).url)
    }

    "return OK and the correct view for a GET for a company" in {
      when(mockPsaDetailsService.getUserAnswers(any(), any())(any(), any()))
        .thenReturn(Future.successful(company))
      val result = controller(dataRetrievalAction).onPageLoad()(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe
        viewAsString(company, controllers.register.company.routes.CompanyContactAddressController.onPageLoad(CheckUpdateMode).url)
    }

    "return OK and the correct view for a GET for a partnership" in {
      when(mockPsaDetailsService.getUserAnswers(any(), any())(any(), any()))
        .thenReturn(Future.successful(partnership))
      val result = controller(dataRetrievalAction).onPageLoad()(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe
        viewAsString(company, controllers.register.partnership.routes.PartnershipContactAddressController.onPageLoad(CheckUpdateMode).url)
    }

    "redirect to Session Expired on a GET when no data exists" in {
      val result = controller(dontGetAnyData).onPageLoad()(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.SessionExpiredController.onPageLoad().url)
    }
  }

  private def controller(dataRetrievalAction: DataRetrievalAction = getEmptyData) =
    new UpdateContactAddressController(
      frontendAppConfig,
      FakeAuthAction(UserType.Individual),
      dataRetrievalAction,
      new DataRequiredActionImpl,
      stubMessagesControllerComponents(),
      mockPsaDetailsService,
      countryOptions,
      view
    )
}

object UpdateContactAddressControllerSpec extends ControllerSpecBase {

  private val psaUser = PSAUser(UserType.Individual, None, false, None, None, "")
  private val address = Address("value 1", "value 2", None, None, Some("AB1 1AB"), "GB")
  private val individual = UserAnswers(Json.obj()).registrationInfo(RegistrationInfo(
    RegistrationLegalStatus.Individual, "", false, RegistrationCustomerType.UK, None, None))
    .set(IndividualDetailsId)(TolerantIndividual(Some("Mark"), None, Some("Wright"))).asOpt.value
    .setOrException(IndividualContactAddressId)(address)

  private val company = UserAnswers(Json.obj()).registrationInfo(RegistrationInfo(
    RegistrationLegalStatus.LimitedCompany, "", false, RegistrationCustomerType.UK, None, None))
    .setOrException(BusinessNameId)("Big company")
    .setOrException(CompanyContactAddressId)(address)

  private val partnership = UserAnswers(Json.obj()).registrationInfo(RegistrationInfo(
    RegistrationLegalStatus.Partnership, "", false, RegistrationCustomerType.UK, None, None))
    .setOrException(BusinessNameId)("Big company")
    .setOrException(PartnershipContactAddressId)(address)

  private def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  private val mockPsaDetailsService = mock[PsaDetailsService]

  private val expectedAddressLines = Seq("value 1", "value 2", "AB1 1AB", "Country of GB")

  private val dataRetrievalAction = new FakeDataRetrievalAction(Some(individual.json))

  lazy val view: updateContactAddress = inject[updateContactAddress]

  private def viewAsString(userAnswers: UserAnswers, url:String) =
    view(expectedAddressLines, url)(DataRequest(fakeRequest, "cacheId", psaUser, userAnswers), messages).toString
}
