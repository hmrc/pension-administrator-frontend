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
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, Upstream4xxResponse}
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global

class TaxEnrolmentsConnectorSpec extends AsyncWordSpec with MustMatchers with WireMockHelper with RecoverMethods {

  override protected def portConfigKey: String = "microservice.services.tax-enrolments.port"

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val testPsaId = "test-psa-id"

  private def url: String = s"/tax-enrolments/enrolments/${Enrol(testPsaId).key}"

  private lazy val connector = injector.instanceOf[TaxEnrolmentsConnector]

  ".enrol" must {

    "return a successful response" when {
      "enrolments returns code NO_CONTENT" which {
        "means the enrolment was updated or created successfully" in {

          val knownFacts = KnownFacts(Set(KnownFact("NINO", "JJ123456P")))

          server.stubFor(
            put(urlEqualTo(url))
              .willReturn(
                noContent
              )
          )

          connector.enrol(testPsaId, knownFacts) map {
            result =>
              result.status mustEqual NO_CONTENT
          }

        }
      }
    }
    "return a failure" when {
      "enrolments returns BAD_REQUEST" which {
        "means the POST body wasn't as expected" in {

          val knownFacts = KnownFacts(Set.empty[KnownFact])

          server.stubFor(
            put(urlEqualTo(url))
              .willReturn(
                badRequest
              )
          )

          recoverToSucceededIf[BadRequestException] {
            connector.enrol(testPsaId, knownFacts)
          }

        }
      }
      "enrolments returns UNAUTHORISED" which {
        "means missing or incorrect MDTP bearer token" in {

          val knownFacts = KnownFacts(Set(KnownFact("NINO", "JJ123456P")))

          server.stubFor(
            put(urlEqualTo(url))
              .willReturn(
                unauthorized
              )
          )

          recoverToSucceededIf[Upstream4xxResponse] {
            connector.enrol(testPsaId, knownFacts)
          }

        }
      }
    }

  }

}
