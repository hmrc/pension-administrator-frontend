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

package connectors.cache

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import identifiers.TypedIdentifier
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.*
import play.api.libs.ws.WSBodyWritables.writeableOf_JsValue
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, HttpResponse, NotFoundException, StringContextOps}
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[UserAnswersCacheConnectorImpl])
trait UserAnswersCacheConnector {

  def save[A, I <: TypedIdentifier[A]](id: I, value: A)
                                      (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue]

  def remove[I <: TypedIdentifier[?]](id: I)
                                     (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue]

  def fetch(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[JsValue]]

  def upsert(value: JsValue)
            (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue]

  def removeAll(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Result]
}

class UserAnswersCacheConnectorImpl@Inject()(
                                              config: FrontendAppConfig,
                                              http: HttpClientV2
                                            ) extends UserAnswersCacheConnector {

  protected def url = s"${config.pensionAdministratorUrl}/pension-administrator/journey-cache/psa-data-self"

  override def save[A, I <: TypedIdentifier[A]](id: I, value: A)
                                               (implicit fmt: Format[A], ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
    modify(_.set(id)(value))

  def remove[I <: TypedIdentifier[?]](id: I)
                                     (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
    modify(_.remove(id))

  override def upsert(value: JsValue)
                     (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
    modify(_ => JsSuccess(UserAnswers(value)))

  private[connectors] def modify(modification: UserAnswers => JsResult[UserAnswers])
                                (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
    fetch.flatMap {
      json =>
        modification(UserAnswers(json.getOrElse(Json.obj()))) match {
          case JsSuccess(UserAnswers(updatedJson), _) =>
            updatedJson match {
              case obj: JsObject =>
                http
                  .post(url"$url")
                  .withBody(obj)
                  .execute[HttpResponse]
                  .map {
                    response =>
                      response.status match {
                        case OK => obj
                        case _ => throw new HttpException(response.body, response.status)
                      }
                  }
              case _ =>
                Future.failed(new IllegalArgumentException("Updated JSON is not a JsObject"))
            }

          case JsError(errors) =>
            Future.failed(JsResultException(errors))
        }
    }

  override def fetch(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[JsValue]] =
    http
      .get(url"$url")
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
            throw new HttpException(response.body, response.status)
        }
      }

  override def removeAll(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Result] =
    http
      .delete(url"$url")
      .execute[HttpResponse]
      .map { _ =>
        Ok
      }
}

