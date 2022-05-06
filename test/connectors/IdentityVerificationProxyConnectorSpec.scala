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
import org.scalatest.{AsyncWordSpec, MustMatchers, RecoverMethods}
import play.api.http.Status
import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.WireMockHelper

class IdentityVerificationProxyConnectorSpec extends AsyncWordSpec with MustMatchers with WireMockHelper with RecoverMethods {

  import IdentityVerificationProxyConnectorSpec._

  private val url = "/identity-verification-proxy/journey/start"

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override protected def portConfigKey: String = "microservice.services.identity-verification-proxy.port"

  private lazy val connector = injector.instanceOf[PersonalDetailsValidationConnector]

  "IdentityVerificationConnector .startRegisterOrganisationAsIndividual" must {
    "return correct responses successfully with status 201 (Created)" in {
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
          result mustBe "a link"
      }
    }

    "throw a UpstreamErrorResponse if bad gateway status returned" in {
      server.stubFor(
        post(urlEqualTo(url))
          .willReturn(
            aResponse()
              .withStatus(Status.BAD_GATEWAY)
          )
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
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

object IdentityVerificationProxyConnectorSpec {
  private val successResponse = Json.stringify(
    Json.obj(
      "link" -> "a link",
      "journeyLink" -> "a journey link"
    )
  )

  private val invalidResponse = Json.stringify(
    Json.obj(
      "nolink" -> "a token",
      "journeyLink" -> "a url"
    )
  )

  private def postBody(completionURL: String, failureURL: String) = Json.stringify(
    Json.obj(
      "origin" -> "PODS",
      "completionURL" -> completionURL,
      "failureURL" -> failureURL,
      "confidenceLevel" -> 200
    )
  )

  private val completionURL = "completion url"
  private val failureURL = "failure url"

}
