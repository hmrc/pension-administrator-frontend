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

import com.fasterxml.jackson.core.JsonParseException
import com.github.tomakehurst.wiremock.client.WireMock._
import identifiers.TypedIdentifier
import org.scalatest._
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.http.{HeaderCarrier, HttpException}
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global

class MicroserviceCacheConnectorSpec extends AsyncWordSpec with MustMatchers with WireMockHelper with OptionValues with RecoverMethods {

  private object FakeIdentifier extends TypedIdentifier[String] {
    override def toString: String = "fake-identifier"
  }

  override protected def portConfigKey: String = "microservice.services.pensions-scheme.port"

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private def url(id: String): String = s"/pensions-scheme/journey-cache/psa/$id"

  private lazy val connector = injector.instanceOf[MicroserviceCacheConnector]
  private lazy val crypto = injector.instanceOf[ApplicationCrypto].JsonCrypto

  ".fetch" must {

    "return `None` when the server returns a 404" in {

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

    "return decrypted data when the server returns 200" in {

      val plaintext = PlainText("{}")

      server.stubFor(
        get(urlEqualTo(url("foo")))
          .willReturn(
            ok(crypto.encrypt(plaintext).value)
          )
      )

      connector.fetch("foo") map {
        result =>
          result.value mustEqual Json.obj()
      }
    }

    "return a failed future when the body can't be transformed into json" in {

      val plaintext = PlainText("foobar")

      server.stubFor(
        get(urlEqualTo(url("foo")))
          .willReturn(
            ok(crypto.encrypt(plaintext).value)
          )
      )

      recoverToSucceededIf[JsonParseException] {
        connector.fetch("foo")
      }

    }

    "return a failed future on upstream error" in {

      server.stubFor(
        get(urlEqualTo(url("foo")))
          .willReturn(
            serverError
          )
      )

      recoverToExceptionIf[HttpException]{
        connector.fetch("foo")
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

      val cryptoText = crypto.encrypt(PlainText(Json.stringify(json))).value

      server.stubFor(
        get(urlEqualTo(url("foo")))
          .willReturn(
            notFound
          )
      )

      server.stubFor(
        post(urlEqualTo(url("foo")))
          .withRequestBody(equalTo(cryptoText))
          .willReturn(
            ok
          )
      )

      connector.save("foo", FakeIdentifier, "foobar") map {
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

      val cryptoText = crypto.encrypt(PlainText(Json.stringify(json))).value
      val updatedCrypto = crypto.encrypt(PlainText(Json.stringify(updatedJson))).value

      server.stubFor(
        get(urlEqualTo(url("foo")))
          .willReturn(
            ok(cryptoText)
          )
      )

      server.stubFor(
        post(urlEqualTo(url("foo")))
          .withRequestBody(equalTo(updatedCrypto))
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

      val cryptoText = crypto.encrypt(PlainText(Json.stringify(json))).value
      val updatedCrypto = crypto.encrypt(PlainText(Json.stringify(updatedJson))).value

      server.stubFor(
        get(urlEqualTo(url("foo")))
          .willReturn(
            ok(cryptoText)
          )
      )

      server.stubFor(
        post(urlEqualTo(url("foo")))
          .withRequestBody(equalTo(updatedCrypto))
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

      val cryptoText = crypto.encrypt(PlainText(Json.stringify(json))).value
      val updatedCrypto = crypto.encrypt(PlainText(Json.stringify(updatedJson))).value

      server.stubFor(
        get(urlEqualTo(url("foo")))
          .willReturn(
            ok(cryptoText)
          )
      )

      server.stubFor(
        post(urlEqualTo(url("foo")))
          .withRequestBody(equalTo(updatedCrypto))
          .willReturn(
            serverError
          )
      )


      recoverToExceptionIf[HttpException]{
        connector.save("foo", FakeIdentifier, "foobar")
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

      val cryptoText = crypto.encrypt(PlainText(Json.stringify(json))).value
      val updatedCrypto = crypto.encrypt(PlainText(Json.stringify(updatedJson))).value

      server.stubFor(
        get(urlEqualTo(url("foo")))
          .willReturn(
            ok(cryptoText)
          )
      )

      server.stubFor(
        post(urlEqualTo(url("foo")))
          .withRequestBody(equalTo(updatedCrypto))
          .willReturn(
            ok
          )
      )

      connector.remove("foo", FakeIdentifier) map {
        _ mustEqual updatedJson
      }
    }
  }
}
