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

import base.SpecBase
import identifiers.TypedIdentifier
import org.scalatest._
import play.api.libs.json.{Format, JsValue, Json}
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class MicroserviceCacheConnectorSpec extends AsyncWordSpec with MustMatchers with OptionValues with RecoverMethods {

  import MicroserviceCacheConnectorSpec._

  private object FakeIdentifier extends TypedIdentifier[String] {
    override def toString: String = "fake-identifier"
  }

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  ".save is called" must {
    "save the data to admin if data already exist in admin" in {
      val connector = new MicroserviceCacheConnector(
        fakePensionAdminCacheConnector(true)
      )
      connector.save("foo", FakeIdentifier, "") map {
        result =>
          result mustBe Json.obj("data" -> "admin saved")
      }
    }
  }
  ".fetch  is called" must {

    "return data from admin collection when the admin collection has data" in {
      val connector = new MicroserviceCacheConnector(
        fakePensionAdminCacheConnector(true)
      )
      connector.fetch("foo") map {
        result =>
          result.value mustBe Json.obj("data" -> "admin")
      }
    }
  }
  ".remove is called" must {
    "removes data from admin collection when the admin collection has data" in {
      val connector = new MicroserviceCacheConnector(
        fakePensionAdminCacheConnector(true)
      )
      connector.remove("foo", FakeIdentifier) map {
        result =>
          result mustBe Json.obj("data" -> "admin removed")
      }
    }
  }
  ".removeAll is called" must {
    "removes all the data from admin collection when the admin collection has data" in {
      val connector = new MicroserviceCacheConnector(
        fakePensionAdminCacheConnector(true)
      )
      val result = connector.removeAll("foo")
      status(result) mustBe OK
      contentAsString(result) mustBe "admin remove all"
    }
  }
  ".upsert is called" must {
    "upsert the data from admin collection when the admin collection has data" in {
      val connector = new MicroserviceCacheConnector(
        fakePensionAdminCacheConnector(true)
      )
      connector.upsert("foo", Json.obj()) map {
        result =>
          result mustBe Json.obj("data" -> "admin upsert")
      }
    }
  }
}

object MicroserviceCacheConnectorSpec extends SpecBase {
  private val fakeWsClient = new WSClient {
    override def underlying[T]: T = ???

    override def url(url: String): WSRequest = ???

    override def close(): Unit = ???
  }

  private def fetchResponse(isDataExist: Boolean, data: String) = if (isDataExist) {
    Future.successful(Some(Json.obj("data" -> data)))
  } else {
    Future.successful(None)
  }

  private def fakePensionSchemeCacheConnector(isDataExist: Boolean) = new PensionsSchemeCacheConnector(frontendAppConfig, fakeWsClient) {
    override def fetch(id: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[JsValue]] = fetchResponse(isDataExist, "scheme")

    override def save[A, I <: TypedIdentifier[A]](cacheId: String, id: I, value: A)(implicit fmt: Format[A],
                                                                                    ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
      Future.successful(Json.obj("data" -> "scheme saved"))

    override def remove[I <: TypedIdentifier[_]](cacheId: String, id: I)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
      Future.successful(Json.obj("data" -> "scheme removed"))

    override def removeAll(id: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Result] = Future.successful(Ok("scheme remove all"))

    override def upsert(cacheId: String, value: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
      Future.successful(Json.obj("data" -> "scheme upsert"))
  }

  private def fakePensionAdminCacheConnector(isDataExist: Boolean) = new PensionAdminCacheConnector(frontendAppConfig, fakeWsClient) {
    override def fetch(id: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Option[JsValue]] = fetchResponse(isDataExist, "admin")

    override def save[A, I <: TypedIdentifier[A]](cacheId: String, id: I, value: A)(implicit fmt: Format[A],
                                                                                    ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
      Future.successful(Json.obj("data" -> "admin saved"))

    override def remove[I <: TypedIdentifier[_]](cacheId: String, id: I)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
      Future.successful(Json.obj("data" -> "admin removed"))

    override def removeAll(id: String)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[Result] = Future.successful(Ok("admin remove all"))

    override def upsert(cacheId: String, value: JsValue)(implicit ec: ExecutionContext, hc: HeaderCarrier): Future[JsValue] =
      Future.successful(Json.obj("data" -> "admin upsert"))
  }
}
