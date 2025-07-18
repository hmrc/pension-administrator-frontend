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

package controllers

import connectors.cache.UserAnswersCacheConnector
import controllers.actions._
import identifiers.register.individual.{IndividualContactAddressId, IndividualDetailsId}
import models._
import models.requests.DataRequest
import org.mockito.ArgumentMatchers._
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.Call
import play.api.test.Helpers._
import services.PsaDetailsService
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, FakeNavigator, UserAnswers}
import views.html.updateContactAddress

import scala.concurrent.Future

class UpdateContactAddressControllerSpec extends ControllerSpecBase with BeforeAndAfterEach with MockitoSugar{

  import UpdateContactAddressControllerSpec._

  override protected def beforeEach(): Unit = {
    reset(mockUserAnswersCacheConnector)
    when(mockUserAnswersCacheConnector.upsert(any())(any(), any()))
      .thenReturn(Future.successful(JsNull))
  }

  "UpdateContactAddressController" must {
    "return OK and the correct view for a GET" in {
      when(mockPsaDetailsService.getUserAnswers(any(), any()))
        .thenReturn(Future.successful(individual))

      val result = controller(dataRetrievalAction).onPageLoad()(fakeRequest)

      status(result) mustBe OK

      contentAsString(result) mustBe
        viewAsString(individual, onwardRoute)
    }
  }

  private val mockUserAnswersCacheConnector = mock[UserAnswersCacheConnector]

  private def controller(dataRetrievalAction: DataRetrievalAction) =
    new UpdateContactAddressController(
      FakeAuthAction(UserType.Individual),
      dataRetrievalAction,
      controllerComponents,
      mockPsaDetailsService,
      countryOptions,
      mockUserAnswersCacheConnector,
      navigator,
      view
    )
}

object UpdateContactAddressControllerSpec extends ControllerSpecBase with MockitoSugar {

  private val onwardRoute = "/url"
  private val navigator = new FakeNavigator(Call("GET", onwardRoute))

  private val psaUser = PSAUser(UserType.Individual, None, false, None, None, "")
  private val address = Address("value 1", "value 2", None, None, Some("AB1 1AB"), "GB")
  private val individual = UserAnswers(Json.obj()).registrationInfo(RegistrationInfo(
    RegistrationLegalStatus.Individual, "", false, RegistrationCustomerType.UK, None, None))
    .set(IndividualDetailsId)(TolerantIndividual(Some("Mark"), None, Some("Wright"))).asOpt.value
    .setOrException(IndividualContactAddressId)(address)

  private def countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  private val mockPsaDetailsService = mock[PsaDetailsService]

  private val expectedAddressLines = Seq("value 1", "value 2", "AB1 1AB", "Country of GB")

  private val dataRetrievalAction = new FakeDataRetrievalAction(Some(individual.json))

  lazy val view: updateContactAddress = inject[updateContactAddress]

  private def viewAsString(userAnswers: UserAnswers, url:String) =
    view(expectedAddressLines, url)(DataRequest(fakeRequest, "cacheId", psaUser, userAnswers), messages).toString
}
