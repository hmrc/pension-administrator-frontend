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

package connectors

import base.JsonFileReader
import com.github.tomakehurst.wiremock.client.WireMock._
import models.IndividualDetails
import models.MinimalPSA
import org.scalatest.AsyncFlatSpec
import org.scalatest.Matchers
import org.scalatest.OptionValues
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.JsResultException
import play.api.libs.json.Json
import uk.gov.hmrc.http.BadRequestException
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.NotFoundException
import utils.WireMockHelper
import play.api.http.Status

class MinimalPsaConnectorImplSpec extends AsyncFlatSpec with Matchers with WireMockHelper {

  import MinimalPsaConnectorImplSpec._

  override protected lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        portConfigKey -> server.port().toString,
        "auditing.enabled" -> false,
        "metrics.enabled" -> false
      )
      .build()

  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  "isPsaSuspended" should "return suspended flag the PSA subscription for a valid request" in {

    server.stubFor(
      get(urlEqualTo(minimalPsaDetailsUrl))
        .willReturn(
          ok(validResponse)
            .withHeader("Content-Type", "application/json")
        )
    )

    val connector = injector.instanceOf[MinimalPsaConnectorImpl]

    connector.isPsaSuspended(psaId).map(isSuspended =>
      isSuspended shouldBe true
    )

  }

  it should "throw JsResultException if the response status is not 200 OK" in {

    server.stubFor(
      get(urlEqualTo(minimalPsaDetailsUrl))
        .willReturn(
          ok(invalidPayloadResponse)
            .withHeader("Content-Type", "application/json")
        )
    )

    val connector = injector.instanceOf[MinimalPsaConnectorImpl]

    recoverToSucceededIf[JsResultException] {
      connector.isPsaSuspended(psaId)
    }

  }

  it should "throw NotFoundException" in {

    server.stubFor(
      get(urlEqualTo(minimalPsaDetailsUrl))
        .willReturn(
          notFound()
        )
    )

    val connector = injector.instanceOf[MinimalPsaConnectorImpl]

    recoverToSucceededIf[NotFoundException] {
      connector.isPsaSuspended(psaId)
    }

  }

  "getMinimalPsaDetails" should "return the MinimalPsa for a valid request/response" in {

    server.stubFor(
      get(urlEqualTo(minimalPsaDetailsUrl))
        .withHeader("psaId", equalTo(psaId))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(validMinimalPsaDetailsResponse)
        )
    )

    val connector = injector.instanceOf[MinimalPsaConnector]

    connector.getMinimalPsaDetails(psaId).map(psa =>
      psa shouldBe expectedResponse
    )

  }

  it should "throw BadRequestException for a 400 INVALID_PSAID response" in {

    server.stubFor(
      get(urlEqualTo(minimalPsaDetailsUrl))
        .withHeader("psaId", equalTo(psaId))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody(errorResponse("INVALID_PSAID"))
        )
    )

    val connector = injector.instanceOf[MinimalPsaConnector]
    recoverToSucceededIf[BadRequestException] {
      connector.getMinimalPsaDetails(psaId)
    }
  }

  it should "throw BadRequest for a 400 INVALID_CORRELATIONID response" in {

    server.stubFor(
      get(urlEqualTo(minimalPsaDetailsUrl))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody(errorResponse("INVALID_CORRELATIONID"))
        )
    )
    val connector = injector.instanceOf[MinimalPsaConnector]

    recoverToSucceededIf[BadRequestException] {
      connector.getMinimalPsaDetails(psaId)
    }
  }
}

object MinimalPsaConnectorImplSpec extends OptionValues with JsonFileReader {

  private val minimalPsaDetailsUrl = "/pension-administrator/get-minimal-psa"

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val psaId = "test-psa-id"

  private val validResponse =
    Json.stringify(
      Json.obj(
        "isPsaSuspended" -> true
      )
    )

  private val invalidPayloadResponse =
    Json.stringify(
      Json.obj(
        "isPsaSuspended" -> "reason"
      )
    )
    def errorResponse(code: String): String = {
      Json.stringify(
        Json.obj(
          "code" -> code,
          "reason" -> s"Reason for $code"
        )
      )
    }
  private val validMinimalPsaDetailsResponse = readJsonFromFile("/data/validMinimalPsaDetails.json").toString()
  private val email = "test@test.com"
  private val expectedResponse = MinimalPSA(email,false,None,Some(IndividualDetails("First",Some("Middle"),"Last")), rlsFlag = false)

}
