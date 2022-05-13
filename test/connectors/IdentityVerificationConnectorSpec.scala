/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import config.FrontendAppConfig
import org.mockito.Mockito.when
import org.scalatest.{AsyncWordSpec, MustMatchers, OptionValues}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import utils.{FrontendAppConfigSpyProvider, WireMockHelper}

class IdentityVerificationConnectorSpec extends AsyncWordSpec with MustMatchers with WireMockHelper with OptionValues {

  import IdentityVerificationConnectorSpec._

  override protected lazy val app: Application = {

    new GuiceApplicationBuilder()
      .configure(
        portConfigKey -> server.port().toString,
        "auditing.enabled" -> false,
        "metrics.enabled" -> false
      ).overrides(
      bind[FrontendAppConfig].toProvider[FrontendAppConfigSpyProvider]
    )
      .build()
  }

  override protected def portConfigKey: String = "microservice.services.identity-verification.port"

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val connector = injector.instanceOf[PersonalDetailsValidationConnector]
  private lazy val frontendAppConfig = injector.instanceOf[FrontendAppConfig]

  private val url: String = s"/identity-verification/journey/$journeyId"

  override def beforeAll(): Unit = {
    super.beforeAll()
    //when(frontendAppConfig.pointingFromIvApiToPdvApi).thenReturn(false)
  }

  ".retrieveNinoFromIV" must {

    "return a Nino" when {

      "IV returned successfully with a nino" in {
        server.stubFor(
          get(urlEqualTo(url)
          ).willReturn(
            ok(expectedResponse)
          )
        )

        connector.retrieveNino(journeyId).map {
          result =>
            result.value mustBe Nino("AB000003D")
        }
      }
    }

    "return None" when {
      "no nino returned from IV" in {

        server.stubFor(
          get(urlEqualTo(url)
          ).willReturn(
            ok(responseWithoutNino)
          )
        )

        connector.retrieveNino(journeyId).map {
          result => {
            result mustBe None
          }
        }
      }

      "nino is malformed" in {
        server.stubFor(
          get(urlEqualTo(url)
          ).willReturn(
            ok(responseWithMalformedNino)
          )
        )

        connector.retrieveNino(journeyId).map {
          result =>
            result mustBe None
        }
      }

      "no results are found for the requested journey Id" in {
        server.stubFor(
          get(urlEqualTo(url)
          ).willReturn(
            notFound()
          )
        )

        connector.retrieveNino(journeyId).map {
          result =>
            result mustBe None
        }
      }
    }
  }
}

object IdentityVerificationConnectorSpec {

  private val journeyId = "1234"
  private val expectedResponse =
    """{
      |  "journeyId" : "502f90f7-13ab-44c4-a4fa-474da0f0fe03",
      |  "journeyType" : "OneTimeLogin",
      |  "progress" : {
      |    "questions" : [ {
      |      "questionKey" : "fia-bankaccount",
      |      "answers" : [ "7890", "4567", "2134", "4564", "8345" ],
      |      "respondTo" : "http://localhost:38376/identity-verification/journey/502f90f7-13ab-44c4-a4fa-474da0f0fe03/answer",
      |      "source" : "financial-accounts"
      |    } ],
      |    "numAnswered" : 0,
      |    "result" : "Incomplete"
      |  },
      |  "serviceContract": {
      |    "origin": "tama",
      |    "completionURL": "/completionURL",
      |    "failureURL": "/failureURL",
      |    "confidenceLevel": 100
      |  },
      |  "availableEvidenceOptions" : [ {
      |    "evidenceOption" : "financial-accounts"
      |  } ],
      |  "nino": "AB000003D",
      |  "self": "http://identity-verification.service:80/identity-verification/journey/1234",
      |  "start": "http://identity-verification.service:80/identity-verification/journey/1234/start"
      |}""".stripMargin

  private val responseWithMalformedNino =
    """{
      |  "journeyId" : "502f90f7-13ab-44c4-a4fa-474da0f0fe03",
      |  "journeyType" : "OneTimeLogin",
      |  "progress" : {
      |    "questions" : [ {
      |      "questionKey" : "fia-bankaccount",
      |      "answers" : [ "7890", "4567", "2134", "4564", "8345" ],
      |      "respondTo" : "http://localhost:38376/identity-verification/journey/502f90f7-13ab-44c4-a4fa-474da0f0fe03/answer",
      |      "source" : "financial-accounts"
      |    } ],
      |    "numAnswered" : 0,
      |    "result" : "Incomplete"
      |  },
      |  "serviceContract": {
      |    "origin": "tama",
      |    "completionURL": "/completionURL",
      |    "failureURL": "/failureURL",
      |    "confidenceLevel": 100
      |  },
      |  "availableEvidenceOptions" : [ {
      |    "evidenceOption" : "financial-accounts"
      |  } ],
      |  "nino": "blah",
      |  "self": "http://identity-verification.service:80/identity-verification/journey/1234",
      |  "start": "http://identity-verification.service:80/identity-verification/journey/1234/start"
      |}""".stripMargin

  private val responseWithoutNino =
    """{
      |  "journeyId" : "502f90f7-13ab-44c4-a4fa-474da0f0fe03",
      |  "journeyType" : "OneTimeLogin",
      |  "progress" : {
      |    "questions" : [ {
      |      "questionKey" : "fia-bankaccount",
      |      "answers" : [ "7890", "4567", "2134", "4564", "8345" ],
      |      "respondTo" : "http://localhost:38376/identity-verification/journey/502f90f7-13ab-44c4-a4fa-474da0f0fe03/answer",
      |      "source" : "financial-accounts"
      |    } ],
      |    "numAnswered" : 0,
      |    "result" : "Incomplete"
      |  },
      |  "serviceContract": {
      |    "origin": "tama",
      |    "completionURL": "/completionURL",
      |    "failureURL": "/failureURL",
      |    "confidenceLevel": 100
      |  },
      |  "availableEvidenceOptions" : [ {
      |    "evidenceOption" : "financial-accounts"
      |  } ],
      |  "self": "http://identity-verification.service:80/identity-verification/journey/1234",
      |  "start": "http://identity-verification.service:80/identity-verification/journey/1234/start"
      |}""".stripMargin
}
