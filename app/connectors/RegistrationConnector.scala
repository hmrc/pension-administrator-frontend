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

import java.util.UUID.randomUUID

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import javax.inject.Singleton

import models._
import models.registrationnoid.RegistrationNoIdIndividualRequest
import org.joda.time.LocalDate
import play.Logger
import play.api.http.Status
import play.api.libs.json._
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

@ImplementedBy(classOf[RegistrationConnectorImpl])
trait RegistrationConnector {
  def registerWithIdOrganisation
  (utr: String, organisation: Organisation, legalStatus: RegistrationLegalStatus)
  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganizationRegistration]

  def registerWithIdIndividual
  (nino: String)
  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[IndividualRegistration]

  def registerWithNoIdOrganisation
  (name: String, address: Address)
  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RegistrationInfo]

  def registerWithNoIdIndividual
  (firstName: String, lastName: String, address: Address, dateOfBirth: LocalDate)
  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RegistrationInfo]
}

@Singleton
class RegistrationConnectorImpl @Inject()(http: HttpClient, config: FrontendAppConfig) extends RegistrationConnector {

  private val readsSapNumber: Reads[String] = (JsPath \ "sapNumber").read[String]

  override def registerWithIdOrganisation
  (utr: String, organisation: Organisation, legalStatus: RegistrationLegalStatus)
  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganizationRegistration] = {

    val url = config.registerWithIdOrganisationUrl

    val body = Json.obj(
      "utr" -> utr,
      "organisationName" -> organisation.organisationName,
      "organisationType" -> organisation.organisationType.toString
    )

    http.POST(url, body) map { response =>
      require(response.status == Status.OK, "The only valid response to registerWithIdOrganisation is 200 OK")

      val json = Json.parse(response.body)

      json.validate[OrganizationRegisterWithIdResponse] match {
        case JsSuccess(value, _) =>
          val info = registrationInfo(
            json,
            legalStatus,
            RegistrationCustomerType.fromAddress(value.address),
            Some(RegistrationIdType.UTR), Some(utr))
            OrganizationRegistration(value, info
          )
        case JsError(errors) => throw JsResultException(errors)
      }
    } andThen {
      case Failure(ex: NotFoundException) =>
        Logger.warn("Organisation not found with registerWithIdOrganisation", ex)
        ex
      case Failure(ex) =>
        Logger.error("Unable to connect to registerWithIdOrganisation", ex)
        ex
    }

  }

  override def registerWithIdIndividual
  (nino: String)
  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[IndividualRegistration] = {

    val url = config.registerWithIdIndividualUrl

    http.POSTEmpty(url) map { response =>
      require(response.status == Status.OK, "The only valid response to registerWithIdIndividual is 200 OK")

      val json = Json.parse(response.body)

      json.validate[IndividualRegisterWithIdResponse] match {
        case JsSuccess(value, _) =>
          val info = registrationInfo(
            json,
            RegistrationLegalStatus.Individual,
            RegistrationCustomerType.fromAddress(value.address),
            Some(RegistrationIdType.Nino),
            Some(nino)
          )
          IndividualRegistration(value, info)
        case JsError(errors) => throw JsResultException(errors)
      }
    } andThen {
      case Failure(ex: NotFoundException) =>
        Logger.warn("Individual not found with registerWithIdIndividual", ex)
        ex
      case Failure(ex) =>
        Logger.error("Unable to connect to registerWithIdIndividual", ex)
        ex
    }

  }

  override def registerWithNoIdOrganisation
  (name: String, address: Address)
  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RegistrationInfo] = {

    val organisationRegistrant = OrganisationRegistrant(OrganisationName(name), address)

    http.POST(config.registerWithNoIdOrganisationUrl, Json.toJson(organisationRegistrant)) map { response =>
      require(response.status == Status.OK, "The only valid response to registerWithNoIdOrganisation is 200 OK")
      val jsValue = Json.parse(response.body)

      registrationInfo(
        jsValue,
        RegistrationLegalStatus.LimitedCompany,
        RegistrationCustomerType.NonUK,
        None,
        None
      )
    } andThen {
      case Failure(ex) =>
        Logger.error("Unable to connect to registerWithNoIdOrganisation", ex)
        ex
    }
  }

  override def registerWithNoIdIndividual
  (firstName: String, lastName: String, address: Address, dateOfBirth: LocalDate)
  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[RegistrationInfo] = {

    val registrant = RegistrationNoIdIndividualRequest(firstName, lastName, dateOfBirth, address)

    http.POST(config.registerWithNoIdIndividualUrl, Json.toJson(registrant)) map { response =>
      require(response.status == Status.OK, "The only valid response to registerWithNoIdIndividual is 200 OK")
      val jsValue = Json.parse(response.body)

      registrationInfo(
        jsValue,
        RegistrationLegalStatus.Individual,
        RegistrationCustomerType.NonUK,
        None,
        None
      )
    } andThen {
      case Failure(ex) =>
        Logger.error("Unable to connect to registerWithNoIdIndividual", ex)
        ex
    }
  }

  private def registrationInfo(
                                json: JsValue,
                                legalStatus: RegistrationLegalStatus,
                                customerType: RegistrationCustomerType,
                                idType: Option[RegistrationIdType],
                                idNumber: Option[String]): RegistrationInfo = {

    json.validate[String](readsSapNumber) match {
      case JsSuccess(sapNumber, _) =>
        RegistrationInfo(legalStatus, sapNumber, false, customerType, idType, idNumber)
      case JsError(errors) => throw JsResultException(errors)
    }
  }
}
