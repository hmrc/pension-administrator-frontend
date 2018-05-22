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
import com.github.tomakehurst.wiremock.matching.UrlPattern
import org.scalatest.{AsyncWordSpec, MustMatchers, RecoverMethods, Succeeded}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, Upstream4xxResponse}
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global

class AuthenticationConnectorSpec extends AsyncWordSpec with MustMatchers with WireMockHelper with RecoverMethods {

  override protected def portConfigKey: String = "microservice.services.government-gateway-authentication.port"

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def urlMapping: UrlPattern = urlEqualTo("/government-gateway-authentication/refresh-profile")

  protected def configuration(switch: Boolean): Map[String, Any] = Map(
    portConfigKey -> server.port().toString,
    "auditing.enabled" -> false,
    "metrics.enabled" -> false,
    "microservice.services.feature-switches.refresh-profile" -> switch
  )

  protected def app(switch: Boolean = true): Application =
    new GuiceApplicationBuilder()
      .configure(configuration(switch))
      .build()

  ".refreshProfile" must {

    "return a successful response" when {
      "authenticator returns code NO_CONTENT" which {
        "means the auth profile has been refreshed" in {

          val connector = app().injector.instanceOf[AuthenticationConnector]

          server.stubFor(
            post(urlMapping)
              .willReturn(
                noContent
              )
          )

          connector.refreshProfile map {
            result =>
              server.verify(1, postRequestedFor(urlMapping))
              result.status mustEqual NO_CONTENT
          }

        }
      }
      "feature switch is off" which {
        "means API is not called" in {

          val connector = app(false).injector.instanceOf[AuthenticationConnector]

          server.stubFor(
            post(urlMapping)
              .willReturn(
                noContent
              )
          )

          connector.refreshProfile map { _ =>
            server.verify(0, postRequestedFor(urlMapping))
            Succeeded
          }

        }
      }
    }

    "return a failure" when {
      "authenticator returns UNAUTHORISED" which {
        "means bearer token is missing or invalid" in {

          val connector = app().injector.instanceOf[AuthenticationConnector]

          server.stubFor(
            post(urlMapping)
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

          val connector = app().injector.instanceOf[AuthenticationConnector]

          server.stubFor(
            post(urlMapping)
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
