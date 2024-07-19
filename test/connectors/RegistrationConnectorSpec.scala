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
import models._
import models.registrationnoid.RegistrationNoIdIndividualRequest
import org.scalatest._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsResultException, Json}
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException}
import utils.{UnrecognisedHttpResponseException, WireMockHelper}

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class RegistrationConnectorSpec()
  extends AsyncFlatSpec with Matchers with OptionValues with WireMockHelper {

  import RegistrationConnectorSpec._

  override protected lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        portConfigKey -> server.port().toString,
        "auditing.enabled" -> false,
        "metrics.enabled" -> false
      )
      .build()

  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global


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
    connector.registerWithIdOrganisation(utr, organisation, legalStatus).map {
      case registration@(_: OrganizationRegistration) => registration.response.address shouldBe expectedAddress(true)
    }

  }

  it should "return the registration info for a company with a UK address" in {

    val info = RegistrationInfo(
      RegistrationLegalStatus.LimitedCompany,
      sapNumber,
      noIdentifier = false,
      RegistrationCustomerType.UK,
      Some(RegistrationIdType.UTR),
      Some(utr)
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
    connector.registerWithIdOrganisation(utr, organisation, legalStatus).map {
      case registration@(_: OrganizationRegistration) => registration.info shouldBe info
    }

  }

  it should "return the registration info for a company with a non-UK address" in {

    val info = RegistrationInfo(
      RegistrationLegalStatus.LimitedCompany,
      sapNumber,
      noIdentifier = false,
      RegistrationCustomerType.NonUK,
      Some(RegistrationIdType.UTR),
      Some(utr)
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
    connector.registerWithIdOrganisation(utr, organisation, legalStatus).map {
      case registration@(_: OrganizationRegistration) => registration.info shouldBe info
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
    recoverToSucceededIf[UnrecognisedHttpResponseException] {
      connector.registerWithIdOrganisation(utr, organisation, legalStatus)
    }

  }

  it should "propagate exceptions from HttpClient" in {

    server.stubFor(
      post(urlEqualTo(organizationPath))
        .willReturn(
          aResponse()
            .withStatus(Status.BAD_REQUEST)
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    recoverToSucceededIf[BadRequestException] {
      connector.registerWithIdOrganisation(utr, organisation, legalStatus)
    }

  }

  it should "return a result of type OrganizationRegistrationStatus if details not found" in {

    server.stubFor(
      post(urlEqualTo(organizationPath))
        .willReturn(
          aResponse()
            .withStatus(Status.NOT_FOUND)
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    connector.registerWithIdOrganisation(utr, organisation, legalStatus).map { result =>
      result shouldBe a[OrganizationRegistrationStatus]
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
    val executionContext: ExecutionContext = implicitly[ExecutionContext]

    connector.registerWithIdOrganisation(utr, organisation, legalStatus)(hc, executionContext).map { _ =>
      succeed
    }

  }

  "registerWithIdIndividual" should "return the individual and address given a valid NINO" in {
    val postRequestBody = Json.obj("nino" -> "AB123456C")
    server.stubFor(
      post(urlEqualTo(individualPath))
        .withRequestBody(equalToJson(Json.stringify(postRequestBody)))
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

  it should "return the individual and address given a valid NINO when manual Iv is disabled" in {
    lazy val appWithIvDisabled: Application = new GuiceApplicationBuilder()
      .configure(
        portConfigKey -> server.port().toString,
        "auditing.enabled" -> false,
        "metrics.enabled" -> false
      ).build()

    val injector = appWithIvDisabled.injector
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
      noIdentifier = false,
      RegistrationCustomerType.UK,
      Some(RegistrationIdType.Nino),
      Some(nino.nino)
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
      noIdentifier = false,
      RegistrationCustomerType.NonUK,
      Some(RegistrationIdType.Nino),
      Some(nino.nino)
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
    recoverToSucceededIf[UnrecognisedHttpResponseException] {
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
    val executionContext: ExecutionContext = implicitly[ExecutionContext]

    connector.registerWithIdIndividual(nino)(hc, executionContext).map { _ =>
      succeed
    }

  }

  "registerWithNoIdOrganisation" should "return successfully given a valid name and address" in {

    server.stubFor(
      post(urlEqualTo(noIdOrganisationPath))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validNonUkResponse))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    connector.registerWithNoIdOrganisation(organisation.organisationName, expectedAddress(uk = false).toAddress.get, legalStatus).map { registration =>
      registration.sapNumber shouldBe sapNumber
    }
  }

  it should "return successfully with noIdentifier set to true" in {

    server.stubFor(
      post(urlEqualTo(noIdOrganisationPath))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validNonUkResponse))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    connector.registerWithNoIdOrganisation(organisation.organisationName, expectedAddress(uk = false).toAddress.get, legalStatus).map { registration =>
      registration.noIdentifier shouldBe true
    }
  }


  it should "only accept responses with status 200 OK" in {

    server.stubFor(
      post(urlEqualTo(noIdOrganisationPath))
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
            .withHeader("Content-Type", "application/json")
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    recoverToSucceededIf[UnrecognisedHttpResponseException] {
      connector.registerWithNoIdOrganisation(organisation.organisationName, expectedAddress(uk = false).toAddress.get, legalStatus)
    }

  }

  it should "propagate exceptions from HttpClient" in {

    server.stubFor(
      post(urlEqualTo(noIdOrganisationPath))
        .willReturn(
          aResponse()
            .withStatus(Status.NOT_FOUND)
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    recoverToSucceededIf[NotFoundException] {
      connector.registerWithNoIdOrganisation(organisation.organisationName, expectedAddress(uk = false).toAddress.get, legalStatus)
    }

  }

  "registerWithNoIdIndividual" should "return successfully given a valid name, dob and address" in {

    server.stubFor(
      post(urlEqualTo(noIdIndividualPath))
        .withRequestBody(equalToJson(Json.stringify(registerWithoutIdIndividualRequest)))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validNonUkResponse))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    connector.registerWithNoIdIndividual(firstName, lastName, expectedAddress(uk = false).toAddress.get, individualDateOfBirth).map {
      registration =>
        registration.sapNumber shouldBe sapNumber
    }
  }

  it should "return successfully with noIdentifier equal to true" in {

    server.stubFor(
      post(urlEqualTo(noIdIndividualPath))
        .withRequestBody(equalToJson(Json.stringify(registerWithoutIdIndividualRequest)))
        .willReturn(
          aResponse()
            .withStatus(Status.OK)
            .withHeader("Content-Type", "application/json")
            .withBody(Json.stringify(validNonUkResponse))
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    connector.registerWithNoIdIndividual(firstName, lastName, expectedAddress(uk = false).toAddress.get, individualDateOfBirth).map {
      registration =>
        registration.noIdentifier shouldBe true
    }
  }


  it should "only accept responses with status 200 OK" in {

    server.stubFor(
      post(urlEqualTo(noIdIndividualPath))
        .willReturn(
          aResponse()
            .withStatus(Status.ACCEPTED)
            .withHeader("Content-Type", "application/json")
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    recoverToSucceededIf[UnrecognisedHttpResponseException] {
      connector.registerWithNoIdIndividual(firstName, lastName, expectedAddress(uk = false).toAddress.get, individualDateOfBirth)
    }

  }


  it should "propagate exceptions from HttpClient" in {

    server.stubFor(
      post(urlEqualTo(noIdIndividualPath))
        .willReturn(
          aResponse()
            .withStatus(Status.NOT_FOUND)
        )
    )

    val connector = injector.instanceOf[RegistrationConnector]
    recoverToSucceededIf[NotFoundException] {
      connector.registerWithNoIdIndividual(firstName, lastName, expectedAddress(uk = false).toAddress.get, individualDateOfBirth)
    }

  }

}

object RegistrationConnectorSpec extends OptionValues {
  private val utr = "test-utr"
  private val nino = Nino("AB123456C")
  private val sapNumber = "test-sap-number"

  private val organizationPath = "/pension-administrator/register-with-id/organisation"
  private val noIdOrganisationPath = "/pension-administrator/register-with-no-id/organisation"
  private val noIdIndividualPath = "/pension-administrator/register-with-no-id/individual"
  private val individualPath = "/pension-administrator/register-with-id/individual"

  private val organisation = Organisation("Test Ltd", OrganisationTypeEnum.CorporateBody)
  private val firstName = "John"
  private val lastName = "Doe"
  private val individualDateOfBirth = LocalDate.now()
  private val legalStatus = RegistrationLegalStatus.LimitedCompany
  private val registerWithoutIdIndividualRequest = Json.toJson(
    RegistrationNoIdIndividualRequest(firstName, lastName, individualDateOfBirth, expectedAddress(uk = false).toAddress.get))

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
    "countryCode" -> address.countryOpt.value,
    "postalCode" -> address.postcode.value
  )

  private def validNonUkResponse = Json.obj(
    "safeId" -> "",
    "sapNumber" -> sapNumber
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

}
