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

package service

import base.SpecBase
import config.FeatureSwitchManagementServiceTestImpl
import connectors.{DeRegistrationConnector, SubscriptionConnector, UserAnswersCacheConnector}
import identifiers.register.RegistrationInfoId
import models.{PSAUser, UserType}
import models.PsaSubscription.PsaSubscription
import models.requests.AuthenticatedRequest
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{AsyncFlatSpec, Matchers, OptionValues}
import play.api.Configuration
import play.api.libs.json.JsValue
import services.{MandatoryIndividualDetailsMissing, PsaDetailServiceImpl, RegistrationServiceImpl}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FakeCountryOptions, FakeFeatureSwitchManagementService, UserAnswers}
import viewmodels.PsaViewDetailsViewModel

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

class PsaDetailsServiceSpec extends AsyncFlatSpec with Matchers with OptionValues with SpecBase with MockitoSugar {

  import PsaDetailsServiceSpec._

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit  val request = AuthenticatedRequest(fakeRequest, "", PSAUser(UserType.Organisation, None, false, Some("test Psa id")))

  val fs = new FeatureSwitchManagementServiceTestImpl(configuration, environment)
  val configuration = injector.instanceOf[Configuration]
  val mockSubscriptionConnector = mock[SubscriptionConnector]
  val mockDeRegistrationConnector = mock[DeRegistrationConnector]
  val mockUserAnswersConnector = mock[UserAnswersCacheConnector]

  def service = new PsaDetailServiceImpl(
    fs,
    messagesApi,
    mockSubscriptionConnector,
    FakeCountryOptions(),
    mockDeRegistrationConnector,
    mockUserAnswersConnector
  )

  "PsaDetailsService" should "return the correct view model for PSA Individual" in {

    service.retrievePsaDataAndGenerateViewModel("").map{ result =>
      result shouldBe PsaViewDetailsViewModel(Seq.empty, "", false, false)
    }
  }
}
object PsaDetailsServiceSpec {

}


