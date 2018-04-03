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

import javax.inject.Singleton

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import models.{IndividualRegisterWithIdResponse, Organisation, OrganizationRegisterWithIdResponse}
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

@ImplementedBy(classOf[RegistrationConnectorImpl])
trait RegistrationConnector {
  def registerWithIdOrganisation
      (utr: String, organisation: Organisation)
      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganizationRegisterWithIdResponse]

  def registerWithIdIndividual()
      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[IndividualRegisterWithIdResponse]
}

@Singleton
class RegistrationConnectorImpl @Inject()(http: HttpClient, config: FrontendAppConfig) extends RegistrationConnector {

  override def registerWithIdOrganisation
      (utr: String, organisation: Organisation)
      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[OrganizationRegisterWithIdResponse] = {

    val url = config.registerWithIdOrganisationUrl

    val body = Json.obj(
      "utr" -> utr,
      "organisationName" -> organisation.organisationName,
      "organisationType" -> organisation.organisationType.toString
    )

    http.POST(url, body) map {response =>
      require(response.status == Status.OK, "The only valid response to registerWithIdOrganisation is 200 OK")

      Json.parse(response.body).validate[OrganizationRegisterWithIdResponse] match {
        case JsSuccess(value, _) => value
        case JsError(errors) => throw JsResultException(errors)
      }
    } andThen {
      case Failure(ex) =>
        Logger.error("Unable to connect to registerWithIdOrganisation", ex)
        ex
    }

  }

  override def registerWithIdIndividual()
      (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[IndividualRegisterWithIdResponse] = {

    val url = config.registerWithIdIndividualUrl

    http.POSTEmpty(url) map { response =>
      require(response.status == Status.OK, "The only valid response to registerWithIdIndividual is 200 OK")

      Json.parse(response.body).validate[IndividualRegisterWithIdResponse] match {
        case JsSuccess(value, _) => value
        case JsError(errors) => throw JsResultException(errors)
      }
    } andThen {
      case Failure(ex) =>
        Logger.error("Unable to connect to registerWithIdOrganisation", ex)
        ex
    }

  }

}
