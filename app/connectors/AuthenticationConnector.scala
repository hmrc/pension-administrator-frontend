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

import javax.inject.Inject

import com.google.inject.{ImplementedBy, Singleton}
import config.FrontendAppConfig
import play.api.Logger
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import play.api.http.Status._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

@ImplementedBy(classOf[AuthenticationConnectorImpl])
trait AuthenticationConnector {
  def refreshProfile(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse]
}

@Singleton
class AuthenticationConnectorImpl @Inject()(val http: HttpClient, config: FrontendAppConfig) extends AuthenticationConnector {

  val url: String = config.authenticationUrl

  def refreshProfile(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    if(config.refreshProfileSwitch) {
      http.POSTEmpty(url) map { response =>
        Logger.info("[AuthenticationConnector] Current user profile was refreshed")
        response
      } andThen {
        logExceptions
      }
    } else {
      Future.successful(HttpResponse(NO_CONTENT))
    }

  private def logExceptions: PartialFunction[Try[HttpResponse], Unit] = {
    case Failure(t: Throwable) => Logger.error(s"Unable to connect to Refresh Auth Profile: ${t.getLocalizedMessage}", t)
  }
}