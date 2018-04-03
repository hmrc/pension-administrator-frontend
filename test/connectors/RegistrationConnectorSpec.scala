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
import models.{Organisation, OrganisationTypeEnum, TolerantAddress, TolerantIndividual}
import org.scalatest._
import play.api.http.Status
import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import utils.WireMockHelper

import scala.concurrent.ExecutionContext

class RegistrationConnectorSpec ()
  extends AsyncFlatSpec with Matchers with OptionValues with WireMockHelper {

  override protected def portConfigKey: String = "microservice.services.pensions-scheme.port"

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private val utr = "1234567890"

  private val organizationPath = "/pensions-scheme/register-with-id/organisation"
  private val individualPath = "/pensions-scheme/register-with-id/individual"

  private val organisation = Organisation("Test Ltd", OrganisationTypeEnum.CorporateBody)

  private val expectedIndividual = TolerantIndividual(
    Some("John"),
    Some("T"),
    Some("Doe")
  )

  private val expectedAddress = TolerantAddress(
    Some("Building Name"),
    Some("1 Main Street"),
    Some("Some Village"),
    Some("Some Town"),
    Some("GB"),
    Some("ZZ1 1ZZ")
  )

  private val validOrganizationResponse = Json.obj(
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

  private val validIndividualResponse = Json.obj(
    "safeId" -> "",
    "isEditable" -> false,
    "isAnAgent" -> false,
    "isAnIndividual" -> true,
    "individual" -> Json.obj(
      "firstName" -> expectedIndividual.firstName.value,
      "middleName" -> expectedIndividual.middleName.value,
      "lastName" -> expectedIndividual.lastName.value,
      "dateOfBirth" -> ""
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

  "registerWithIdOrganisation" should "return the address given a valid UTR" in {

    server.stubFor(
      post(urlEqualTo(organizationPath))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validOrganizationResponse))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    connector.registerWithIdOrganisation(utr, organisation).map { response =>
      response.address shouldBe expectedAddress
    }

  }

  it should "only accept responses with status 200 OK" in {

    server.stubFor(
      post(urlEqualTo(organizationPath))
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validOrganizationResponse))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    recoverToSucceededIf[IllegalArgumentException] {
      connector.registerWithIdOrganisation(utr, organisation)
    }

  }

  it should "propagate exceptions from HttpClient" in {

    server.stubFor(
      post(urlEqualTo(organizationPath))
        .willReturn(
          aResponse()
            .withStatus(Status.NOT_FOUND)
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    recoverToSucceededIf[NotFoundException] {
      connector.registerWithIdOrganisation(utr, organisation)
    }

  }

  it should "identify JSON parse errors" in {

    server.stubFor(
      post(urlEqualTo(organizationPath))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(invalidResponse))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    recoverToSucceededIf[JsResultException] {
      connector.registerWithIdOrganisation(utr, organisation)
    }

  }

  it should "forward HTTP headers" in {

    val headerName = "test-header-name"
    val headerValue = "test-header-value"

    server.stubFor(
      post(urlEqualTo(organizationPath))
        .withHeader(headerName, equalTo(headerValue))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validOrganizationResponse))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]

    val hc: HeaderCarrier = HeaderCarrier(extraHeaders = Seq((headerName, headerValue)))
    val ec: ExecutionContext = implicitly[ExecutionContext]

    connector.registerWithIdOrganisation(utr, organisation)(hc, ec).map { _ =>
      succeed
    }

  }

  "registerWithIdIndividual" should "return the individual and address given a valid NINO" in {

    server.stubFor(
      post(urlEqualTo(individualPath))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validIndividualResponse))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    connector.registerWithIdIndividual().map { response =>
      response.individual shouldBe expectedIndividual
      response.address shouldBe expectedAddress
    }

  }

  it should "only accept responses with status 200 OK" in {

    server.stubFor(
      post(urlEqualTo(individualPath))
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validIndividualResponse))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    recoverToSucceededIf[IllegalArgumentException] {
      connector.registerWithIdIndividual()
    }

  }

  it should "propagate exceptions from HttpClient" in {

    server.stubFor(
      post(urlEqualTo(individualPath))
        .willReturn(
          aResponse()
            .withStatus(Status.NOT_FOUND)
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    recoverToSucceededIf[NotFoundException] {
      connector.registerWithIdIndividual()
    }

  }

  it should "identify JSON parse errors" in {

    server.stubFor(
      post(urlEqualTo(individualPath))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(invalidResponse))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    recoverToSucceededIf[JsResultException] {
      connector.registerWithIdIndividual()
    }

  }

  it should "forward HTTP headers" in {

    val headerName = "test-header-name"
    val headerValue = "test-header-value"

    server.stubFor(
      post(urlEqualTo(individualPath))
        .withHeader(headerName, equalTo(headerValue))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validIndividualResponse))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]

    val hc: HeaderCarrier = HeaderCarrier(extraHeaders = Seq((headerName, headerValue)))
    val ec: ExecutionContext = implicitly[ExecutionContext]

    connector.registerWithIdIndividual()(hc, ec).map { _ =>
      succeed
    }

  }

}
