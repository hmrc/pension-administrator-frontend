/*
 * Copyright 2021 HM Revenue & Customs
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
import models.{MinimalPSA, IndividualDetails}
import org.scalatest.{OptionValues, Matchers, AsyncFlatSpec}
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import utils.WireMockHelper

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
  private val expectedResponse = MinimalPSA(email,isPsaSuspended = false,None,Some(IndividualDetails("First",Some("Middle"),"Last")),
    rlsFlag = false, deceasedFlag = false)

}
