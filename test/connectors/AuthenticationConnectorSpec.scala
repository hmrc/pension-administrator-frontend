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
import models.register.{Enrol, KnownFact, KnownFacts}
import org.scalatest.{AsyncWordSpec, MustMatchers, RecoverMethods}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, Upstream4xxResponse, Upstream5xxResponse}
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global

class AuthenticationConnectorSpec extends AsyncWordSpec with MustMatchers with WireMockHelper with RecoverMethods {

  override protected def portConfigKey: String = "microservice.services.government-gateway-authentication.port"

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def url: String = "/government-gateway-authentication/refresh-profile"

  private lazy val connector = injector.instanceOf[AuthenticationConnector]

  ".refreshProfile" must {

    "return a successful response" when {
      "authenticator returns code NO_CONTENT" which {
        "means the auth profile has been refreshed" in {

          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(
                noContent
              )
          )

          connector.refreshProfile map {
            result =>
              result.status mustEqual NO_CONTENT
          }

        }
      }
    }

    "return a failure" when {
      "authenticator returns UNAUTHORISED" which {
        "means bearer token is missing or invalid" in {

          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(
                unauthorized
              )
          )

          recoverToSucceededIf[Upstream4xxResponse] {
            connector.refreshProfile
          }

        }
      }
      "authenticator returns FORBIDDEN" which {
        "user is not a GG user" in {

          server.stubFor(
            post(urlEqualTo(url))
              .willReturn(
                forbidden
              )
          )

          recoverToSucceededIf[Upstream4xxResponse] {
            connector.refreshProfile
          }

        }
      }

    }

  }

}
