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
import config.FrontendAppConfig
import models.Deregistration
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.http.Status
import play.api.http.Status.FORBIDDEN
import play.api.libs.json.{JsBoolean, JsResultException, JsString, Json}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import utils.PSAConstants.PSA_ACTIVE_RELATIONSHIP_EXISTS
import utils.{PsaActiveRelationshipExistsException, WireMockHelper}

import scala.concurrent.{ExecutionContext, Future}

class DeregistrationConnectorSpec extends AsyncFlatSpec with Matchers with WireMockHelper{

  import DeregistrationConnectorSpec._
  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  "Delete" should "return successful following a successful deletion" in {
    server.stubFor(
      delete(urlEqualTo(deregisterSelfUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.NO_CONTENT)
        )
    )

    val connector = injector.instanceOf[DeregistrationConnector]

    connector.stopBeingPSA(psaId).map {
      _ => server.findAll(deleteRequestedFor(urlEqualTo(deregisterSelfUrl))).size() shouldBe 1
    }
  }

  it should "throw BadRequestException for a 400 INVALID_PAYLOAD response" in {

    server.stubFor(
      delete(urlEqualTo(deregisterSelfUrl))
        .willReturn(
          badRequest
            .withHeader("Content-Type", "application/json")
            .withBody(errorResponse("INVALID_PAYLOAD"))
        )
    )

    val connector = injector.instanceOf[DeregistrationConnector]
    recoverToSucceededIf[BadRequestException] {
      connector.stopBeingPSA(psaId)
    }
  }

  "canDeRegister" should "return the boolean true/false for a valid response" in {
    val jsonString: String = Json.obj("canDeregister" -> JsBoolean(true),
      "isOtherPsaAttached" -> JsBoolean(false)).toString()
    server.stubFor(
      get(urlEqualTo(canRegisterUrl))
        .willReturn(
          ok(jsonString)
        )
    )

    val connector = injector.instanceOf[DeregistrationConnector]

    connector.canDeRegister(psaId).map(response =>
      response shouldBe Deregistration(canDeregister = true, isOtherPsaAttached = false)
    )
  }

  it should "throw JsResultException is the data returned is not boolean" in {
    server.stubFor(
      get(urlEqualTo(canRegisterUrl))
        .willReturn(
          ok(Json.stringify(JsString("invalid data")))
        )
    )

    val connector = injector.instanceOf[DeregistrationConnector]

    recoverToSucceededIf[JsResultException] {
      connector.canDeRegister(psaId)
    }
  }

  it should "throw a UpstreamErrorResponse if service unavailable is returned" in {
    server.stubFor(
      get(urlEqualTo(canRegisterUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.SERVICE_UNAVAILABLE)
        )
    )
    val connector = injector.instanceOf[DeregistrationConnector]

    recoverToSucceededIf[UpstreamErrorResponse] {
      connector.canDeRegister(psaId)
    }
  }

  it should "throw a UpstreamErrorResponse if service returns 404 (forbidden) response" in {
    server.stubFor(
      get(urlEqualTo(canRegisterUrl))
        .willReturn(
          aResponse()
            .withStatus(Status.FORBIDDEN)
        )
    )
    val connector = injector.instanceOf[DeregistrationConnector]

    recoverToSucceededIf[UpstreamErrorResponse] {
      connector.canDeRegister(psaId)
    }
  }

  "Delete" should "return FORBIDDEN with PSA_ACTIVE_RELATIONSHIP_EXISTS when PsaActiveRelationshipExistsException is thrown" in {
    val mockHttpClient = injector.instanceOf[HttpClientV2]
    val mockConfig = injector.instanceOf[FrontendAppConfig]
    val connector = new DeregistrationConnectorImpl(httpV2Client = mockHttpClient, config = mockConfig) {
      override def stopBeingPSA(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
        Future.failed(new PsaActiveRelationshipExistsException("PSA active relationship exists"))
          .recover {
            case _: PsaActiveRelationshipExistsException =>
              HttpResponse(FORBIDDEN, PSA_ACTIVE_RELATIONSHIP_EXISTS)
          }(ec)
      }
    }

    connector.stopBeingPSA(psaId).map { response =>
      response.status shouldBe FORBIDDEN
      response.body shouldBe PSA_ACTIVE_RELATIONSHIP_EXISTS
    }
  }
}


object DeregistrationConnectorSpec {
  implicit val hc : HeaderCarrier = HeaderCarrier()

  private val psaId = "238DAJFASS"
  private val deregisterSelfUrl = s"/pension-administrator/deregister-psa-self"
  private val canRegisterUrl = s"/pension-administrator/can-deregister/$psaId"

  def errorResponse(code: String): String = {
    Json.stringify(
      Json.obj(
        "code" -> code,
        "reason" -> s"Reason for $code"
      )
    )
  }
}

