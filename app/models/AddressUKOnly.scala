/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import play.api.libs.json.*
import utils.countryOptions.CountryOptions

case class AddressRecordUKOnly(address: AddressUKOnly)

object AddressRecordUKOnly {
  implicit val addressRecordFormat: Format[AddressRecord] = Json.format[AddressRecord]
}

case class AddressUKOnly(addressLine1: String,
                   addressLine2: String,
                   addressLine3: Option[String],
                   addressLine4: Option[String],
                   postcode: Option[String]
                  ) {

  def lines: Seq[String] = {
    Seq(
      Some(addressLine1),
      Some(addressLine2),
      addressLine3,
      addressLine4,
      postcode
    ).flatten
  }

  def toTolerantAddress: TolerantAddress = {
    TolerantAddress(
      Some(addressLine1),
      Some(addressLine2),
      addressLine3,
      addressLine4,
      postcode,
      None
    )
  }

}

object AddressUKOnly {

  def applyUK(addressLine1: String,
                 addressLine2: String,
                 addressLine3: Option[String],
                 addressLine4: Option[String],
                 postcode: Option[String]
                 ): AddressUKOnly = new AddressUKOnly(addressLine1, addressLine2, addressLine3, addressLine4, postcode)

  def unapplyUK(address: AddressUKOnly): Option[(String, String, Option[String], Option[String])] = {
    Some((address.addressLine1, address.addressLine2, address.addressLine3, address.addressLine4))
  }

  def fromTolerant(t: TolerantAddress): AddressUKOnly =
    AddressUKOnly(
      t.addressLine1.getOrElse(""),
      t.addressLine2.getOrElse(""),
      t.addressLine3,
      t.addressLine4,
      t.postcode
    )

  implicit val formatsAddress: Format[AddressUKOnly] = Json.format[AddressUKOnly]

}
