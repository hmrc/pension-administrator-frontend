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
import identifiers.{Identifier, TypedIdentifier}
import play.api.libs.json._
import repositories.SessionRepository
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.{CascadeUpsert, JsLens}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataCacheConnectorImpl @Inject()(
                                        val sessionRepository: SessionRepository,
                                        val cascadeUpsert: CascadeUpsert
                                      ) extends DataCacheConnector {

  // TODO cascade upsert
  override def save[A](cacheId: String, path: JsPath, value: A)(implicit fmt: Format[A]): Future[JsValue] = {
    sessionRepository().get(cacheId).flatMap {
      json =>
        val lens = JsLens.fromPath(path)
        lens.set(Json.toJson(value), json.getOrElse(Json.obj())) match {
          case JsSuccess(updatedJson, _) =>
            sessionRepository().upsert(cacheId, updatedJson)
              .map(_ => updatedJson)
          case JsError(errors) =>
            Future.failed(JsResultException(errors))
        }
    }
  }

  override def fetch(cacheId: String): Future[Option[JsValue]] =
    sessionRepository().get(cacheId)
}

@ImplementedBy(classOf[DataCacheConnectorImpl])
trait DataCacheConnector {

  def save[A](cacheId: String, path: JsPath, value: A)(implicit fmt: Format[A]): Future[JsValue]

  def save[A](cacheId: String, id: TypedIdentifier[A], value: A)(implicit fmt: Format[A]): Future[JsValue] = {
    save[A](cacheId: String, id.path, value)
  }

  def fetch(cacheId: String): Future[Option[JsValue]]
}
