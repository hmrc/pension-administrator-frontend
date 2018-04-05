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
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import uk.gov.hmrc.http.{HeaderCarrier, HttpException}
import utils.WireMockHelper

import scala.concurrent.ExecutionContext.Implicits.global

class MicroserviceCacheConnectorSpec extends WordSpec
  with MustMatchers with WireMockHelper with OptionValues
  with ScalaFutures with IntegrationPatience {

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
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      whenReady(connector.fetch("foo")) {
        result =>
          result mustNot be(defined)
      }
    }

    "return decrypted data when the server returns 200" in {

      val plaintext = PlainText("{}")

      server.stubFor(
        get(urlEqualTo(url("foo")))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(crypto.encrypt(plaintext).value)
          )
      )

      whenReady(connector.fetch("foo")) {
        result =>
          result.value mustEqual Json.obj()
      }
    }

    "return a failed future when the body can't be transformed into json" in {

      val plaintext = PlainText("foobar")

      server.stubFor(
        get(urlEqualTo(url("foo")))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(crypto.encrypt(plaintext).value)
          )
      )

      whenReady(connector.fetch("foo").failed) {
        exception =>
          exception mustBe a[JsonParseException]
      }
    }

    "return a failed future on upstream error" in {

      server.stubFor(
        get(urlEqualTo(url("foo")))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      whenReady(connector.fetch("foo").failed) {
        case exception: HttpException =>
          exception.responseCode mustEqual INTERNAL_SERVER_ERROR
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
            aResponse()
              .withStatus(NOT_FOUND)
          )
      )

      server.stubFor(
        post(urlEqualTo(url("foo")))
          .withRequestBody(equalTo(cryptoText))
          .willReturn(
            aResponse()
              .withStatus(OK)
          )
      )

      whenReady(connector.save("foo", FakeIdentifier, "foobar")) {
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
            aResponse()
              .withStatus(OK)
              .withBody(cryptoText)
          )
      )

      server.stubFor(
        post(urlEqualTo(url("foo")))
          .withRequestBody(equalTo(updatedCrypto))
          .willReturn(
            aResponse()
              .withStatus(OK)
          )
      )

      whenReady(connector.save("foo", FakeIdentifier, "foobar")) {
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
            aResponse()
              .withStatus(OK)
              .withBody(cryptoText)
          )
      )

      server.stubFor(
        post(urlEqualTo(url("foo")))
          .withRequestBody(equalTo(updatedCrypto))
          .willReturn(
            aResponse()
              .withStatus(OK)
          )
      )

      whenReady(connector.save("foo", FakeIdentifier, "foobar")) {
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
            aResponse()
              .withStatus(OK)
              .withBody(cryptoText)
          )
      )

      server.stubFor(
        post(urlEqualTo(url("foo")))
          .withRequestBody(equalTo(updatedCrypto))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      whenReady(connector.save("foo", FakeIdentifier, "foobar").failed) {
        case exception: HttpException =>
          exception.responseCode mustEqual INTERNAL_SERVER_ERROR
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
            aResponse()
              .withStatus(OK)
              .withBody(cryptoText)
          )
      )

      server.stubFor(
        post(urlEqualTo(url("foo")))
          .withRequestBody(equalTo(updatedCrypto))
          .willReturn(
            aResponse()
              .withStatus(OK)
          )
      )

      whenReady(connector.remove("foo", FakeIdentifier)) {
        _ mustEqual updatedJson
      }
    }
  }
}
