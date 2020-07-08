/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.http.Status
import play.api.libs.json._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class IdentityVerificationConnectorImpl @Inject()(http: HttpClient, appConfig: FrontendAppConfig) extends IdentityVerificationConnector {
  def startRegisterOrganisationAsIndividual(completionURL: String, failureURL: String)
                                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = {

    val jsonData = Json.obj(
      "origin" -> "PODS",
      "completionURL" -> completionURL,
      "failureURL" -> failureURL,
      "confidenceLevel" -> 200
    )

    http.POST(appConfig.ivRegisterOrganisationAsIndividualUrl, jsonData).map { response =>
      require(response.status == Status.CREATED)
      (response.json \ "link").validate[String] match {
        case JsSuccess(value, _) => value
        case JsError(errors) => throw JsResultException(errors)
      }
    } andThen {
      logExceptions("Unable to start registration of organisation as individual via IV")
    }
  }

  override def retrieveNinoFromIV(journeyId: String)
                                 (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Nino]] = {
    val url = s"${appConfig.identityVerification}/identity-verification/journey/$journeyId"

    implicit val rds: HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
      override def read(method: String, url: String, response: HttpResponse): HttpResponse = response
    }

    http.GET(url).flatMap {
      case response if response.status equals Status.OK =>
        Future.successful((response.json \ "nino").asOpt[Nino])
      case response =>
        Logger.debug(s"Call to retrieve Nino from IV failed with status ${response.status} and response body ${response.body}")
        Future.successful(None)
    }
  } andThen {
    logExceptions("Unable to retrieve Nino from IV")
  }

  private def logExceptions[T](msg: String): PartialFunction[Try[T], Unit] = {
    case Failure(t: Throwable) => Logger.error(msg, t)
  }
}

@ImplementedBy(classOf[IdentityVerificationConnectorImpl])
trait IdentityVerificationConnector {
  def startRegisterOrganisationAsIndividual(completionURL: String, failureURL: String)
                                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String]

  def retrieveNinoFromIV(journeyId: String)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Nino]]
}
