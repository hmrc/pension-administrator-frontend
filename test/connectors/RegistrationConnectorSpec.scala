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
import models.{TolerantAddress, Organisation, OrganisationTypeEnum}
import org.scalatest._
import play.api.http.Status
import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import utils.WireMockHelper

class RegistrationConnectorSpec ()
  extends AsyncFlatSpec with Matchers with OptionValues with WireMockHelper {

  override protected def portConfigKey: String = "microservice.services.pensions-scheme.port"

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val utr = "1234567890"
  private val path = "/pensions-scheme/register-with-id/organisation"

  private val organisation = Organisation("Test Ltd", OrganisationTypeEnum.CorporateBody)

  private val expectedAddress = TolerantAddress(
    Some("Building Name"),
    Some("1 Main Street"),
    Some("Some Village"),
    Some("Some Town"),
    Some("GB"),
    Some("ZZ1 1ZZ")
  )

  private val validResponse = Json.obj(
    "safeId" -> "",
    "isEditable" -> false,
    "isAnAgent" -> false,
    "isAnIndividual" -> false,
    "organisation" -> Json.obj(
      "organisationName" -> organisation.organisationName,
      "organisationType" -> organisation.organisationType.toString
    ),
    "address" -> Json.obj(
      "addressLine1" -> expectedAddress.addressLine1.value,
      "addressLine2" -> expectedAddress.addressLine2.value,
      "addressLine3" -> expectedAddress.addressLine3.value,
      "addressLine4" -> expectedAddress.addressLine4.value,
      "countryCode" -> expectedAddress.country.value,
      "postalCode" -> expectedAddress.postcode.value
    ),
    "contactDetails" -> Json.obj()
  )

  private val invalidResponse = Json.obj(
    "invalid-element" -> "Meh!"
  )

  "CompanyAddressConnector" should "return the address given a valid UTR" in {

    server.stubFor(
      post(urlEqualTo(path))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validResponse))
        )
    )

    val connector = injector.instanceOf[RegistrationConnectorImpl]
    connector.registerWithIdOrganisation(utr, organisation).map { response =>
      response.address shouldBe expectedAddress
    }

  }

  it should "only accept valid requests with status 200 OK" in {

    server.stubFor(
      post(urlEqualTo(path))
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validResponse))
        )
    )

    val connector = injector.instanceOf[RegistrationConnectorImpl]
    recoverToSucceededIf[IllegalArgumentException] {
      connector.registerWithIdOrganisation(utr, organisation)
    }

  }

  it should "propagate exceptions from HttpClient" in {

    server.stubFor(
      post(urlEqualTo(path))
        .willReturn(
          aResponse()
            .withStatus(Status.NOT_FOUND)
        )
    )

    val connector = injector.instanceOf[RegistrationConnectorImpl]
    recoverToSucceededIf[NotFoundException] {
      connector.registerWithIdOrganisation(utr, organisation)
    }

  }

  it should "identify JSON parse errors" in {

    server.stubFor(
      post(urlEqualTo(path))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(invalidResponse))
        )
    )

    val connector = injector.instanceOf[RegistrationConnectorImpl]
    recoverToSucceededIf[JsResultException] {
      connector.registerWithIdOrganisation(utr, organisation)
    }

  }

}
