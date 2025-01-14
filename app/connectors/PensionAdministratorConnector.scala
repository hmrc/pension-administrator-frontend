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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import models.register.PsaSubscriptionResponse
import play.api.Logger
import play.api.http.Status.OK
import play.api.libs.json._
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.{HttpResponseHelper, UserAnswers}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[PensionAdministratorConnectorImpl])
trait PensionAdministratorConnector {

  def registerPsa(answers: UserAnswers)
                 (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[PsaSubscriptionResponse]

  def updatePsa(answers: UserAnswers)
               (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[HttpResponse]
}

@Singleton
class PensionAdministratorConnectorImpl @Inject()(httpV2Client: HttpClientV2, config: FrontendAppConfig)
  extends PensionAdministratorConnector
    with HttpResponseHelper {

  private val logger = Logger(classOf[PensionAdministratorConnectorImpl])

  def registerPsa(answers: UserAnswers)
                 (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[PsaSubscriptionResponse] = {
    val url = url"${config.registerPsaUrl}"

    httpV2Client.post(url).withBody(answers.json).execute[HttpResponse] map {
      response =>

        response.status match {
          case OK =>
            Json.parse(response.body).validate[PsaSubscriptionResponse] match {
              case JsSuccess(value, _) =>
                value
              case JsError(errors) =>
                throw JsResultException(errors)
            }
          case _ =>
            logger.error("Unable to register PSA")
            handleErrorResponse("POST", url.toString)(response)
        }
    }
  }

  def updatePsa(answers: UserAnswers)
               (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[HttpResponse] = {
    val url = url"${config.updatePsaSelfUrl}"

    httpV2Client.post(url).withBody(answers.json).execute[HttpResponse]
  }
}
