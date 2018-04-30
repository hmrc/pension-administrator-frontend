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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import models.register.PsaSubscriptionResponse
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsError, JsResultException, JsSuccess, Json}
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, Upstream4xxResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

@ImplementedBy(classOf[PensionsSchemeConnectorImpl])
trait PensionsSchemeConnector {

  def registerPsa(answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PsaSubscriptionResponse]

}

@Singleton
class PensionsSchemeConnectorImpl @Inject()(http: HttpClient, config: FrontendAppConfig) extends PensionsSchemeConnector {

  def registerPsa(answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PsaSubscriptionResponse] = {
    val url = config.registerPsaUrl

    http.POST(url, answers.json).map { response =>
      require(response.status == Status.OK)

      val json = Json.parse(response.body)

      json.validate[PsaSubscriptionResponse] match {
        case JsSuccess(value, _) => value
        case JsError(errors) => throw JsResultException(errors)
      }
    } andThen {
      logExceptions()
    } recoverWith {
      translateExceptions()
    }

  }

  private def translateExceptions(): PartialFunction[Throwable, Future[PsaSubscriptionResponse]] = {
    case ex: BadRequestException
      if ex.message.contains("INVALID_PAYLOAD")
        => Future.failed(InvalidPayloadException())
    case ex: BadRequestException
      if ex.message.contains("INVALID_CORRELATION_ID")
        => Future.failed(InvalidCorrelationIdException())
    case ex @ Upstream4xxResponse(_, Status.FORBIDDEN, _, _)
      if ex.message.contains("INVALID_BUSINESS_PARTNER")
        => Future.failed(InvalidBusinessPartnerException())
    case ex @ Upstream4xxResponse(_, Status.CONFLICT, _, _)
      if ex.message.contains("DUPLICATE_SUBMISSION")
        => Future.failed(DuplicateSubmissionException())
  }

  private def logExceptions(): PartialFunction[Try[PsaSubscriptionResponse], Unit] = {
    case Failure(t: Throwable) => Logger.error("Unable to register PSA", t)
  }

}

sealed trait RegisterPsaException extends Exception

case class InvalidPayloadException() extends RegisterPsaException
case class InvalidCorrelationIdException() extends RegisterPsaException
case class InvalidBusinessPartnerException() extends RegisterPsaException
case class DuplicateSubmissionException() extends RegisterPsaException
