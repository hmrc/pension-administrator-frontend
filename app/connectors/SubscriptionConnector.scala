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

import com.google.inject.Inject
import config.FrontendAppConfig
import models.PsaSubscription.PsaSubscription
import play.api.http.Status.*
import play.api.libs.json.{JsError, JsResultException, JsSuccess, JsValue}
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.Logging
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

class SubscriptionConnector @Inject()(httpV2Client: HttpClientV2, config: FrontendAppConfig)
  extends HttpResponseHelper
    with Logging {

  def getSubscriptionDetailsSelf(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[JsValue] = {

    val subscriptionDetailsSelfUrl = url"${config.subscriptionDetailsSelfUrl}"

    httpV2Client
      .get(subscriptionDetailsSelfUrl)
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK =>
            response.json
          case BAD_REQUEST if response.body.contains("INVALID_PSAID") =>
            throw new PsaIdInvalidSubscriptionException
          case BAD_REQUEST if response.body.contains("INVALID_CORRELATIONID") =>
            throw new CorrelationIdInvalidSubscriptionException
          case NOT_FOUND =>
            throw new PsaIdNotFoundSubscriptionException
          case _ =>
            handleErrorResponse("GET", subscriptionDetailsSelfUrl.toString)(response)
        }
      }
      .andThen {
        case Failure(t: Throwable) =>
          logger.warn("Unable to get PSA subscription details", t)
      }

  }

  def getSubscriptionModel(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PsaSubscription] =
    getSubscriptionDetailsSelf
      .map { response =>
        response.validate[PsaSubscription] match {
          case JsSuccess(value, _) =>
            value
          case JsError(errors) =>
            throw JsResultException(errors)
        }
      }

  def updateSubscriptionDetails(answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    httpV2Client
      .put(url"${config.updateSubscriptionDetailsUrl}")
      .withBody(answers.json)
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK =>
            ()
          case BAD_REQUEST if response.body.contains("INVALID_PAYLOAD") =>
            throw InvalidSubscriptionPayloadException()
          case _ =>
            handleErrorResponse("PUT", config.updateSubscriptionDetailsUrl)(response)
        }
      }
      .andThen {
        case Failure(t: Throwable) =>
          logger.warn("Unable to update PSA subscription details", t)
      }
}
