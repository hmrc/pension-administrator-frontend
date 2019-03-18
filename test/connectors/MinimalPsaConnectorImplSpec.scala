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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.{AsyncFlatSpec, Matchers, OptionValues}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
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
}

object MinimalPsaConnectorImplSpec extends OptionValues {

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
}
