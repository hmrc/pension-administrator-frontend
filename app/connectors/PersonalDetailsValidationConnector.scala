/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.http.Status._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.HttpClient
import utils.HttpResponseHelper

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class PersonalDetailsValidationConnectorImpl @Inject()(http: HttpClient, frontendAppConfig: FrontendAppConfig)
  extends PersonalDetailsValidationConnector
    with HttpResponseHelper {

  private val logger = Logger(classOf[PersonalDetailsValidationConnectorImpl])

  override def retrieveNino(validationId: String)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Nino]] = {
    val url = s"${frontendAppConfig.personalDetailsValidation}/personal-details-validation/$validationId"

    http.GET[HttpResponse](url).map {
      case response if response.status equals OK =>
        (response.json \ "personalDetails" \ "nino").asOpt[Nino]
      case response =>
        logger.debug(s"Call to retrieve Nino failed with status ${response.status} and response body ${response.body}")
        None
    }
  } andThen {
    logExceptions("Unable to retrieve Nino")
  }

  private def logExceptions[T](msg: String): PartialFunction[Try[T], Unit] = {
    case Failure(t: Throwable) => logger.error(msg, t)
  }
}

@ImplementedBy(classOf[PersonalDetailsValidationConnectorImpl])
trait PersonalDetailsValidationConnector {
  def retrieveNino(validationId: String)
                  (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[Nino]]
}
