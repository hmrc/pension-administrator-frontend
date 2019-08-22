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

package connectors

import com.fasterxml.jackson.core.JsonParseException
import com.github.tomakehurst.wiremock.client.WireMock._
import identifiers.register.BusinessTypeId
import models.register.{BusinessType, PsaSubscriptionResponse}
import org.scalatest.{AsyncFlatSpec, Matchers, OptionValues}
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException, Upstream5xxResponse}
import utils.{UserAnswers, WireMockHelper}

class PensionsSchemeConnectorSpec extends AsyncFlatSpec with Matchers with WireMockHelper {

  import PensionsSchemeConnectorSpec._

  override protected lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        portConfigKey -> server.port().toString,
        "auditing.enabled" -> false,
        "metrics.enabled" -> false
      )
      .build()

  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  "registerPsa" should "return the PSA subscription for a valid request/response" in {

    server.stubFor(
      post(urlEqualTo(registerPsaUrl))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalToJson(Json.stringify(userAnswers.json)))
        .willReturn(
          ok(validResponse)
            .withHeader("Content-Type", "application/json")
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    connector.registerPsa(userAnswers).map(subscription =>
      subscription shouldBe psaSubscriptionResponse
    )

  }

  it should "throw IllegalArgumentException if the response status is not 200 OK" in {

    server.stubFor(
      post(urlEqualTo(registerPsaUrl))
        .willReturn(
          created
            .withHeader("Content-Type", "application/json")
            .withBody(validResponse)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[IllegalArgumentException] {
      connector.registerPsa(userAnswers)
    }

  }

  it should "throw JsonParseException if there are JSON parse errors" in {

    server.stubFor(
      post(urlEqualTo(registerPsaUrl))
        .willReturn(
          ok("this-is-not-valid-json")
            .withHeader("Content-Type", "application/json")
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[JsonParseException] {
      connector.registerPsa(userAnswers)
    }

  }

  it should "throw JsResultException if the JSON is not valid" in {

    server.stubFor(
      post(urlEqualTo(registerPsaUrl))
        .willReturn(
          ok("{}")
            .withHeader("Content-Type", "application/json")
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[JsResultException] {
      connector.registerPsa(userAnswers)
    }

  }

  it should "propagate exceptions" in {

    server.stubFor(
      post(urlEqualTo(registerPsaUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.BAD_REQUEST)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[BadRequestException] {
      connector.registerPsa(userAnswers)
    }
  }

  "updatePsa" should "return without exceptions for a valid request/response" in {
    val psaId = "testpsa"
    server.stubFor(
      post(urlEqualTo(updatePsaUrl(psaId = psaId)))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalToJson(Json.stringify(userAnswers.json)))
        .willReturn(
          ok(validResponse)
            .withHeader("Content-Type", "application/json")
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    noException shouldBe thrownBy {
      connector.updatePsa(psaId, userAnswers)
    }
  }

  it should "return BadRequestException where invalid psaid response is received" in {
    val psaId = "testpsa"
    server.stubFor(
      post(urlEqualTo(updatePsaUrl(psaId = psaId)))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody(invalidPSAIDResponse)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[BadRequestException] {
      connector.updatePsa(psaId, userAnswers)
    }
  }


  it should "return BadRequestException where not found response is received" in {
    val psaId = "testpsa"
    server.stubFor(
      post(urlEqualTo(updatePsaUrl(psaId = psaId)))
        .willReturn(
          aResponse.withStatus(Status.NOT_FOUND)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[NotFoundException] {
      connector.updatePsa(psaId, userAnswers)
    }
  }

  it should "return BadRequestException where 400 response is received" in {
    val psaId = "testpsa"
    server.stubFor(
      post(urlEqualTo(updatePsaUrl(psaId = psaId)))
        .willReturn(
          aResponse.withStatus(Status.BAD_REQUEST)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[BadRequestException] {
      connector.updatePsa(psaId, userAnswers)
    }
  }

  it should "return Upstream5xxResponse where 500 response is received" in {
    val psaId = "testpsa"
    server.stubFor(
      post(urlEqualTo(updatePsaUrl(psaId = psaId)))
        .willReturn(
          aResponse.withStatus(Status.INTERNAL_SERVER_ERROR)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[Upstream5xxResponse] {
      connector.updatePsa(psaId, userAnswers)
    }
  }
}

object PensionsSchemeConnectorSpec extends OptionValues {

  private val registerPsaUrl = "/pension-administrator/register-psa"

  private def updatePsaUrl(psaId: String) = "/pension-administrator/psa-variation/%s".format(psaId)

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val userAnswers = UserAnswers().set(BusinessTypeId)(BusinessType.LimitedCompany).asOpt.value
  private val psaId = "test-psa-id"
  private val psaSubscriptionResponse = PsaSubscriptionResponse(psaId)

  private val validResponse =
    Json.stringify(
      Json.obj(
        "processingDate" -> "1969-07-20T20:18:00Z",
        "formBundle" -> "test-form-bundle",
        "psaId" -> psaId
      )
    )

  private val invalidPayloadResponse =
    Json.stringify(
      Json.obj(
        "code" -> "INVALID_PAYLOAD",
        "reason" -> "test-reason"
      )
    )

  private val invalidCorrelationIdResponse =
    Json.stringify(
      Json.obj(
        "code" -> "INVALID_CORRELATION_ID",
        "reason" -> "test-reason"
      )
    )

  private val invalidBusinessPartnerResponse =
    Json.stringify(
      Json.obj(
        "code" -> "INVALID_BUSINESS_PARTNER",
        "reason" -> "test-reason"
      )
    )

  private val duplicateSubmissionResponse =
    Json.stringify(
      Json.obj(
        "code" -> "DUPLICATE_SUBMISSION",
        "reason" -> "test-reason"
      )
    )

  private val invalidPSAIDResponse =
    Json.stringify(
      Json.obj(
        "code" -> "INVALID_PSAID",
        "reason" -> "test-reason"
      )
    )
}
