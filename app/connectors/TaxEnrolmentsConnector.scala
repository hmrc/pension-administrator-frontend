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

import audit.{AuditService, DeregisterEvent, PSAEnrolmentEvent}
import com.google.inject.{ImplementedBy, Singleton}
import config.FrontendAppConfig
import models.register.KnownFacts
import models.requests.DataRequest
import play.api.Logging
import play.api.http.Status.*
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{AnyContent, RequestHeader}
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import utils.{HttpResponseHelper, RetryHelper}

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

@ImplementedBy(classOf[TaxEnrolmentsConnectorImpl])
trait TaxEnrolmentsConnector {
  def deEnrol(groupId: String, psaId: String, userId: String)
             (implicit hc: HeaderCarrier, ec: ExecutionContext, rh: RequestHeader): Future[HttpResponse]

  def enrol(enrolmentKey: String, knownFacts: KnownFacts)
           (implicit w: Writes[KnownFacts], hc: HeaderCarrier, ec: ExecutionContext, request: DataRequest[AnyContent]): Future[HttpResponse]

}

@Singleton
class TaxEnrolmentsConnectorImpl @Inject()(
                                            httpV2Client: HttpClientV2,
                                            config: FrontendAppConfig,
                                            auditService: AuditService
                                          )
  extends TaxEnrolmentsConnector
    with RetryHelper
    with HttpResponseHelper
    with Logging {

  def url: URL = url"${config.taxEnrolmentsUrl("HMRC-PODS-ORG")}"

  override def enrol(enrolmentKey: String, knownFacts: KnownFacts)
                    (implicit w: Writes[KnownFacts], hc: HeaderCarrier, ec: ExecutionContext, request: DataRequest[AnyContent]): Future[HttpResponse] =
    retryOnFailure(() => enrolmentRequest(enrolmentKey, knownFacts)(hc, ec, request), config).andThen {
      case Failure(t: Throwable) =>
        logger.error("Unable to connect to Tax Enrolments", t)
        logger.debug(s"Known Facts: ${Json.toJson(knownFacts)}")
    }

  private def enrolmentRequest(enrolmentKey: String, knownFacts: KnownFacts)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext, request: DataRequest[AnyContent]): Future[HttpResponse] =
    httpV2Client
      .put(url)
      .withBody(knownFacts)
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case NO_CONTENT =>
            auditService.sendEvent(PSAEnrolmentEvent(request.externalId, enrolmentKey))
            Future.successful(response)
          case _ =>
            if (response.body.contains("INVALID_JSON")) logger.warn(s"INVALID_JSON returned from call to $url")
            handleErrorResponse("PUT", url.toString)(response)
        }
      }


  def deEnrol(groupId: String, psaId: String, userId: String)
             (implicit hc: HeaderCarrier, ec: ExecutionContext, rh: RequestHeader): Future[HttpResponse] =
    retryOnFailure(() => deEnrolmentRequest(groupId, psaId, userId), config).andThen {
      case Failure(t: Throwable) =>
        logger.error("Unable to connect to Tax Enrolments to de enrol the PSA", t)
    }

  private def deEnrolmentRequest(groupId: String, psaId: String, userId: String)
                                (implicit hc: HeaderCarrier, ec: ExecutionContext, rh: RequestHeader): Future[HttpResponse] = {
    
    val url: String = config.taxDeEnrolmentUrl.format(groupId, s"HMRC-PODS-ORG~PSAID~$psaId")

    logger.debug(s"Calling de-enrol URL:$url")

    httpV2Client
      .delete(url"$url")
      .execute[HttpResponse]
      .flatMap {
        case response if response.status.equals(NO_CONTENT) =>
          auditService.sendEvent(DeregisterEvent(userId, psaId))
          Future.successful(response)
        case response =>
          Future.failed(new HttpException(response.body, response.status))
      }
  }

}
