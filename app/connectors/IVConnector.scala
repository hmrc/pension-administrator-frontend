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

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import play.api.Logger
import play.api.http.Status._
import uk.gov.hmrc.http.{HeaderCarrier, HttpException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

@ImplementedBy(classOf[IVConnectorImpl])
trait IVConnector {

  def getNinoFromIV(journeyId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]]
}

class IVConnectorImpl @Inject()(http: HttpClient, appConfig: FrontendAppConfig) extends IVConnector {

  override def getNinoFromIV(journeyId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[String]] = {
    val url = s"${appConfig.identityVerification}/journey/$journeyId"

    http.GET(url).flatMap {
      case response if response.status equals OK =>
        Future.successful((response.json \ "nino").asOpt[String])
      case response =>
        Logger.debug(s"Call to Get Nino from IV failed with status ${response.status} and response body ${response.body}")
        Future.failed(new HttpException(response.body, response.status))
    }
  } andThen {
    case Failure(t: Throwable) =>
      Logger.error("Unable to get the Nino from Manual IV", t)
      Logger.debug(s"Unable to get the Nino from Manual IV for Journey Id: $journeyId")
  }
}
