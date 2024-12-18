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

import base.JsonFileReader
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.http.Status._
import play.api.libs.json.{JsResultException, JsValue, Json}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, UpstreamErrorResponse}
import utils.testhelpers.PsaSubscriptionBuilder._
import utils.{UserAnswers, WireMockHelper}

class SubscriptionConnectorSpec
  extends AsyncFlatSpec
    with Matchers
    with WireMockHelper {

  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  lazy val connector: SubscriptionConnector = injector.instanceOf[SubscriptionConnector]

  import SubscriptionConnectorSpec._

  "calling getSubscriptionDetails" should "return 200" in {

    server.stubFor(
      get(urlEqualTo(subscriptionDetailsSelfUrl))
        .willReturn(
          aResponse()
            .withStatus(OK).withBody(Json.toJson(individualJsonResponse).toString())
        )
    )

    connector.getSubscriptionDetailsSelf().map {
      result =>
        result shouldBe individualJsonResponse
        server.findAll(getRequestedFor(urlEqualTo(subscriptionDetailsSelfUrl))).size() shouldBe 1
    }

  }

  it should "throw badrequest if INVALID_PSAID" in {
    server.stubFor(
      get(urlEqualTo(subscriptionDetailsSelfUrl))
        .willReturn(
          aResponse()
            .withStatus(400).withBody("INVALID_PSAID")
        )
    )

    recoverToExceptionIf[PsaIdInvalidSubscriptionException] {
      connector.getSubscriptionDetailsSelf()
    } map {
      _ =>
        server.findAll(getRequestedFor(urlEqualTo(subscriptionDetailsSelfUrl))).size() shouldBe 1
    }
  }

  it should "throw badrequest if INVALID_CORRELATIONID" in {
    server.stubFor(
      get(urlEqualTo(subscriptionDetailsSelfUrl))
        .willReturn(
          aResponse()
            .withStatus(400).withBody("INVALID_CORRELATIONID")
        )
    )

    recoverToExceptionIf[CorrelationIdInvalidSubscriptionException] {
      connector.getSubscriptionDetailsSelf()
    } map {
      _ =>
        server.findAll(getRequestedFor(urlEqualTo(subscriptionDetailsSelfUrl))).size() shouldBe 1
    }
  }

  it should "throw Not Found" in {
    server.stubFor(
      get(urlEqualTo(subscriptionDetailsSelfUrl))
        .willReturn(
          notFound()
        )
    )

    recoverToExceptionIf[PsaIdNotFoundSubscriptionException] {
      connector.getSubscriptionDetailsSelf()
    } map {
      _ =>
        server.findAll(getRequestedFor(urlEqualTo(subscriptionDetailsSelfUrl))).size() shouldBe 1
    }
  }

  it should "throw UpstreamErrorResponse for internal server error" in {
    server.stubFor(
      get(urlEqualTo(subscriptionDetailsSelfUrl))
        .willReturn(
          serverError()
        )
    )

    recoverToExceptionIf[UpstreamErrorResponse] {
      connector.getSubscriptionDetailsSelf()
    } map {
      _ =>
        server.findAll(getRequestedFor(urlEqualTo(subscriptionDetailsSelfUrl))).size() shouldBe 1
    }
  }

  it should "throw Generic exception for all others" in {
    server.stubFor(
      get(urlEqualTo(subscriptionDetailsSelfUrl))
        .willReturn(
          serverError()
        )
    )

    recoverToExceptionIf[Exception] {
      connector.getSubscriptionDetailsSelf()
    } map {
      _ =>
        server.findAll(getRequestedFor(urlEqualTo(subscriptionDetailsSelfUrl))).size() shouldBe 1
    }
  }

  "getSubscriptionModel" should "return 200" in {

    server.stubFor(
      get(urlEqualTo(subscriptionDetailsSelfUrl))
        .willReturn(
          aResponse()
            .withStatus(OK).withBody(Json.toJson(psaSubscriptionIndividual).toString())
        )
    )

    connector.getSubscriptionModel().map {
      result =>
        result shouldBe psaSubscriptionIndividual
        server.findAll(getRequestedFor(urlEqualTo(subscriptionDetailsSelfUrl))).size() shouldBe 1
    }

  }

  it should "throw exception if failed to parse the json" in {

    server.stubFor(
      get(urlEqualTo(subscriptionDetailsSelfUrl))
        .willReturn(
          aResponse()
            .withStatus(OK).withBody(invalidResponse)
        )
    )

    recoverToExceptionIf[JsResultException] {
      connector.getSubscriptionModel()
    } map {
      _ =>
        server.findAll(getRequestedFor(urlEqualTo(subscriptionDetailsSelfUrl))).size() shouldBe 1
    }

  }

  "updateSubscriptionDetails" should "return successfully when received success response from DES" in {
    server.stubFor(
      put(urlEqualTo(updateSubscriptionDetailsUrl))
        .withRequestBody(equalToJson(Json.stringify(userAnswers.json)))
        .willReturn(
          ok()
            .withHeader("Content-Type", "application/json")
            .withBody("SUCCESSFUL RESPONSE")
        )
    )

    val connector = injector.instanceOf[SubscriptionConnector]

    connector.updateSubscriptionDetails(userAnswers) map(
      _ =>
        server.findAll(putRequestedFor(urlEqualTo(updateSubscriptionDetailsUrl))).size() shouldBe 1
    )
  }

  it should "throw InvalidSubscriptionPayloadException when bad request - INVALID_PAYLOAD response returned from DES" in {
    server.stubFor(
      put(urlEqualTo(updateSubscriptionDetailsUrl))
        .withRequestBody(equalToJson(Json.stringify(userAnswers.json)))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody(invalidPayloadResponse)
        )
    )

    val connector = injector.instanceOf[SubscriptionConnector]

    recoverToExceptionIf[InvalidSubscriptionPayloadException] {
      connector.updateSubscriptionDetails(userAnswers)
    } map {
      _ =>
        server.findAll(putRequestedFor(urlEqualTo(updateSubscriptionDetailsUrl))).size() shouldBe 1
    }
  }

  it should "throw UpstreamErrorResponse when internal server error response returned from DES" in {
    server.stubFor(
      put(urlEqualTo(updateSubscriptionDetailsUrl))
        .withRequestBody(equalToJson(Json.stringify(userAnswers.json)))
        .willReturn(
          serverError()
            .withHeader("Content-Type", "application/json")
            .withBody("INTERNAL SERVER ERROR")
        )
    )

    recoverToExceptionIf[UpstreamErrorResponse] {
      connector.updateSubscriptionDetails(userAnswers)
    } map {
      _ =>
        server.findAll(putRequestedFor(urlEqualTo(updateSubscriptionDetailsUrl))).size() shouldBe 1
    }
  }

  it should "throw BadRequestException when bad request response returned from DES" in {
    server.stubFor(
      put(urlEqualTo(updateSubscriptionDetailsUrl))
        .withRequestBody(equalToJson(Json.stringify(userAnswers.json)))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody("BAD REQUEST")
        )
    )

    recoverToExceptionIf[BadRequestException] {
      connector.updateSubscriptionDetails(userAnswers)
    } map {
      _ =>
        server.findAll(putRequestedFor(urlEqualTo(updateSubscriptionDetailsUrl))).size() shouldBe 1
    }
  }

}

object SubscriptionConnectorSpec extends JsonFileReader {

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  val psaId = "A1234567"
  val subscriptionDetailsSelfUrl = s"/pension-administrator/psa-subscription-details-self"
  private val updateSubscriptionDetailsUrl = "/pension-administrator/update-psa-subscription-details"

  val psaIdJson: String = Json.stringify(
    Json.obj(
      "psaId" -> s"$psaId"
    )
  )

  private val userAnswers = UserAnswers(Json.obj())

  val invalidResponse = """{"invalid" : "response"}"""
  val individualJsonResponse: JsValue = readJsonFromFile("/data/psaIndividualUserAnswers.json")
  private val invalidPayloadResponse =
    Json.stringify(
      Json.obj(
        "code" -> "INVALID_PAYLOAD",
        "reason" -> "test-reason"
      )
    )
}
