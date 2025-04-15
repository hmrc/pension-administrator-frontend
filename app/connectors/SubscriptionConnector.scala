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
import models.PsaSubscription.PsaSubscription
import play.api.Logger
import play.api.http.Status.*
import play.api.libs.json.{JsError, JsResultException, JsSuccess, JsValue}
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.{HttpResponseHelper, UserAnswers}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

abstract class SubscriptionException extends Exception

class PsaIdInvalidSubscriptionException extends SubscriptionException

class CorrelationIdInvalidSubscriptionException extends SubscriptionException

class PsaIdNotFoundSubscriptionException extends SubscriptionException

case class InvalidSubscriptionPayloadException() extends SubscriptionException

@ImplementedBy(classOf[SubscriptionConnectorImpl])
trait SubscriptionConnector {

  def getSubscriptionDetailsSelf()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsValue]

  def getSubscriptionModel()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PsaSubscription]

  def updateSubscriptionDetails(answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit]
}

class SubscriptionConnectorImpl @Inject()(httpV2Client: HttpClientV2, config: FrontendAppConfig)
  extends SubscriptionConnector
    with HttpResponseHelper {

  private val logger = Logger(classOf[SubscriptionConnectorImpl])

  override def getSubscriptionDetailsSelf()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsValue] = {

    val subscriptionDetailsSelfUrl = url"${config.subscriptionDetailsSelfUrl}"

    httpV2Client.get(subscriptionDetailsSelfUrl).execute[HttpResponse] map { response =>
      response.status match {
        case OK => response.json
        case BAD_REQUEST if response.body.contains("INVALID_PSAID") => throw new PsaIdInvalidSubscriptionException
        case BAD_REQUEST if response.body.contains("INVALID_CORRELATIONID") => throw new CorrelationIdInvalidSubscriptionException
        case NOT_FOUND => throw new PsaIdNotFoundSubscriptionException
        case _ => handleErrorResponse("GET", subscriptionDetailsSelfUrl.toString)(response)
      }
    } andThen {
      case Failure(t: Throwable) => logger.warn("Unable to get PSA subscription details", t)
    }

  }

  override def getSubscriptionModel()(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PsaSubscription] = {
    getSubscriptionDetailsSelf().map(_.validate[PsaSubscription] match {
      case JsSuccess(value, _) => value
      case JsError(errors) => throw JsResultException(errors)
    })
  }

  def updateSubscriptionDetails(answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    val url = url"${config.updateSubscriptionDetailsUrl}"

    httpV2Client.put(url).withBody(answers.json).execute[HttpResponse] map { response =>
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
