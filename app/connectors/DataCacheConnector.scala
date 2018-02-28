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
import identifiers.TypedIdentifier
import play.api.libs.json._
import repositories.SessionRepository
import utils.{Cleanup, UserAnswers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataCacheConnectorImpl @Inject()(
                                        val sessionRepository: SessionRepository
                                      ) extends DataCacheConnector {

  override def save[A, I <: TypedIdentifier[A]](cacheId: String, id: I, value: A)(implicit fmt: Format[A], cu: Cleanup[I]): Future[JsValue] = {
    sessionRepository().get(cacheId).flatMap {
      json =>
        UserAnswers(json.getOrElse(Json.obj())).set(id)(value) match {
          case JsSuccess(UserAnswers(updatedJson), _) =>
            cacheUpsert(cacheId, updatedJson)
          case JsError(errors) =>
            Future.failed(JsResultException(errors))
        }
    }
  }

  override def cacheUpsert(cacheId: String, value: JsValue): Future[JsValue] = {
    sessionRepository().upsert(cacheId, value) map (_ => value)
  }

  override def fetch(cacheId: String): Future[Option[JsValue]] =
    sessionRepository().get(cacheId)
}

@ImplementedBy(classOf[DataCacheConnectorImpl])
trait DataCacheConnector {

  def save[A, I <: TypedIdentifier[A]](cacheId: String, id: I, value: A)(implicit fmt: Format[A], cleanup: Cleanup[I]): Future[JsValue]

  def cacheUpsert(cacheId: String, value: JsValue): Future[JsValue]

  def fetch(cacheId: String): Future[Option[JsValue]]
}
