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
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{JsError, JsResultException, JsSuccess}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

@ImplementedBy(classOf[DeRegistrationConnectorImpl])
trait DeRegistrationConnector {
  def canDeRegister(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean]
}

@Singleton
class DeRegistrationConnectorImpl @Inject()(http: HttpClient, config: FrontendAppConfig) extends DeRegistrationConnector {

  override def canDeRegister(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {

    val url = config.canDeRegisterPsaUrl(psaId)

    http.GET(url).map { response =>
      require(response.status == Status.OK)
      response.json.validate[Boolean] match {
        case JsSuccess(value, _) => value
        case JsError(errors) => throw JsResultException(errors)
      }
    } andThen {
      case Failure(t: Throwable) => Logger.warn("Unable to get the response from can de register api", t)
    }
  }
}
