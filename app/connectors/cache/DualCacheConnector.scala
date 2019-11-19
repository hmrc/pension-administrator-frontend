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

package connectors.cache

import com.google.inject.Inject
import config.{FeatureSwitchManagementService, FrontendAppConfig}
import identifiers.TypedIdentifier
import play.api.libs.json.{Format, JsValue, Json}
import play.api.mvc.Result
import play.mvc.Http.Status
import uk.gov.hmrc.http.{HeaderCarrier, HttpException}
import utils.Toggles
import play.api.mvc.Results.Ok

import scala.concurrent.{ExecutionContext, Future}

class DualCacheConnector @Inject()(
                                    config: FrontendAppConfig,
                                    oldSchemeCache: PensionAdminCacheConnector,
                                    newSchemeCache: PensionAdminDataCacheConnector,
                                    fs: FeatureSwitchManagementService
                                  ) extends UserAnswersCacheConnector {

  override def save[A, I <: TypedIdentifier[A]](cacheId: String, id: I, value: A)
                                               (implicit
                                                fmt: Format[A],
                                                ec: ExecutionContext,
                                                hc: HeaderCarrier
                                               ): Future[JsValue] =
    if (fs.get(Toggles.isDataShiftEnabled)) {
      doesDataExistInOldCache(cacheId).flatMap { dataInOldCache =>
        doesDataExistInNewCache(cacheId).flatMap { dataInNewCache =>
          (dataInNewCache, dataInOldCache) match {
            case (true, false) =>
              newSchemeCache.save(cacheId, id, value)
            case (false, true) =>
              oldSchemeCache.save(cacheId, id, value)
            case (false, false) =>
              newSchemeCache.save(cacheId, id, value)
            case _ =>
              Future.failed(
                new HttpException("Mongo Data cannot exist in both old and new cache", Status.BAD_REQUEST))
          }
        }
      }
    } else {
      oldSchemeCache.save(cacheId, id, value)
    }

  override def remove[I <: TypedIdentifier[_]](cacheId: String, id: I)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
    doLogicAndReturnResult[JsValue](cacheId, () => oldSchemeCache.remove(cacheId, id), () => newSchemeCache.remove(cacheId, id), Json.obj())

  override def removeAll(cacheId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Result] =
    doLogicAndReturnResult[Result](cacheId, () => oldSchemeCache.removeAll(cacheId), () => newSchemeCache.removeAll(cacheId), Ok)

  override def fetch(cacheId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[JsValue]] =
    doLogicAndReturnResult[Option[JsValue]](cacheId, () => oldSchemeCache.fetch(cacheId), () => newSchemeCache.fetch(cacheId), None)

  override def upsert(cacheId: String, value: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
    doLogicAndReturnResult[JsValue](cacheId, () => oldSchemeCache.upsert(cacheId, value), () => newSchemeCache.upsert(cacheId, value), Json.obj())

  private def doLogicAndReturnResult[T](cacheId: String, blockForOldCache: () => Future[T],
                                        blockForNewCache: () => Future[T], returnDefault: T)(implicit
                                                                                             ec: ExecutionContext,
                                                                                             hc: HeaderCarrier
                                       ): Future[T] =
    if (fs.get(Toggles.isDataShiftEnabled)) {
      doesDataExistInOldCache(cacheId).flatMap { dataInOldCache =>
        doesDataExistInNewCache(cacheId).flatMap { dataInNewCache =>
          (dataInNewCache, dataInOldCache) match {
            case (true, false) =>
              blockForNewCache()
            case (false, true) =>
              blockForOldCache()
            case (false, false) =>
              blockForNewCache()
            case _ =>
              Future.successful(returnDefault)
          }
        }
      }
    } else {
      blockForOldCache()
    }

  private def doesDataExistInOldCache(cacheId: String)(implicit
                                                       ec: ExecutionContext,
                                                       hc: HeaderCarrier
  ): Future[Boolean] =
    oldSchemeCache.fetch(cacheId).map {
      case None =>
        false
      case Some(_) =>
        true
    }

  private def doesDataExistInNewCache(cacheId: String)(implicit
                                                       ec: ExecutionContext,
                                                       hc: HeaderCarrier
  ): Future[Boolean] =
    newSchemeCache.fetch(cacheId).map {
      case None =>
        false
      case Some(_) =>
        true
    }
}