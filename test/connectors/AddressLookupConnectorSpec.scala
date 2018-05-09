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


import org.scalatest.RecoverMethods

import scala.concurrent.ExecutionContext.Implicits.global
import com.github.tomakehurst.wiremock.client.WireMock._
import models.TolerantAddress
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.json.{JsArray, JsString, Json}
import play.api.test.Helpers._
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.http.{HeaderCarrier, HttpException}
import utils.WireMockHelper


class AddressLookupConnectorSpec extends WordSpec
  with MustMatchers
  with WireMockHelper
  with ScalaFutures
  with IntegrationPatience
  with RecoverMethods {

  private def url = s"/v2/uk/addresses?postcode=ZZ11ZZ"

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override protected def portConfigKey: String = "microservice.services.address-lookup.port"

  private lazy val connector = injector.instanceOf[AddressLookupConnector]

  ".addressLookupByPostCode" must {
    "returns an Ok and empty list" which {
      "means the AddressLookup has found no data for postcode" in {
        server.stubFor(
          get(urlEqualTo(url)).willReturn
          (
            aResponse().withStatus(OK)
              .withBody("[]")
          )
        )
        whenReady(connector.addressLookupByPostCode("ZZ11ZZ")) {
          result =>
            result mustEqual Nil
        }
      }

    }

    "returns an ok and Seq of Addresses" which {
      "means the AddressLookup has found data for the postcode" in {
        val payload = JsArray(Seq(Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"), JsString("line2"), JsString("line3"), JsString("line4"))),
          "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code" -> "UK")))))

        val addresses = PlainText(Json.stringify(payload)).value

        val tolerantAddressSample = TolerantAddress(Some("line1"), Some("line2"), Some("line3"), Some("line4"), Some("ZZ1 1ZZ"), Some("UK"))
        server.stubFor(
          get(urlEqualTo(url)).willReturn
          (
            aResponse().withStatus(OK)
              .withBody(addresses)
          )
        )
        whenReady(connector.addressLookupByPostCode("ZZ11ZZ")) {
          result =>
            result mustEqual Seq(tolerantAddressSample)
        }
      }
    }
    "returns an exception" which {
      "means the Address Lookup has returned a non 200 response " in {

        val tolerantAddressSample = TolerantAddress(Some("line1"), Some("line2"), Some("line3"), Some("line4"), Some("ZZ1 1ZZ"), Some("UK"))
        server.stubFor(
          get(urlEqualTo(url)).willReturn
          (
            aResponse().withStatus(NOT_FOUND).withBody("Something is wrong")
          )
        )

        recoverToSucceededIf[HttpException] {

          connector.addressLookupByPostCode("ZZ11ZZ")

        }
      }

    }
  }
}
