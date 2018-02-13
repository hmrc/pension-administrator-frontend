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

import identifiers.TypedIdentifier
import play.api.libs.json._
import utils.Cleanup

import scala.concurrent.Future

class FakeDataCacheConnector extends DataCacheConnector {

  override def save[A, I <: TypedIdentifier[A]](cacheId: String, id: I, value: A)(implicit fmt: Format[A], cleanup: Cleanup[I]): Future[JsValue] =
    Future.successful(Json.obj())

  override def fetch(cacheId: String): Future[Option[JsValue]] =
    Future.successful(Some(Json.obj()))
}

object FakeDataCacheConnector extends FakeDataCacheConnector
