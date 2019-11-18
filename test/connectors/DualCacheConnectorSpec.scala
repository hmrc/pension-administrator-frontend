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

import com.github.tomakehurst.wiremock.client.WireMock._
import config.FeatureSwitchManagementService
import connectors.cache.{DualCacheConnector, ICacheConnector}
import identifiers.TypedIdentifier
import org.scalatest._
import play.api.http.Status
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.Json
import play.api.mvc.Results._
import uk.gov.hmrc.http.{HeaderCarrier, HttpException}
import utils.{FakeFeatureSwitchManagementService, WireMockHelper}

import scala.concurrent.ExecutionContext.Implicits.global

class DualCacheConnectorSpec extends AsyncWordSpec with MustMatchers with WireMockHelper with OptionValues with RecoverMethods {

  private object FakeIdentifier extends TypedIdentifier[String] {
    override def toString: String = "fake-identifier"
  }

  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def newUrl(id: String): String = s"/pension-administrator/journey-cache/psa-data/$id"

  private def oldUrl(id: String): String = s"/pension-administrator/journey-cache/psa/$id"

  override def bindings: Seq[GuiceableModule] =
    Seq(bind[FeatureSwitchManagementService].toInstance(new FakeFeatureSwitchManagementService(enableToggle = true)))

  private lazy val connector = injector.instanceOf[DualCacheConnector]

  ".fetch" must {

    "return `None` when the server returns a 404 for both the collections" in {
      server.stubFor(
        get(urlEqualTo(oldUrl("testId")))
          .willReturn(
            notFound
          )
      )

      server.stubFor(
        get(urlEqualTo(newUrl("testId")))
          .willReturn(
            notFound
          )
      )

      connector.fetch("testId") map {
        result =>
          result mustNot be(defined)
      }
    }

    "return data from old collection if data is present in old collection" in {
      server.stubFor(
        get(urlEqualTo(oldUrl("testId")))
          .willReturn(
            ok(Json.obj("testId" -> "data").toString())
          )
      )
      server.stubFor(
        get(urlEqualTo(newUrl("testId")))
          .willReturn(
            notFound
          )
      )

      connector.fetch("testId") map {
        result =>
          result.value mustEqual Json.obj("testId" -> "data")
      }
    }

    "return data from new collection if data is present in new collection" in {
      server.stubFor(
        get(urlEqualTo(oldUrl("testId")))
          .willReturn(
            notFound
          )
      )
      server.stubFor(
        get(urlEqualTo(newUrl("testId")))
          .willReturn(
            ok(Json.obj("testId" -> "new data").toString())
          )
      )

      connector.fetch("testId") map {
        result =>
          result.value mustEqual Json.obj("testId" -> "new data")
      }
    }

    "return a failed future on upstream error" in {

      server.stubFor(
        get(urlEqualTo(newUrl("testId")))
          .willReturn(
            serverError
          )
      )

      server.stubFor(
        get(urlEqualTo(oldUrl("testId")))
          .willReturn(
            serverError
          )
      )

      recoverToExceptionIf[HttpException] {
        connector.fetch("testId")
      } map {
        _.responseCode mustEqual Status.INTERNAL_SERVER_ERROR
      }

    }
  }

  ".save" must {

    "save the data in new cache when no data exists in both the cache" in {

      val json = Json.obj(
        "fake-identifier" -> "foobar"
      )

      val value = Json.stringify(json)

      server.stubFor(
        get(urlEqualTo(newUrl("testId")))
          .willReturn(
            notFound
          )
      )

      server.stubFor(
        get(urlEqualTo(oldUrl("testId")))
          .willReturn(
            notFound
          )
      )

      server.stubFor(
        post(urlEqualTo(newUrl("testId")))
          .withRequestBody(equalTo(value))
          .willReturn(
            ok
          )
      )

      connector.save("testId", FakeIdentifier, "foobar") map {
        _ mustEqual json
      }
    }

    "save the data in old cache when data exists in old cache" in {

      val json = Json.obj(
        "testId" -> "bar"
      )

      val updatedJson = Json.obj(
        "testId" -> "bar",
        "fake-identifier" -> "foobar"
      )

      val value = Json.stringify(json)
      val updatedValue = Json.stringify(updatedJson)

      server.stubFor(
        get(urlEqualTo(newUrl("testId")))
          .willReturn(
            notFound
          )
      )

      server.stubFor(
        get(urlEqualTo(oldUrl("testId")))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(
        post(urlEqualTo(oldUrl("testId")))
          .withRequestBody(equalTo(updatedValue))
          .willReturn(
            ok
          )
      )

      connector.save("testId", FakeIdentifier, "foobar") map {
        _ mustEqual updatedJson
      }
    }

    "save the data in new cache when data exists in new cache" in {

      val json = Json.obj(
        "testId" -> "bar"
      )

      val updatedJson = Json.obj(
        "testId" -> "bar",
        "fake-identifier" -> "foobar"
      )

      val value = Json.stringify(json)
      val updatedValue = Json.stringify(updatedJson)

      server.stubFor(
        get(urlEqualTo(oldUrl("testId")))
          .willReturn(
            notFound
          )
      )

      server.stubFor(
        get(urlEqualTo(newUrl("testId")))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(
        post(urlEqualTo(newUrl("testId")))
          .withRequestBody(equalTo(updatedValue))
          .willReturn(
            ok
          )
      )

      connector.save("testId", FakeIdentifier, "foobar") map {
        _ mustEqual updatedJson
      }
    }

    "return a Bad Request if data exist in both the cache" in {

      val json = Json.obj(
        "testId" -> "bar"
      )
      val value = Json.stringify(json)

      server.stubFor(
        get(urlEqualTo(newUrl("testId")))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(
        get(urlEqualTo(oldUrl("testId")))
          .willReturn(
            ok(value)
          )
      )

      recoverToExceptionIf[HttpException] {
        connector.save("testId", FakeIdentifier, "foobar")
      } map {
        _.responseCode mustEqual Status.BAD_REQUEST
      }

    }
  }

  ".remove" must {
    "remove the data from the old cache if data exists in old cache" in {
      val json = Json.obj(
        FakeIdentifier.toString -> "fake value",
        "other-key" -> "meh"
      )

      val updatedJson = Json.obj(
        "other-key" -> "meh"
      )

      val value = Json.stringify(json)
      val updatedValue = Json.stringify(updatedJson)

      server.stubFor(
        get(urlEqualTo(oldUrl("testId")))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(
        get(urlEqualTo(newUrl("testId")))
          .willReturn(
            notFound
          )
      )

      server.stubFor(
        post(urlEqualTo(oldUrl("testId")))
          .withRequestBody(equalTo(updatedValue))
          .willReturn(
            ok
          )
      )

      connector.remove("testId", FakeIdentifier) map {
        _ mustEqual updatedJson
      }
    }

    "remove the data from the new cache if data exists in new cache" in {
      val json = Json.obj(
        FakeIdentifier.toString -> "fake value",
        "other-key" -> "meh"
      )

      val updatedJson = Json.obj(
        "other-key" -> "meh"
      )

      val value = Json.stringify(json)
      val updatedValue = Json.stringify(updatedJson)

      server.stubFor(
        get(urlEqualTo(newUrl("testId")))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(
        get(urlEqualTo(oldUrl("testId")))
          .willReturn(
            notFound
          )
      )

      server.stubFor(
        post(urlEqualTo(newUrl("testId")))
          .withRequestBody(equalTo(updatedValue))
          .willReturn(
            ok
          )
      )

      connector.remove("testId", FakeIdentifier) map {
        _ mustEqual updatedJson
      }
    }
  }

  ".removeAll" must {
    "remove all the data from new cache if data exists in new cache" in {
      val json = Json.obj(
        FakeIdentifier.toString -> "fake value",
        "other-key" -> "meh"
      )
      val value = Json.stringify(json)
      server.stubFor(
        get(urlEqualTo(oldUrl("testId")))
          .willReturn(
            notFound
          )
      )
      server.stubFor(
        get(urlEqualTo(newUrl("testId")))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(delete(urlEqualTo(newUrl("testId"))).
        willReturn(ok)
      )

      connector.removeAll("testId").map {
        _ mustEqual Ok
      }
    }

    "remove all the data from old cache if data exists in old cache" in {
      val json = Json.obj(
        FakeIdentifier.toString -> "fake value",
        "other-key" -> "meh"
      )
      val value = Json.stringify(json)
      server.stubFor(
        get(urlEqualTo(newUrl("testId")))
          .willReturn(
            notFound
          )
      )
      server.stubFor(
        get(urlEqualTo(oldUrl("testId")))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(delete(urlEqualTo(oldUrl("testId"))).
        willReturn(ok)
      )

      connector.removeAll("testId").map {
        _ mustEqual Ok
      }
    }
  }
}
