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

package models.reads

import models.{NoAddressLinesFoundException, TolerantAddress}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json._


class TolerantAddressReadsSpec extends WordSpec with MustMatchers with OptionValues {
  "A Postcode Lookup response payload" should {
    "map correctly to a tolerant addres" when {
      val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"), JsString("line2"), JsString("line3"), JsString("line4"))),
      "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK")))

      "We have line1" in {
        val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

        result.addressLine1 mustBe tolerantAddressSample.addressLine1
      }

      "We have line2" in {
        val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

        result.addressLine2 mustBe tolerantAddressSample.addressLine2
      }

      "We have line3" in {
        val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

        result.addressLine3 mustBe tolerantAddressSample.addressLine3
      }

      "We have line4" in {
        val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

        result.addressLine4 mustBe tolerantAddressSample.addressLine4
      }

      "we have a postcode" in {
        val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

        result.postcode mustBe tolerantAddressSample.postcode
      }

      "we have a country code" in {
        val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

        result.country mustBe tolerantAddressSample.country
      }

      "We have more than 4 lines in the array containing address lines" in {
        val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"), JsString("line2"), JsString("line3"), JsString("line4"), JsString("line5"))),
          "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK")))

        val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

        result.addressLine4 mustBe tolerantAddressSample.addressLine4
      }

      "we have a town" which {
        "maps to line2" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "town" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine2 = Some("Tyne and Wear"))

          result.addressLine2 mustBe expectedAddress.addressLine2
        }

        "maps to line3" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("line2"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "town" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine3 = Some("Tyne and Wear"))

          result.addressLine3 mustBe expectedAddress.addressLine3
        }

        "maps to line4" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("line2"),JsString("line3"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "town" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine4 = Some("Tyne and Wear"))

          result.addressLine4 mustBe expectedAddress.addressLine4
        }

        "is already included in element 2 in lines sequence" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("Tyne & Wear"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "town" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

          result.addressLine3 mustBe None
        }

        "is already included in element 3 in lines sequence" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("line2"),JsString("Tyne & Wear"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "town" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

          result.addressLine4 mustBe None
        }
      }

      "we have a county" which {
        "maps to line2" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "county" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine2 = Some("Tyne and Wear"))

          result.addressLine2 mustBe expectedAddress.addressLine2
        }

        "maps to line3" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("line2"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "county" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine3 = Some("Tyne and Wear"))

          result.addressLine3 mustBe expectedAddress.addressLine3
        }

        "maps to line4" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("line2"),JsString("line3"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "county" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine4 = Some("Tyne and Wear"))

          result.addressLine4 mustBe expectedAddress.addressLine4
        }

        "is already included in element 2 in lines sequence" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("Tyne & Wear"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "county" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

          result.addressLine3 mustBe None
        }

        "is already included in element 3 in lines sequence" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("line2"),JsString("Tyne & Wear"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "county" -> JsString("Tyne & Wear")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

          result.addressLine4 mustBe None
        }
      }

      "we have a town and county" which {
        "maps town to line 2 and county to line 3" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "town" -> JsString("Tyne & Wear"), "county" -> JsString("County Test")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine2 = Some("Tyne and Wear"), addressLine3 = Some("County Test"))

          result.addressLine2 mustBe expectedAddress.addressLine2
          result.addressLine3 mustBe expectedAddress.addressLine3
        }


        "maps town to line 3 and county to line 4" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("line2"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "town" -> JsString("Tyne & Wear"), "county" -> JsString("County Test")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine3 = Some("Tyne and Wear"), addressLine4 = Some("County Test"))

          result.addressLine3 mustBe expectedAddress.addressLine3
          result.addressLine4 mustBe expectedAddress.addressLine4
        }

        "town is already included in lines so we map county to line 3" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("Tyne and Wear"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "town" -> JsString("Tyne & Wear"), "county" -> JsString("County Test")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine3 = Some("County Test"))

          result.addressLine3 mustBe expectedAddress.addressLine3
          result.addressLine4 mustBe None
        }

        "county is already included in lines so don't map it to line 4" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("County Test"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "town" -> JsString("Tyne & Wear"), "county" -> JsString("County Test")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine3 = Some("Tyne and Wear"))

          result.addressLine3 mustBe expectedAddress.addressLine3
          result.addressLine4 mustBe None
        }

        "county and town are in lines so we don't map them" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("Tyne & Wear"),JsString("County Test"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "town" -> JsString("Tyne & Wear"), "county" -> JsString("County Test")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

          result.addressLine3 mustBe None
          result.addressLine4 mustBe None
        }


        "maps county to line 4" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("line2"),JsString("line3"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "town" -> JsString("Tyne & Wear"), "county" -> JsString("County Test")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine4 = Some("County Test"))

          result.addressLine3 mustBe expectedAddress.addressLine3
          result.addressLine4 mustBe expectedAddress.addressLine4
        }

        "county is already in lines so we don't map it to line 4" in {
          val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1"),JsString("line2"),JsString("County Test"))),
            "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK"), "county" -> JsString("County Test")))
          val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)
          val expectedAddress = tolerantAddressSample.copy(addressLine3 = Some("County Test"))

          result.addressLine3 mustBe expectedAddress.addressLine3
          result.addressLine4 mustBe None
        }
      }


      "we have a list of addresses" in {
        val addresses = JsArray(Seq(payload,payload))

        val result = addresses.as[Seq[TolerantAddress]](TolerantAddress.postCodeLookupReads)

        result.head.country mustBe tolerantAddressSample.country
      }

      "we have a & in the address" in {
        val tolerantAddressSample = TolerantAddress(Some("line1 and line1"), Some("line2 and line2"), Some("line3 and line3"), Some("line4 and line4"),Some("ZZ1 1ZZ"),Some("UK"))
        val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq(JsString("line1 & line1"), JsString("line2 & line2"), JsString("line3 & line3"), JsString("line4 & line4"))),
          "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK")))

        val result = payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)

        result.addressLine1 mustBe tolerantAddressSample.addressLine1
        result.addressLine2 mustBe tolerantAddressSample.addressLine2
        result.addressLine3 mustBe tolerantAddressSample.addressLine3
        result.addressLine4 mustBe tolerantAddressSample.addressLine4
      }
    }
    "throw a NoAddressLines exception" when {
      "we have no lines in the address" in {
        val payload = Json.obj("address" -> Json.obj("lines" -> JsArray(Seq()),
          "postcode" -> "ZZ1 1ZZ", "country" -> Json.obj("code"-> "UK")))

        the [NoAddressLinesFoundException] thrownBy(payload.as[TolerantAddress](TolerantAddress.postCodeLookupAddressReads)) must have message("Address with no address lines received")
      }
    }
  }

  val tolerantAddressSample = TolerantAddress(Some("line1"), Some("line2"), Some("line3"), Some("line4"),Some("ZZ1 1ZZ"),Some("UK"))
}
