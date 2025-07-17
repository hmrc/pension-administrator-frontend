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

import com.google.inject.Inject
import config.FrontendAppConfig
import play.api.http.Status._
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results._
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw

import scala.concurrent.{ExecutionContext, Future}

class SessionDataCacheConnector @Inject()(
                                           config: FrontendAppConfig,
                                           httpV2Client: HttpClientV2
                                         ) {
  private lazy val url = url"${config.pensionAdministratorUrl}/pension-administrator/journey-cache/session-data-self"

  def fetch(implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Option[JsValue]] =
    httpV2Client
      .get(url)
      .execute[HttpResponse]
      .recoverWith {
        case _: NotFoundException =>
          Future.successful(HttpResponse(NOT_FOUND, "Not found"))
      }
      .map { response =>
        response.status match {
          case NOT_FOUND =>
            None
          case OK =>
            Some(Json.parse(response.body))
          case _ =>
            Future.failed(new HttpException(response.body, response.status))
            throw new HttpException(response.body, response.status)
        }
      }

  def removeAll()
               (implicit ec: ExecutionContext, headerCarrier: HeaderCarrier): Future[Result] =
    httpV2Client
      .delete(url)
      .execute[HttpResponse]
      .map { _ =>
        Ok
      }

}
