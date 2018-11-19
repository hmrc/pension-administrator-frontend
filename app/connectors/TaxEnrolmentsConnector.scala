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

import com.google.inject.{ImplementedBy, Singleton}
import config.FrontendAppConfig
import javax.inject.Inject
import models.register.KnownFacts
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{Json, Writes}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.{RetryHelper, RetryHelperImpl}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

@ImplementedBy(classOf[TaxEnrolmentsConnectorImpl])
trait TaxEnrolmentsConnector {

  def enrol(enrolmentKey: String, knownFacts: KnownFacts)
           (implicit w: Writes[KnownFacts], hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse]

}

@Singleton
class TaxEnrolmentsConnectorImpl @Inject()(val http: HttpClient, config: FrontendAppConfig) extends TaxEnrolmentsConnector with RetryHelper{

  def url: String = config.taxEnrolmentsUrl("HMRC-PODS-ORG")

  override def enrol(enrolmentKey: String, knownFacts: KnownFacts)
                    (implicit w: Writes[KnownFacts], hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    retryOnFailure(() => enrolmentRequest(enrolmentKey, knownFacts))
  }

  private def enrolmentRequest(enrolmentKey: String, knownFacts: KnownFacts)
           (implicit w: Writes[KnownFacts], hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    http.PUT(url, knownFacts) flatMap {
      case response if response.status equals NO_CONTENT => Future.successful(response)
      case response => Future.failed(new HttpException(response.body, response.status))
    } andThen {
      logExceptions(knownFacts)
    }
  }

  private def logExceptions(knownFacts: KnownFacts): PartialFunction[Try[HttpResponse], Unit] = {
    case Failure(t: Throwable) =>
      Logger.error("Unable to connect to Tax Enrolments", t)
      Logger.debug(s"Known Facts: ${Json.toJson(knownFacts)}")
  }

}
