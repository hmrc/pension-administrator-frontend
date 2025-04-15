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
import models.SendEmailRequest
import models.enumeration.JourneyType
import play.api.Logger
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.domain.PsaId
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import scala.concurrent.{ExecutionContext, Future}

sealed trait EmailStatus

case object EmailSent extends EmailStatus

case object EmailNotSent extends EmailStatus

@ImplementedBy(classOf[EmailConnectorImpl])
trait EmailConnector {

  def sendEmail(emailAddress: String, templateName: String, templateParams: Map[String, String], psaId: PsaId,
                journeyType : JourneyType.Name)
               (implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[EmailStatus]
}

class EmailConnectorImpl @Inject()(
                                    appConfig: FrontendAppConfig,
                                    httpV2Client: HttpClientV2,
                                    crypto: ApplicationCrypto
                                  ) extends EmailConnector {

  private val logger = Logger(classOf[EmailConnectorImpl])

  private def callBackUrl(psaId: PsaId, journeyType: JourneyType.Name): String = {
    val encryptedPsaId = URLEncoder.encode(crypto.QueryParameterCrypto.encrypt(PlainText(psaId.value)).value, StandardCharsets.UTF_8.toString)
    appConfig.psaEmailCallback(encryptedPsaId, journeyType.toString)
  }

  override def sendEmail(
                          emailAddress: String,
                          templateName: String,
                          templateParams: Map[String, String],
                          psaId: PsaId,
                          journeyType: JourneyType.Name
                        )(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[EmailStatus] = {
    val emailServiceUrl = url"${appConfig.emailUrl}"

    val sendEmailReq = SendEmailRequest(List(emailAddress), templateName, templateParams, appConfig.emailSendForce, callBackUrl(psaId, journeyType))

    val jsonData = Json.toJson(sendEmailReq)

    (httpV2Client.post(emailServiceUrl).withBody(jsonData).execute[HttpResponse] map { response =>
      response.status match {
        case ACCEPTED =>
          EmailSent
        case status =>
          logger.warn(s"Sending Email failed with response status $status")
          EmailNotSent
      }
    }).recoverWith(logExceptions)
  }

  private def logExceptions: PartialFunction[Throwable, Future[EmailStatus]] = {
    case t: Throwable =>
      logger.warn("Unable to connect to Email Service", t)
      Future.successful(EmailNotSent)
  }
}

