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

import com.google.inject.Inject
import config.FrontendAppConfig
import identifiers.TypedIdentifier
import play.api.http.Status._
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw

class ICacheConnector @Inject()(
                                 config: FrontendAppConfig,
                                 http: HttpClientV2
                               ) extends UserAnswersCacheConnector {

  protected def url(id: String) = s"${config.pensionAdministratorUrl}/pension-administrator/journey-cache/psa/$id"

  override def save[A, I <: TypedIdentifier[A]](cacheId: String, id: I, value: A)
                                               (implicit
                                                fmt: Format[A],
                                                executionContext: ExecutionContext,
                                                hc: HeaderCarrier
                                               ): Future[JsValue] = {
    modify(cacheId, _.set(id)(value))
  }

  def remove[I <: TypedIdentifier[?]](cacheId: String, id: I
                                     )(implicit executionContext: ExecutionContext, hc: HeaderCarrier): Future[JsValue] = {
    modify(cacheId, _.remove(id))
  }

  override def upsert(cacheId: String, value: JsValue
                     )(implicit executionContext: ExecutionContext, hc: HeaderCarrier): Future[JsValue] = {
    modify(cacheId, _ => JsSuccess(UserAnswers(value)))
  }

  private[connectors] def modify(cacheId: String,
                                 modification: UserAnswers => JsResult[UserAnswers]
                                )(implicit executionContext: ExecutionContext, hc: HeaderCarrier): Future[JsValue] = {
    fetch(cacheId).flatMap {
      json =>
        modification(UserAnswers(json.getOrElse(Json.obj()))) match {
          case JsSuccess(UserAnswers(updatedJson), _) =>
            updatedJson match {
              case obj: JsObject =>
                http.post(url"${url(cacheId)}").withBody(obj).execute[HttpResponse] map {
                  response =>
                    response.status match {
                      case OK => obj
                      case  _ => throw new HttpException(response.body, response.status)
                    }
                }
              case _ =>
                Future.failed(new IllegalArgumentException("Updated JSON is not a JsObject"))
            }

          case JsError(errors) =>
            Future.failed(JsResultException(errors))
        }
    }
  }

  override def fetch(id: String
                    )(implicit executionContext: ExecutionContext, hc: HeaderCarrier): Future[Option[JsValue]] = {
    http.get(url"${url(id)}").execute[HttpResponse]
      .recoverWith(mapExceptionsToStatus) map{ response =>
          response.status match {
            case NOT_FOUND =>
              None
            case OK =>
              Some(Json.parse(response.body))
            case _ =>
              throw new HttpException(response.body, response.status)
          }
      }
  }

  override def removeAll(id: String)(implicit executionContext: ExecutionContext, hc: HeaderCarrier): Future[Result] = {
    http.delete(url"${url(id)}").execute[HttpResponse] map { _ =>
      Ok
    }
  }

  private def mapExceptionsToStatus: PartialFunction[Throwable, Future[HttpResponse]] = {
    case _: NotFoundException =>
      Future.successful(HttpResponse(NOT_FOUND, "Not found"))
  }

}
