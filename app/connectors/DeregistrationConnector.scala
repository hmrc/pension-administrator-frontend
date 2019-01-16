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

import com.google.inject.{ImplementedBy, Inject}
import config.{FeatureSwitchManagementService, FrontendAppConfig}
import javax.inject.Singleton
import play.api.Logger
import play.api.http.Status._
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.RetryHelper

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}

@ImplementedBy(classOf[DeregistrationConnectorImpl])
trait DeregistrationConnector {

  def stopBeingPSA(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse]
}

@Singleton
class DeregistrationConnectorImpl @Inject()(http: HttpClient,
                                          config: FrontendAppConfig,
                                          fs: FeatureSwitchManagementService
                                         ) extends DeregistrationConnector with RetryHelper {


  override def stopBeingPSA(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =

    retryOnFailure(() => deregisterRequest(psaId), config) andThen logDeregistrationExceptions


  def deregisterRequest(psaId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val deregisterUrl = config.deregisterPsaUrl.format(psaId)
    http.DELETE(deregisterUrl) flatMap {
      case response if response.status equals NO_CONTENT =>
        Future.successful(response)
      case response =>
        Future.failed(new HttpException(response.body, response.status))
    }
  }

  private def logDeregistrationExceptions: PartialFunction[Try[HttpResponse], Unit] = {
    case Failure(t: Throwable) =>
      Logger.error("Unable to connect to deregister the PSA", t)
  }

}
