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

import audit.DeregisterEvent
import audit.{AuditService, PSAEnrolmentEvent}
import com.google.inject.{ImplementedBy, Singleton}
import config.FrontendAppConfig
import javax.inject.Inject
import models.register.KnownFacts
import models.requests.DataRequest
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{Writes, Json}
import play.api.mvc.AnyContent
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.RetryHelper

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Failure, Try}

@ImplementedBy(classOf[TaxEnrolmentsConnectorImpl])
trait TaxEnrolmentsConnector {
  def deEnrol(groupId: String, psaId:String, userId: String)
             (implicit hc: HeaderCarrier, ec: ExecutionContext, rh:RequestHeader): Future[HttpResponse]
  def enrol(enrolmentKey: String, knownFacts: KnownFacts)
           (implicit w: Writes[KnownFacts], hc: HeaderCarrier, executionContext: ExecutionContext, request: DataRequest[AnyContent]): Future[HttpResponse]

}

@Singleton
class TaxEnrolmentsConnectorImpl @Inject()(val http: HttpClient,
                                           config: FrontendAppConfig,
                                           auditService: AuditService) extends TaxEnrolmentsConnector with RetryHelper{

  def url: String = config.taxEnrolmentsUrl("HMRC-PODS-ORG")

  override def enrol(enrolmentKey: String, knownFacts: KnownFacts)
                    (implicit w: Writes[KnownFacts], hc: HeaderCarrier, executionContext: ExecutionContext, request: DataRequest[AnyContent]): Future[HttpResponse] = {
    retryOnFailure(() => enrolmentRequest(enrolmentKey, knownFacts), config)
  } andThen {
    logExceptions(knownFacts)
  }

  private def enrolmentRequest(enrolmentKey: String, knownFacts: KnownFacts)
                              (implicit w: Writes[KnownFacts], hc: HeaderCarrier, executionContext: ExecutionContext,
                               request: DataRequest[AnyContent]):Future[HttpResponse] = {

    http.PUT(url, knownFacts) flatMap {
      case response if response.status equals NO_CONTENT =>
        auditService.sendEvent(PSAEnrolmentEvent(request.externalId, enrolmentKey))
        Future.successful(response)
      case response =>
        Future.failed(new HttpException(response.body, response.status))
    }
  }


  private def logExceptions(knownFacts: KnownFacts): PartialFunction[Try[HttpResponse], Unit] = {
    case Failure(t: Throwable) =>
      Logger.error("Unable to connect to Tax Enrolments", t)
      Logger.debug(s"Known Facts: ${Json.toJson(knownFacts)}")
  }


    def deEnrol(groupId: String, psaId:String, userId: String)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext, rh:RequestHeader): Future[HttpResponse] = {
    retryOnFailure(() => deEnrolmentRequest(groupId, psaId, userId), config)
  } andThen {
    logDeEnrolmentExceptions
  }

  private def deEnrolmentRequest(groupId: String, psaId: String, userId: String)
             (implicit hc: HeaderCarrier, ec: ExecutionContext, rh:RequestHeader): Future[HttpResponse] = {

    val enrolmentKey = s"HMRC-PODS-ORG~PSAID~$psaId"
    val deEnrolmentUrl = config.taxDeEnrolmentUrl.format(groupId, enrolmentKey)
    http.DELETE(deEnrolmentUrl) flatMap {
      case response if response.status equals NO_CONTENT =>
        auditService.sendEvent(DeregisterEvent(userId, psaId))
        Future.successful(response)
      case response =>
        Future.failed(new HttpException(response.body, response.status))
    }
  }

  private def logDeEnrolmentExceptions: PartialFunction[Try[HttpResponse], Unit] = {
    case Failure(t: Throwable) =>
      Logger.error("Unable to connect to Tax Enrolments to de enrol the PSA", t)
  }


}
