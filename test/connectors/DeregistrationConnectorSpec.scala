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
import models.register.{KnownFact, KnownFacts}
import org.scalatest.{AsyncWordSpec, MustMatchers, RecoverMethods}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, Upstream4xxResponse}
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global

class DeregistrationConnectorSpec extends AsyncWordSpec with MustMatchers with WireMockHelper with RecoverMethods {

  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val testPsaId = "test-psa-id"

  private def deEnrolUrl: String = s"/pension-administrator/deregister-psa/$testPsaId"

  private lazy val connector = injector.instanceOf[DeregistrationConnector]

  ".stopBeingPSA" must {

    "return a successful response" when {
      "enrolments returns code NO_CONTENT" which {
        "means the de-enrolment was successful" in {

          server.stubFor(
            delete(urlEqualTo(deEnrolUrl))
              .willReturn(
                noContent
              )
          )

          connector.stopBeingPSA(testPsaId) map {
            result =>
              result.status mustEqual NO_CONTENT
          }

        }
      }
    }

    "return a failure" when {
      "de-enrolment returns BAD_REQUEST" in {

          server.stubFor(
            delete(urlEqualTo(deEnrolUrl))
              .willReturn(
                badRequest
              )
          )

          recoverToSucceededIf[BadRequestException] {
            connector.stopBeingPSA(testPsaId)
          }

      }

      "enrolments returns UNAUTHORISED" which {
        "means missing or incorrect MDTP bearer token" in {

          server.stubFor(
            delete(urlEqualTo(deEnrolUrl))
              .willReturn(
                unauthorized
              )
          )

          recoverToSucceededIf[Upstream4xxResponse] {
            connector.stopBeingPSA(testPsaId)
          }

        }
      }
    }

  }

}
