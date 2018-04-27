package models.reads

import models.TolerantAddress
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

      "we have a list of addresses" in {
        val addresses = JsArray(Seq(payload,payload))

        val result = addresses.as[Seq[TolerantAddress]](TolerantAddress.postCodeLookupReads)

        result.head.country mustBe tolerantAddressSample.country
      }
    }
  }

  val tolerantAddressSample = TolerantAddress(Some("line1"), Some("line2"), Some("line3"), Some("line4"),Some("ZZ1 1ZZ"),Some("UK"))
}
