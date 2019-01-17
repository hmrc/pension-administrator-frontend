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
import org.scalatest.{AsyncFlatSpec, Matchers}
import play.api.http.Status
import play.api.libs.json._
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import utils.WireMockHelper

class DeRegistrationConnectorSpec extends AsyncFlatSpec with Matchers with WireMockHelper {

  import DeRegistrationConnectorSpec._

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  "canDeRegister" should "return the boolean true/false for a valid response" in {

    server.stubFor(
      get(urlEqualTo(canRegisterUrl))
        .willReturn(
          ok(Json.stringify(JsBoolean(true)))
        )
    )

    val connector = injector.instanceOf[DeRegistrationConnector]

    connector.canDeRegister(psaId).map(response =>
      response shouldBe true
    )
  }

  it should "throw JsResultException is the data returned is not boolean" in {
    server.stubFor(
      get(urlEqualTo(canRegisterUrl))
        .willReturn(
          ok(Json.stringify(JsString("invalid data")))
        )
    )

    val connector = injector.instanceOf[DeRegistrationConnector]

    recoverToSucceededIf[JsResultException] {
      connector.canDeRegister(psaId)
    }
  }

  it should "throw a Upstream5xxResponse if service unavailable is returned" in {
    server.stubFor(
      get(urlEqualTo(canRegisterUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.SERVICE_UNAVAILABLE)
        )
    )
    val connector = injector.instanceOf[DeRegistrationConnector]

    recoverToSucceededIf[Upstream5xxResponse] {
      connector.canDeRegister(psaId)
    }
  }
}

object DeRegistrationConnectorSpec {
  private val psaId = "test-psa-id"
  private val canRegisterUrl = s"/pension-administrator/can-deregister/$psaId"
}




