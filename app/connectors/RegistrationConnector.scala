/*
 * Copyright 2024 HM Revenue & Customs
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

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import models._
import models.registrationnoid.RegistrationNoIdIndividualRequest
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, NotFoundException, StringContextOps}
import utils.HttpResponseHelper

import java.time.LocalDate
import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

@ImplementedBy(classOf[RegistrationConnectorImpl])
trait RegistrationConnector {
  def registerWithIdOrganisation(
                                  utr: String,
                                  organisation: Organisation,
                                  legalStatus: RegistrationLegalStatus
                                )(
                                  implicit hc: HeaderCarrier,
                                  ec: ExecutionContext
                                ): Future[OrganizationRegistrationStatus]

  def registerWithIdIndividual(
                                nino: Nino
                              )(
                                implicit hc: HeaderCarrier,
                                ec: ExecutionContext
                              ): Future[IndividualRegistration]

  def registerWithNoIdOrganisation(
                                    name: String,
                                    address: Address,
                                    legalStatus: RegistrationLegalStatus
                                  )(
                                    implicit hc: HeaderCarrier,
                                    ec: ExecutionContext
                                  ): Future[RegistrationInfo]

  def registerWithNoIdIndividual(
                                  firstName: String,
                                  lastName: String,
                                  address: Address,
                                  dateOfBirth: LocalDate
                                )(
                                  implicit hc: HeaderCarrier,
                                  ec: ExecutionContext
                                ): Future[RegistrationInfo]
}

@Singleton
class RegistrationConnectorImpl @Inject()(httpV2Client: HttpClientV2, config: FrontendAppConfig)
  extends RegistrationConnector
    with HttpResponseHelper {

  private val logger = Logger(classOf[RegistrationConnectorImpl])

  private val readsSapNumber: Reads[String] = (JsPath \ "sapNumber").read[String]

  override def registerWithIdOrganisation(
                                           utr: String,
                                           organisation: Organisation,
                                           legalStatus: RegistrationLegalStatus
                                         )(
                                           implicit hc: HeaderCarrier,
                                           ec: ExecutionContext
                                         ): Future[OrganizationRegistrationStatus] = {

    val url = url"${config.registerWithIdOrganisationUrl}"

    val body = Json.obj(
      "utr" -> utr,
      "organisationName" -> organisation.organisationName,
      "organisationType" -> organisation.organisationType.toString
    )

    httpV2Client.post(url).withBody(body).execute[HttpResponse] map { response =>
      response.status match {
        case OK =>
          val json = Json.parse(response.body)
          json.validate[OrganizationRegisterWithIdResponse] match {
            case JsSuccess(value, _) =>
              OrganizationRegistration(
                response = value,
                info = registrationInfo(
                  json = json,
                  legalStatus = legalStatus,
                  customerType = RegistrationCustomerType.fromAddress(value.address),
                  idType = Some(RegistrationIdType.UTR), idNumber = Some(utr),
                  noIdentifier = false
                )
              )
            case JsError(errors) =>
              throw JsResultException(errors)
          }
        case NOT_FOUND => OrganisationNotFound
        case _ =>
          handleErrorResponse("POST", url.toString)(response)
      }

    } andThen {
      case Failure(ex: NotFoundException) =>
        logger.warn("Organisation not found with registerWithIdOrganisation", ex)
        ex
      case Failure(ex) =>
        logger.error("Unable to connect to registerWithIdOrganisation", ex)
        ex
    }
  }

  override def registerWithIdIndividual(
                                         nino: Nino
                                       )(
                                         implicit hc: HeaderCarrier,
                                         ec: ExecutionContext
                                       ): Future[IndividualRegistration] = {

    val url = url"${config.registerWithIdIndividualUrl}"

    httpV2Client.post(url).withBody(Json.obj("nino" -> nino)).execute[HttpResponse] map {
      response =>
        response.status match {
          case OK =>
            val json = Json.parse(response.body)

            json.validate[IndividualRegisterWithIdResponse] match {
              case JsSuccess(value, _) =>
                IndividualRegistration(
                  response = value,
                  info = registrationInfo(
                    json = json,
                    legalStatus = RegistrationLegalStatus.Individual,
                    customerType = RegistrationCustomerType.fromAddress(value.address),
                    idType = Some(RegistrationIdType.Nino),
                    idNumber = Some(nino.nino),
                    noIdentifier = false
                  )
                )
              case JsError(errors) =>
                throw JsResultException(errors)
            }
          case _ =>
            handleErrorResponse("POST", url.toString)(response)
        }
    } andThen {
      case Failure(ex: NotFoundException) =>
        logger.warn("Individual not found with registerWithIdIndividual", ex)
        ex
      case Failure(ex) =>
        logger.error("Unable to connect to registerWithIdIndividual", ex)
        ex
    }

  }

  def registerWithNoIdOrganisation(
                                    name: String,
                                    address: Address,
                                    legalStatus: RegistrationLegalStatus
                                  )(
                                    implicit hc: HeaderCarrier,
                                    ec: ExecutionContext
                                  ): Future[RegistrationInfo] = {

    val organisationRegistrant = OrganisationRegistrant(OrganisationName(name), address)
    val url = url"${config.registerWithNoIdOrganisationUrl}"

    httpV2Client.post(url).withBody(Json.toJson(organisationRegistrant)).execute[HttpResponse] map {
      response =>
        response.status match {
          case OK =>
            registrationInfo(
              json = Json.parse(response.body),
              legalStatus = legalStatus,
              customerType = RegistrationCustomerType.NonUK,
              idType = None,
              idNumber = None,
              noIdentifier = true
            )
          case _ =>
            handleErrorResponse("POST", url.toString)(response)
        }
    } andThen {
      case Failure(ex) =>
        logger.error("Unable to connect to registerWithNoIdOrganisation", ex)
        ex
    }
  }

  def registerWithNoIdIndividual(
                                  firstName: String,
                                  lastName: String,
                                  address: Address,
                                  dateOfBirth: LocalDate
                                )(
                                  implicit hc: HeaderCarrier,
                                  ec: ExecutionContext
                                ): Future[RegistrationInfo] = {

    val registrant = RegistrationNoIdIndividualRequest(firstName, lastName, dateOfBirth, address)
    val url = url"${config.registerWithNoIdIndividualUrl}"
    httpV2Client.post(url).withBody(Json.toJson(registrant)).execute[HttpResponse] map { response =>
      response.status match {
        case OK =>
          registrationInfo(
            json = Json.parse(response.body),
            legalStatus = RegistrationLegalStatus.Individual,
            customerType = RegistrationCustomerType.NonUK,
            idType = None,
            idNumber = None,
            noIdentifier = true
          )
        case _ =>
          handleErrorResponse("POST", url.toString)(response)
      }
    } andThen {
      case Failure(ex) =>
        logger.error("Unable to connect to registerWithNoIdIndividual", ex)
        ex
    }
  }

  private def registrationInfo(
                                json: JsValue,
                                legalStatus: RegistrationLegalStatus,
                                customerType: RegistrationCustomerType,
                                idType: Option[RegistrationIdType],
                                idNumber: Option[String],
                                noIdentifier: Boolean
                              ): RegistrationInfo = {

    json.validate[String](readsSapNumber) match {
      case JsSuccess(sapNumber, _) =>
        RegistrationInfo(
          legalStatus = legalStatus,
          sapNumber = sapNumber,
          noIdentifier = noIdentifier,
          customerType = customerType,
          idType = idType,
          idNumber = idNumber
        )
      case JsError(errors) =>
        throw JsResultException(errors)
    }
  }
}
