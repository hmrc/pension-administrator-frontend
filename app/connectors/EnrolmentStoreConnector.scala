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

import config.FrontendAppConfig
import models.register.KnownFacts
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.Writes
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

class EnrolmentStoreConnector @Inject()(val http: HttpClient, config: FrontendAppConfig) {

  val url = config.enrolmentStoreUrl("HMRC-PSA-ORG")

  def enrol(knownFacts: KnownFacts)
           (implicit w: Writes[KnownFacts], hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    http.PUT(url, knownFacts) flatMap {
      case response if response.status equals NO_CONTENT => Future.successful(response)
      case response => Future.failed(new HttpException(response.body, response.status))
    } andThen {
      logExceptions()
    }

  private def logExceptions(): PartialFunction[Try[HttpResponse], Unit] = {
    case Failure(t: Throwable) => Logger.error("Unable to connect to Tax Enrolments", t)
  }

  def constructKnownFacts: KnownFacts = ???

}
