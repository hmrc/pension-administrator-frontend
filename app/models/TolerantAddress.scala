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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}

case class TolerantAddress(addressLine1: Option[String],
                           addressLine2: Option[String],
                           addressLine3: Option[String],
                           addressLine4: Option[String],
                           postcode: Option[String],
                           country: Option[String]) {

  def toAddress: Option[Address] = {
    for {
      addressLine1 <- this.addressLine1
      addressLine2 <- this.addressLine2
      country <- this.country
    } yield {
      Address(
        addressLine1,
        addressLine2,
        this.addressLine3,
        this.addressLine4,
        this.postcode,
        country
      )
    }
  }

  def lines: Seq[String] = {
    Seq(
      this.addressLine1,
      this.addressLine2,
      this.addressLine3,
      this.addressLine4,
      this.country,
      this.postcode
    ).flatten(s => s)
  }

}

object TolerantAddress {

  implicit lazy val formatsTolerantAddress: Format[TolerantAddress] = (
    (JsPath \ "addressLine1").formatNullable[String] and
    (JsPath \ "addressLine2").formatNullable[String] and
    (JsPath \ "addressLine3").formatNullable[String] and
    (JsPath \ "addressLine4").formatNullable[String] and
    (JsPath \ "postalCode").formatNullable[String] and
    (JsPath \ "countryCode").formatNullable[String]
  )(TolerantAddress.apply, unlift(TolerantAddress.unapply))

}
