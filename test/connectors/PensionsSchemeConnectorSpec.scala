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

import com.fasterxml.jackson.core.JsonParseException
import com.github.tomakehurst.wiremock.client.WireMock._
import identifiers.register.BusinessTypeId
import models.register.{BusinessType, PsaSubscriptionResponse}
import org.scalatest.{AsyncFlatSpec, Matchers, OptionValues}
import play.api.http.Status
import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.http.HeaderCarrier
import utils.{UserAnswers, WireMockHelper}

class PensionsSchemeConnectorSpec extends AsyncFlatSpec with Matchers with WireMockHelper {

  import PensionsSchemeConnectorSpec._

  override protected def portConfigKey: String = "microservice.services.pensions-scheme.port"

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

    connector.registerPsa(userAnswers).map( subscription =>
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

  it should "throw InvalidPayloadException for a 400 INVALID_PAYLOAD response" in {

    server.stubFor(
      post(urlEqualTo(registerPsaUrl))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody(invalidPayloadResponse)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[InvalidPayloadException] {
      connector.registerPsa(userAnswers)
    }

  }

  it should "throw InvalidCorrelationIdException for a 400 INVALID_CORRELATION_ID response" in {

    server.stubFor(
      post(urlEqualTo(registerPsaUrl))
        .willReturn(
            badRequest
              .withHeader("Content-Type", "application/json")
              .withBody(invalidCorrelationIdResponse)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[InvalidCorrelationIdException] {
      connector.registerPsa(userAnswers)
    }

  }

  it should "throw InvalidBusinessPartnerException for a 403 INVALID_BUSINESS_PARTNER response" in {

    server.stubFor(
      post(urlEqualTo(registerPsaUrl))
        .willReturn(
          forbidden
            .withHeader("Content-Type", "application/json")
            .withBody(invalidBusinessPartnerResponse)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[InvalidBusinessPartnerException] {
      connector.registerPsa(userAnswers)
    }

  }

  it should "throw DuplicateSubmissionException for a 409 DUPLICATE_SUBMISSION response" in {

    server.stubFor(
      post(urlEqualTo(registerPsaUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.CONFLICT)
            .withHeader("Content-Type", "application/json")
            .withBody(duplicateSubmissionResponse)
        )
    )

    val connector = injector.instanceOf[PensionsSchemeConnector]

    recoverToSucceededIf[DuplicateSubmissionException] {
      connector.registerPsa(userAnswers)
    }

  }

}

object PensionsSchemeConnectorSpec extends OptionValues {

  private val registerPsaUrl = "/pensions-scheme/register-psa"

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

}
