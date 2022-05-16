/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpClient
import utils.HttpResponseHelper

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class IdentityVerificationConnectorImpl @Inject()(http: HttpClient, appConfig: FrontendAppConfig)
  extends IdentityVerificationConnector
    with HttpResponseHelper {

  private val logger = Logger(classOf[IdentityVerificationConnectorImpl])

  def startRegisterOrganisationAsIndividual(completionURL: String, failureURL: String)
                                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = {

    val jsonData = Json.obj(
      "origin" -> "PODS",
      "completionURL" -> completionURL,
      "failureURL" -> failureURL,
      "confidenceLevel" -> 200
    )

    val url = appConfig.ivRegisterOrganisationAsIndividualUrl

    http.POST[JsObject, HttpResponse](url, jsonData).map { response =>
      response.status match {
        case CREATED =>
          (response.json \ "link").validate[String] match {
            case JsSuccess(value, _) => value
            case JsError(errors) => throw JsResultException(errors)
          }
        case _ =>
          handleErrorResponse("POST", url)(response)
      }
    } andThen {
      logExceptions("Unable to start registration of organisation as individual via IV")
    }
  }

  override def retrieveNinoFromIV(journeyId: String)
                                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Nino]] = {
    val url = s"${appConfig.identityVerification}/identity-verification/journey/$journeyId"

    http.GET[HttpResponse](url).flatMap {
      case response if response.status equals OK =>
        Future.successful((response.json \ "nino").asOpt[Nino])
      case response =>
        logger.debug(s"Call to retrieve Nino from IV failed with status ${response.status} and response body ${response.body}")
        Future.successful(None)
    }
  } andThen {
    logExceptions("Unable to retrieve Nino from IV")
  }

  private def logExceptions[T](msg: String): PartialFunction[Try[T], Unit] = {
    case Failure(t: Throwable) => logger.error(msg, t)
  }
}

@ImplementedBy(classOf[IdentityVerificationConnectorImpl])
trait IdentityVerificationConnector {
  def startRegisterOrganisationAsIndividual(completionURL: String, failureURL: String)
                                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String]

  def retrieveNinoFromIV(journeyId: String)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Nino]]
}
