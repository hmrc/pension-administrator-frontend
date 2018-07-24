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
import models._
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

  private val utr = "test-utr"
  private val nino = "test-nino"
  private val sapNumber = "test-sap-number"

  private val organizationPath = "/pensions-scheme/register-with-id/organisation"
  private val individualPath = "/pensions-scheme/register-with-id/individual"

  private val organisation = Organisation("Test Ltd", OrganisationTypeEnum.CorporateBody)
  private val legalStatus = RegistrationLegalStatus.LimitedCompany

  private val expectedIndividual = TolerantIndividual(
    Some("John"),
    Some("T"),
    Some("Doe")
  )

  private def expectedAddress(uk: Boolean) = TolerantAddress(
    Some("Building Name"),
    Some("1 Main Street"),
    Some("Some Village"),
    Some("Some Town"),
    Some("ZZ1 1ZZ"),
    if (uk) Some("GB") else Some("XX")
  )

  private def expectedAddressJson(address: TolerantAddress) = Json.obj(
    "addressLine1" -> address.addressLine1.value,
    "addressLine2" -> address.addressLine2.value,
    "addressLine3" -> address.addressLine3.value,
    "addressLine4" -> address.addressLine4.value,
    "countryCode" -> address.country.value,
    "postalCode" -> address.postcode.value
  )

  private def validOrganizationResponse(uk: Boolean) = Json.obj(
    "safeId" -> "",
    "sapNumber" -> sapNumber,
    "isEditable" -> false,
    "isAnAgent" -> false,
    "isAnIndividual" -> false,
    "organisation" -> Json.obj(
      "organisationName" -> organisation.organisationName,
      "organisationType" -> organisation.organisationType.toString
    ),
    "address" -> expectedAddressJson(expectedAddress(uk)),
    "contactDetails" -> Json.obj()
  )

  private def validIndividualResponse(uk: Boolean) = Json.obj(
    "safeId" -> "",
    "sapNumber" -> sapNumber,
    "isEditable" -> false,
    "isAnAgent" -> false,
    "isAnIndividual" -> true,
    "individual" -> Json.obj(
      "firstName" -> expectedIndividual.firstName.value,
      "middleName" -> expectedIndividual.middleName.value,
      "lastName" -> expectedIndividual.lastName.value,
      "dateOfBirth" -> ""
    ),
    "address" -> expectedAddressJson(expectedAddress(uk)),
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
            .withBody(Json.stringify(validOrganizationResponse(true)))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    connector.registerWithIdOrganisation(utr, organisation, legalStatus).map { registration =>
      registration.response.address shouldBe expectedAddress(true)
    }

  }

  it should "return the registration info for a company with a UK address" in {

    val info = RegistrationInfo(
      RegistrationLegalStatus.LimitedCompany,
      sapNumber,
      false,
      RegistrationCustomerType.UK,
      RegistrationIdType.UTR,
      utr
    )

    server.stubFor(
      post(urlEqualTo(organizationPath))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validOrganizationResponse(true)))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    connector.registerWithIdOrganisation(utr, organisation, legalStatus).map { registration =>
      registration.info shouldBe info
    }

  }

  it should "return the registration info for a company with a non-UK address" in {

    val info = RegistrationInfo(
      RegistrationLegalStatus.LimitedCompany,
      sapNumber,
      false,
      RegistrationCustomerType.NonUK,
      RegistrationIdType.UTR,
      utr
    )

    server.stubFor(
      post(urlEqualTo(organizationPath))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validOrganizationResponse(false)))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    connector.registerWithIdOrganisation(utr, organisation, legalStatus).map { registration =>
      registration.info shouldBe info
    }

  }

  it should "only accept responses with status 200 OK" in {

    server.stubFor(
      post(urlEqualTo(organizationPath))
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validOrganizationResponse(true)))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    recoverToSucceededIf[IllegalArgumentException] {
      connector.registerWithIdOrganisation(utr, organisation, legalStatus)
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
      connector.registerWithIdOrganisation(utr, organisation, legalStatus)
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
      connector.registerWithIdOrganisation(utr, organisation, legalStatus)
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
            .withBody(Json.stringify(validOrganizationResponse(true)))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]

    val hc: HeaderCarrier = HeaderCarrier(extraHeaders = Seq((headerName, headerValue)))
    val ec: ExecutionContext = implicitly[ExecutionContext]

    connector.registerWithIdOrganisation(utr, organisation, legalStatus)(hc, ec).map { _ =>
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
            .withBody(Json.stringify(validIndividualResponse(true)))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    connector.registerWithIdIndividual(nino).map { registration =>
      registration.response.individual shouldBe expectedIndividual
      registration.response.address shouldBe expectedAddress(true)
    }

  }

  it should "return the registration info for an individual with a UK address" in {

    val info = RegistrationInfo(
      RegistrationLegalStatus.Individual,
      sapNumber,
      false,
      RegistrationCustomerType.UK,
      RegistrationIdType.Nino,
      nino
    )

    server.stubFor(
      post(urlEqualTo(individualPath))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validIndividualResponse(true)))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    connector.registerWithIdIndividual(nino).map { registration =>
      registration.info shouldBe info
    }

  }

  it should "return the registration info for an individual with a Non-UK address" in {

    val info = RegistrationInfo(
      RegistrationLegalStatus.Individual,
      sapNumber,
      false,
      RegistrationCustomerType.NonUK,
      RegistrationIdType.Nino,
      nino
    )

    server.stubFor(
      post(urlEqualTo(individualPath))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validIndividualResponse(false)))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    connector.registerWithIdIndividual(nino).map { registration =>
      registration.info shouldBe info
    }

  }

  it should "only accept responses with status 200 OK" in {

    server.stubFor(
      post(urlEqualTo(individualPath))
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validIndividualResponse(true)))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    recoverToSucceededIf[IllegalArgumentException] {
      connector.registerWithIdIndividual(nino)
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
      connector.registerWithIdIndividual(nino)
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
      connector.registerWithIdIndividual(nino)
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
            .withBody(Json.stringify(validIndividualResponse(true)))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]

    val hc: HeaderCarrier = HeaderCarrier(extraHeaders = Seq((headerName, headerValue)))
    val ec: ExecutionContext = implicitly[ExecutionContext]

    connector.registerWithIdIndividual(nino)(hc, ec).map { _ =>
      succeed
    }

  }

}
