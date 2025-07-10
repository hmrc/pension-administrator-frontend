/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.cache.UserAnswersCacheConnector
import identifiers.TypedIdentifier
import org.scalatest._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Results._
import uk.gov.hmrc.http.{HeaderCarrier, HttpException}
import utils.WireMockHelper

class UserAnswersCacheConnectorSpec extends AsyncWordSpec with Matchers with WireMockHelper with OptionValues with RecoverMethods {

  private object FakeIdentifier extends TypedIdentifier[String] {
    override def toString: String = "fake-identifier"
  }

  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def url: String = "/pension-administrator/journey-cache/psa-data-self"

  private lazy val connector = injector.instanceOf[UserAnswersCacheConnector]

  ".fetch" must {

    "return `None` when the server returns a 404" in {
      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(
            notFound
          )
      )

      connector.fetch.map {
        result =>
          result mustNot be(defined)
      }
    }

    "return data when the server returns 200" in {
      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(
            ok("{}")
          )
      )

      connector.fetch.map {
        result =>
          result.value mustEqual Json.obj()
      }
    }

    "return a failed future on upstream error" in {

      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(
            serverError
          )
      )

      recoverToExceptionIf[HttpException] {
        connector.fetch
      } map {
        _.responseCode mustEqual Status.INTERNAL_SERVER_ERROR
      }

    }
  }

  ".save" must {

    "insert when no data exists" in {

      val json = Json.obj(
        "fake-identifier" -> "foobar"
      )

      val value = Json.stringify(json)

      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(
            notFound
          )
      )

      server.stubFor(
        post(urlEqualTo(url))
          .withRequestBody(equalTo(value))
          .willReturn(
            ok
          )
      )

      connector.save(FakeIdentifier, "foobar") map {
        _ mustEqual json
      }
    }

    "add fields to existing data" in {

      val json = Json.obj(
        "foo" -> "bar"
      )

      val updatedJson = Json.obj(
        "foo" -> "bar",
        "fake-identifier" -> "foobar"
      )

      val value = Json.stringify(json)
      val updatedValue = Json.stringify(updatedJson)

      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(
        post(urlEqualTo(url))
          .withRequestBody(equalTo(updatedValue))
          .willReturn(
            ok
          )
      )

      connector.save(FakeIdentifier, "foobar") map {
        _ mustEqual updatedJson
      }
    }

    "update existing data" in {

      val json = Json.obj(
        "fake-identifier" -> "foo"
      )

      val updatedJson = Json.obj(
        "fake-identifier" -> "foobar"
      )

      val value = Json.stringify(json)
      val updatedValue = Json.stringify(updatedJson)

      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(
        post(urlEqualTo(url))
          .withRequestBody(equalTo(updatedValue))
          .willReturn(
            ok
          )
      )

      connector.save(FakeIdentifier, "foobar") map {
        _ mustEqual updatedJson
      }
    }

    "return a failed future on upstream error" in {

      val json = Json.obj(
        "fake-identifier" -> "foo"
      )

      val updatedJson = Json.obj(
        "fake-identifier" -> "foobar"
      )

      val value = Json.stringify(json)
      val updatedValue = Json.stringify(updatedJson)

      server.stubFor(
        get(urlEqualTo(url))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(
        post(urlEqualTo(url))
          .withRequestBody(equalTo(updatedValue))
          .willReturn(
            serverError
          )
      )

      recoverToExceptionIf[HttpException] {
        connector.save(FakeIdentifier, "foobar")
      } map {
        _.responseCode mustEqual Status.INTERNAL_SERVER_ERROR
      }

    }
  }

  ".remove" must {
    "remove existing data" in {
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
        get(urlEqualTo(url))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(
        post(urlEqualTo(url))
          .withRequestBody(equalTo(updatedValue))
          .willReturn(
            ok
          )
      )

      connector.remove(FakeIdentifier) map {
        _ mustEqual updatedJson
      }
    }
  }

  ".removeAll" must {
    "remove all the data" in {
      server.stubFor(delete(urlEqualTo(url)).
        willReturn(ok)
      )

      connector.removeAll.map {
        _ mustEqual Ok
      }
    }
  }
}
