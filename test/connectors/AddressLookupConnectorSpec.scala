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
import models.TolerantAddress
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpException}
import utils.WireMockHelper
import com.github.tomakehurst.wiremock.client.WireMock._

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

       val payload =
          """[{"uprn":990091234524,
            |"localCustodian":{"code":121,"name":"North Somerset","J":""},
            |"id":"GB990091234524",
            |"language":"en",
            |"B":"",
            |"address":{"postcode":"ZZ1 1ZZ","F":"",
            |"country":{"code":"UK","name":"United Kingdom","R":""},
            |"county":"Somerset",
            |"subdivision":{"code":"GB-ENG","name":"England","B":""},
            |"town":"Anytown",
            |"lines":["10 Other Place","Some District"]}
            |},
            |{"Y":"",
            |"uprn":990091234514,
            |"localCustodian":{"code":121,"name":"North Somerset","H":""},
            |"id":"GB990091234514",
            |"language":"en",
            |"address":{"postcode":"ZZ1 1ZZ",
            |"country":{"code":"UK","name":"United Kingdom","U":""},
            |"county":"Somerset",
            |"subdivision":{"code":"GB-ENG","name":"England","R":""},
            |"town":"Anytown",
            |"lines":["2 Other Place","Some District"],"D":""}
            |}
            |]
            |""".stripMargin



        val tolerantAddressSample = Seq(
          TolerantAddress(Some("10 Other Place"),Some("Some District"),None,None,Some("ZZ1 1ZZ"),Some("UK")),
          TolerantAddress(Some("2 Other Place"),Some("Some District"),None,None,Some("ZZ1 1ZZ"),Some("UK"))
        )


        server.stubFor(
          get(urlEqualTo(url))
            .withHeader("user-agent", matching(".+"))
              .willReturn
          (
            aResponse().withStatus(OK)
              .withBody(payload)
          )
        )
        whenReady(connector.addressLookupByPostCode("ZZ11ZZ")) {
          result =>
            result mustEqual tolerantAddressSample
        }
      }
    }
    "returns an exception" which {
      "means the Address Lookup has returned a non 200 response " in {

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
