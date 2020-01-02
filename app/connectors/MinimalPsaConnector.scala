/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.HttpResponseHelper

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

@ImplementedBy(classOf[MinimalPsaConnectorImpl])
trait MinimalPsaConnector {

  def isPsaSuspended(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean]
}

class MinimalPsaConnectorImpl @Inject()(http: HttpClient, config: FrontendAppConfig) extends MinimalPsaConnector with HttpResponseHelper {

  override def isPsaSuspended(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    val psaHc = hc.withExtraHeaders("psaId" -> psaId)

    http.GET[HttpResponse](config.minimalPsaDetailsUrl)(implicitly, psaHc, implicitly) map { response =>
      response.status match {
        case OK =>
          (response.json \ "isPsaSuspended").as[Boolean]
        case _ => handleErrorResponse("GET", config.minimalPsaDetailsUrl)(response)
      }
    } andThen {
      case Failure(t: Throwable) => Logger.warn("Unable to get PSA minimal details", t)
    }
  }

}
