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
import models.PsaSubscription.PsaSubscription
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsError, JsResultException, JsSuccess, JsValue}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpClient
import utils.{HttpResponseHelper, UserAnswers}
import uk.gov.hmrc.http.HttpReads.Implicits._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

abstract class SubscriptionException extends Exception

class PsaIdInvalidSubscriptionException extends SubscriptionException

class CorrelationIdInvalidSubscriptionException extends SubscriptionException

class PsaIdNotFoundSubscriptionException extends SubscriptionException

case class InvalidSubscriptionPayloadException() extends SubscriptionException

@ImplementedBy(classOf[SubscriptionConnectorImpl])
trait SubscriptionConnector {

  def getSubscriptionDetails(psaId: String)
                            (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsValue]

  def getSubscriptionModel(psaId: String)
                          (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PsaSubscription]

  def updateSubscriptionDetails(answers: UserAnswers)
                               (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit]
}

class SubscriptionConnectorImpl @Inject()(http: HttpClient, config: FrontendAppConfig)
  extends SubscriptionConnector
    with HttpResponseHelper {

  private val logger = Logger(classOf[SubscriptionConnectorImpl])

  override def getSubscriptionDetails(psaId: String)
                                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsValue] = {

    val psaIdHC = hc.withExtraHeaders("psaId" -> psaId)

    val url = config.subscriptionDetailsUrl

    http.GET[HttpResponse](url)(implicitly, psaIdHC, implicitly) map { response =>

      response.status match {
        case OK => response.json
        case BAD_REQUEST if response.body.contains("INVALID_PSAID") => throw new PsaIdInvalidSubscriptionException
        case BAD_REQUEST if response.body.contains("INVALID_CORRELATIONID") => throw new CorrelationIdInvalidSubscriptionException
        case NOT_FOUND => throw new PsaIdNotFoundSubscriptionException
        case _ => handleErrorResponse("GET", config.subscriptionDetailsUrl)(response)
      }
    } andThen {
      case Failure(t: Throwable) => logger.warn("Unable to get PSA subscription details", t)
    }

  }

  override def getSubscriptionModel(psaId: String)
                                   (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PsaSubscription] = {
    getSubscriptionDetails(psaId).map(_.validate[PsaSubscription] match {
      case JsSuccess(value, _) => value
      case JsError(errors) => throw JsResultException(errors)
    })
  }

  def updateSubscriptionDetails(answers: UserAnswers)
                               (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    val url = config.updateSubscriptionDetailsUrl

    http.PUT[JsValue, HttpResponse](url, answers.json) map { response =>
      response.status match {
        case OK => ()
        case BAD_REQUEST if response.body.contains("INVALID_PAYLOAD") => throw InvalidSubscriptionPayloadException()
        case _ => handleErrorResponse("PUT", config.updateSubscriptionDetailsUrl)(response)
      }
    } andThen {
      case Failure(t: Throwable) => logger.warn("Unable to update PSA subscription details", t)
    }
  }
}
