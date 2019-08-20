/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.http.{HeaderCarrier, Upstream4xxResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

@ImplementedBy(classOf[PensionsSchemeConnectorImpl])
trait PensionsSchemeConnector {

  def registerPsa(answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PsaSubscriptionResponse]

  def updatePsa(psaId: String, answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit]
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
      handleExceptions()
    }
  }

  private def handleExceptions[A](): PartialFunction[Try[A], Throwable] = {
    case Failure(ex: Upstream4xxResponse) if ex.message.contains("INVALID_BUSINESS_PARTNER") =>
      Logger.warn("Unable to register PSA", ex)
      ex

    case Failure(ex: Throwable) =>
      Logger.error("Unable to register PSA", ex)
      ex
  }

  def updatePsa(psaId: String, answers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    val url = config.updatePsaUrl(psaId)

    http.POST(url, answers.json).map { response =>
      require(response.status == Status.OK)
    } andThen {
      case Failure(ex: Throwable) =>
        Logger.error("Unable to submit PSA Variations", ex)
        ex
    }
  }
}
