/*
 * Copyright 2023 HM Revenue & Customs
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
import models.enumeration.JourneyType
import org.scalatest.RecoverMethods
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.http.Status
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

class EmailConnectorSpec extends AsyncWordSpec with Matchers with WireMockHelper with RecoverMethods {

  override protected def portConfigKey: String = "microservice.services.email.port"

  private val url: String = "/hmrc/email"
  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private lazy val connector = injector.instanceOf[EmailConnector]
  private val testEmailAddress = "test@test.com"
  private val testTemplate = "testTemplate"
  private val testPsaId = PsaId("A1234567")
  private val templateParams = Map("psaName" -> "test psa")

  ".sendEmail" must {
    "return an EmailSent" when {
      "email sent succesfully with status 202 (Accepted)" in {
        server.stubFor(
          post(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withHeader("Content-Type", "application/json")
          )
        )
        connector.sendEmail(testEmailAddress, testTemplate, templateParams, testPsaId, JourneyType.PSA).map {
          result =>
            result mustBe EmailSent
        }
      }
    }

    "return an EmailNotSent" when {
      "email service returns back with 204 (No Content)" in {
        server.stubFor(
          post(urlEqualTo(url)).willReturn(
            noContent()
              .withHeader("Content-Type", "application/json")
          )
        )
        connector.sendEmail(testEmailAddress, testTemplate, templateParams, testPsaId, JourneyType.PSA).map {
          result =>
            result mustBe EmailNotSent
        }
      }
    }

    "return an EmailNotSent" when {
      "email service is down" in {
        server.stubFor(
          post(urlEqualTo(url)).willReturn(
            serviceUnavailable()
              .withHeader("Content-Type", "application/json")
          )
        )

        connector.sendEmail(testEmailAddress, testTemplate, templateParams, testPsaId, JourneyType.PSA).map {
          result =>
            result mustBe EmailNotSent
        }
      }
    }
    "return an EmailSent" when {
      "variation email sent succesfully with status 202 (Accepted)" in {
        server.stubFor(
          post(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(Status.ACCEPTED)
              .withHeader("Content-Type", "application/json")
          )
        )
        connector.sendEmail(testEmailAddress, testTemplate, templateParams, testPsaId, JourneyType.VARIATION).map {
          result =>
            result mustBe EmailSent
        }
      }
    }

    "return an EmailNotSent" when {
      "variation email service returns back with 204 (No Content)" in {
        server.stubFor(
          post(urlEqualTo(url)).willReturn(
            noContent()
              .withHeader("Content-Type", "application/json")
          )
        )
        connector.sendEmail(testEmailAddress, testTemplate, templateParams, testPsaId, JourneyType.VARIATION).map {
          result =>
            result mustBe EmailNotSent
        }
      }
    }

    "return an EmailNotSent" when {
      "variation email service is down" in {
        server.stubFor(
          post(urlEqualTo(url)).willReturn(
            serviceUnavailable()
              .withHeader("Content-Type", "application/json")
          )
        )

        connector.sendEmail(testEmailAddress, testTemplate, templateParams, testPsaId, JourneyType.VARIATION).map {
          result =>
            result mustBe EmailNotSent
        }
      }
    }
  }
}
