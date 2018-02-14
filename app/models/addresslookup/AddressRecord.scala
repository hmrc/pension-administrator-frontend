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

package models.addresslookup

import play.api.libs.json._

case class AddressRecord(address: Address)

object AddressRecord {
  implicit val addressRecordFormat: Format[AddressRecord] = Json.format[AddressRecord]
}

case class Address(addressLine1: String,
                   addressLine2: String,
                   addressLine3: Option[String],
                   addressLine4: Option[String],
                   postcode: Option[String],
                   country: String) {

  def print: String = {
    Seq(
      Some(addressLine1),
      Some(addressLine2),
      addressLine3,
      addressLine4,
      postcode
    ).flatten.mkString(", ")
  }
}

object Address {

  implicit val readAddress: Reads[Address] = {
    import play.api.libs.functional.syntax._
    import play.api.libs.json._

    (
      (__ \ "lines").read[Seq[String]] and
        (__ \ "town").readNullable[String] and
        (__ \ "county").readNullable[String] and
        (__ \ "postcode").readNullable[String] and
        (__ \ "country" \ "name").read[String]
      ) { (lines, town, county, postcode, country) =>
      val line1 = lines.head
      val line2 = lines.tail.head
      Address(line1, line2, town, county, postcode, country)
    }
  }

  implicit val writeAddress: Writes[Address] = {
    import play.api.libs.functional.syntax._
    import play.api.libs.json._
    (
      (__ \ "lines").write[Seq[String]] and
        (__ \ "town").writeNullable[String] and
        (__ \ "county").writeNullable[String] and
        (__ \ "postcode").writeNullable[String] and
        (__ \ "country" \ "name").write[String]
      ) { model =>
      (
        Seq(model.addressLine1, model.addressLine2),
        model.addressLine3,
        model.addressLine4,
        model.postcode,
        model.country)
    }
  }
}
