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
import connectors.cache.ICacheConnector
import identifiers.TypedIdentifier
import org.scalatest._
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.Results._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.WireMockHelper

class ICacheConnectorSpec extends AsyncWordSpec with Matchers with WireMockHelper with OptionValues with RecoverMethods {

  private object FakeIdentifier extends TypedIdentifier[String] {
    override def toString: String = "fake-identifier"
  }

  override protected def portConfigKey: String = "microservice.services.pension-administrator.port"

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def url(id: String): String = s"/pension-administrator/journey-cache/psa/$id"

  private lazy val connector = injector.instanceOf[ICacheConnector]

  ".fetch" must {

    "return `None` when the server returns a 404" ignore {
      server.stubFor(
        get(urlEqualTo(url("foo")))
          .willReturn(
            notFound
          )
      )

      connector.fetch("foo") map {
        result =>
          result mustNot be(defined)
      }
    }

    "return data when the server returns 200" in {
      server.stubFor(
        get(urlEqualTo(url("foo")))
          .willReturn(
            ok("{}")
          )
      )

      connector.fetch("foo") map {
        result =>
          result.value mustEqual Json.obj()
      }
    }

    "return a failed future on upstream error" in {

      server.stubFor(
        get(urlEqualTo(url("foo")))
          .willReturn(
            serverError
          )
      )

      recoverToExceptionIf[UpstreamErrorResponse] {
        connector.fetch("foo")
      } map {
        _.statusCode mustEqual Status.INTERNAL_SERVER_ERROR
      }

    }
  }

  ".save" must {

    "insert when no data exists" ignore {

        val updatedJson = Json.obj(
          "fake-identifier" -> "foobar"
        )

        val updatedValue = Json.stringify(updatedJson)

        server.stubFor(
          get(urlEqualTo(url("foo")))
            .willReturn(
              notFound
            )
        )

        server.stubFor(
          post(urlEqualTo(url("foo")))
            .withRequestBody(equalTo(updatedValue))
            .willReturn(
              ok
            )
        )

        connector.save("foo", FakeIdentifier, "foobar") map {
          _ mustEqual updatedJson
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
        get(urlEqualTo(url("foo")))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(
        post(urlEqualTo(url("foo")))
          .withRequestBody(equalTo(updatedValue))
          .willReturn(
            ok
          )
      )

      connector.save("foo", FakeIdentifier, "foobar") map {
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
        get(urlEqualTo(url("foo")))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(
        post(urlEqualTo(url("foo")))
          .withRequestBody(equalTo(updatedValue))
          .willReturn(
            ok
          )
      )

      connector.save("foo", FakeIdentifier, "foobar") map {
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
        get(urlEqualTo(url("foo")))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(
        post(urlEqualTo(url("foo")))
          .withRequestBody(equalTo(updatedValue))
          .willReturn(
            serverError
          )
      )


      recoverToExceptionIf[UpstreamErrorResponse] {
        connector.save("foo", FakeIdentifier, "foobar")
      } map {
        _.statusCode mustEqual Status.INTERNAL_SERVER_ERROR
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
        get(urlEqualTo(url("foo")))
          .willReturn(
            ok(value)
          )
      )

      server.stubFor(
        post(urlEqualTo(url("foo")))
          .withRequestBody(equalTo(updatedValue))
          .willReturn(
            ok
          )
      )

      connector.remove("foo", FakeIdentifier) map {
        _ mustEqual updatedJson
      }
    }
  }

  ".removeAll" must {
    "remove all the data" in {
      server.stubFor(delete(urlEqualTo(url("foo"))).
        willReturn(ok)
      )

      connector.removeAll("foo").map {
        _ mustEqual Ok
      }
    }
  }

}
