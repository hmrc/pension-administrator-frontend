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

import com.google.inject.Inject
import config.FeatureSwitchManagementService
import identifiers.TypedIdentifier
import play.api.libs.json._
import play.api.mvc.Result
import play.mvc.Http.Status
import uk.gov.hmrc.http._
import utils.Toggles.IsPsaDataShiftEnabled

import scala.concurrent.{ExecutionContext, Future}

class MicroserviceCacheConnector @Inject()(
                                            ps: PensionsSchemeCacheConnector,
                                            pa: PensionAdminCacheConnector,
                                            fs: FeatureSwitchManagementService
                                          ) extends UserAnswersCacheConnector {

  override def save[A, I <: TypedIdentifier[A]](cacheId: String, id: I, value: A)
                                               (implicit
                                                fmt: Format[A],
                                                ec: ExecutionContext,
                                                hc: HeaderCarrier
                                               ): Future[JsValue] = {
    if (fs.get(IsPsaDataShiftEnabled)) {
      isDataExistInScheme(cacheId).flatMap { dataExistInScheme =>
        isDataExistInAdmin(cacheId).flatMap { dataExistInAdmin =>
          (dataExistInAdmin, dataExistInScheme) match {
            case (true, false) =>
              pa.save(cacheId, id, value)
            case (false, true) =>
              ps.save(cacheId, id, value)
            case (false, false) =>
              pa.save(cacheId, id, value)
            case _ =>
              Future.failed(
                new HttpException("Mongo Data cannot exist in both pensions scheme and pension administrator", Status.BAD_REQUEST))
          }
        }
      }
    } else {
      ps.save(cacheId, id, value)
    }
  }

  private def doLogicAndReturnResult[T](cacheId: String, blockForScheme: () => Future[T],
                                        blockForAdmin: () => Future[T])(implicit
                                                                        ec: ExecutionContext,
                                                                        hc: HeaderCarrier
                                       ): Future[T] = {
    if (fs.get(IsPsaDataShiftEnabled)) {
      isDataExistInScheme(cacheId).flatMap { dataExistInScheme =>
        isDataExistInAdmin(cacheId).flatMap { dataExistInAdmin =>
          (dataExistInAdmin, dataExistInScheme) match {
            case (true, false) =>
              blockForAdmin()
            case (false, true) =>
              blockForScheme()
            case _ =>
              Future.failed(
                new HttpException("Issue dealing with mongo collection", Status.BAD_REQUEST))
          }
        }
      }
    } else {
      blockForScheme()
    }
  }

  override def remove[I <: TypedIdentifier[_]](cacheId: String, id: I)
                                              (implicit
                                               ec: ExecutionContext,
                                               hc: HeaderCarrier
                                              ): Future[JsValue] = {
    doLogicAndReturnResult[JsValue](cacheId, () => ps.remove(cacheId, id), () => pa.remove(cacheId, id))
  }

  override def removeAll(cacheId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Result] = {
    doLogicAndReturnResult[Result](cacheId, () => ps.removeAll(cacheId), () => pa.removeAll(cacheId))
  }

  override def fetch(cacheId: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[JsValue]] = {
    doLogicAndReturnResult[Option[JsValue]](cacheId, () => ps.fetch(cacheId), () => pa.fetch(cacheId))
  }

  override def upsert(cacheId: String, value: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] = {
    doLogicAndReturnResult[JsValue](cacheId, () => ps.upsert(cacheId, value), () => pa.upsert(cacheId, value))
  }

  private def isDataExistInScheme(cacheId: String)(implicit
                                                   ec: ExecutionContext,
                                                   hc: HeaderCarrier
  ): Future[Boolean] = {
    ps.fetch(cacheId).map {
      case None =>
        false
      case Some(_) =>
        true
    }
  }

  private def isDataExistInAdmin(cacheId: String)(implicit
                                                  ec: ExecutionContext,
                                                  hc: HeaderCarrier
  ): Future[Boolean] = {
    pa.fetch(cacheId).map {
      case None =>
        false
      case Some(_) =>
        true
    }
  }
}
