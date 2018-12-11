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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.PensionsSchemeConnectorSpec.userAnswers
import org.scalatest.{AsyncWordSpec, MustMatchers, RecoverMethods}
import play.api.http.Status
import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import utils.WireMockHelper

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class IdentityVerificationConnectorSpec extends AsyncWordSpec with MustMatchers with WireMockHelper with RecoverMethods {

  private val successResponse = Json.stringify(
    Json.obj(
      "token" -> "a token",
      "start" -> "a url"
    )
  )

  private val invalidResponse = Json.stringify(
    Json.obj(
      "tken" -> "a token",
      "start" -> "a url"
    )
  )

  private def postBody(completionURL: String, failureURL: String) = Json.stringify(
    Json.obj (
      "origin" -> "PODS",
      "journeyType" -> "UpliftNoNino",
      "completionURL" -> completionURL,
      "failureURL" -> failureURL,
      "confidenceLevel" -> 200
    )
  )

  private val url = "/journey"

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val completionURL = "completion url"
  private val failureURL = "failure url"

  override protected def portConfigKey: String = "microservice.services.identity-verification.port"

  private lazy val connector = injector.instanceOf[IdentityVerificationConnector]

  "IdentityVerificationConnector" must {
    "return correct responses successfully with status 201 (Created)" in {
      val expectedResult = IVRegisterOrganisationAsIndividualResponse("a token", "a url")
      server.stubFor(
        post(urlEqualTo(url))
          .withRequestBody(equalTo(postBody(completionURL, failureURL)))
          .willReturn(
          aResponse()
            .withStatus(Status.CREATED)
            .withHeader("Content-Type", "application/json")
            .withBody(successResponse)
        )
      )
      connector.startRegisterOrganisationAsIndividual(completionURL = completionURL, failureURL = failureURL).map {
        result =>
          result mustBe expectedResult
      }
    }

    "throw a Upstream5xxResponse if bad gateway status returned" in {
      server.stubFor(
        post(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(Status.BAD_GATEWAY)
          )
      )

      recoverToSucceededIf[Upstream5xxResponse] {
        connector.startRegisterOrganisationAsIndividual(completionURL = completionURL, failureURL = failureURL)
      }
    }

    "throw a JsResultException if an invalid response body is returned" in {
      server.stubFor(
        post(urlEqualTo(url))
          .withRequestBody(equalTo(postBody(completionURL, failureURL)))
          .willReturn(
            aResponse()
              .withStatus(Status.CREATED)
              .withHeader("Content-Type", "application/json")
              .withBody(invalidResponse)
          )
      )
      recoverToSucceededIf[JsResultException] {
        connector.startRegisterOrganisationAsIndividual(completionURL = completionURL, failureURL = failureURL)
      }
    }
  }
}
