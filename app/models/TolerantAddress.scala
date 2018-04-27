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
import play.api.libs.json
import play.api.libs.json._
import uk.gov.hmrc.http.HttpReads

import scala.language.implicitConversions

case class TolerantAddress(addressLine1: Option[String],
                           addressLine2: Option[String],
                           addressLine3: Option[String],
                           addressLine4: Option[String],
                           postcode: Option[String],
                           country: Option[String]) {

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

  def print: String = {
    lines.mkString(", ")
  }

  def toAddress:Address = {
    Address(
      addressLine1.getOrElse(""),
      addressLine2.getOrElse(""),
      addressLine3,
      addressLine4,
      postcode,
      country.getOrElse("")
    )
  }
}

object TolerantAddress {

  val postCodeLookupAddressReads : Reads[TolerantAddress] = (
    (JsPath \ "address" \ "lines").read[List[String]] and
      (JsPath \ "address" \ "postcode").read[String] and
      (JsPath \ "address" \ "country" \ "code").read[String]
    )((lines,postCode,countryCode) => {
    val addressLines : (Option[String],Option[String],Option[String],Option[String]) = {
      lines.size match {
        case 1 => (Some(lines(0)),None,None,None)
        case 2 => (Some(lines(0)),Some(lines(1)),None,None)
        case 3 => (Some(lines(0)),Some(lines(1)),Some(lines(2)),None)
        case 4 => (Some(lines(0)),Some(lines(1)),Some(lines(2)),Some(lines(3)))
      }
    }
    TolerantAddress(addressLines._1, addressLines._2, addressLines._3, addressLines._4, Some(postCode),Some(countryCode))
  })



  val postCodeLookupReads : Reads[Seq[TolerantAddress]] = Reads {
    json =>
      json.validate[Seq[JsValue]].flatMap(addresses => {
        addresses.foldLeft[JsResult[List[TolerantAddress]]](JsSuccess(List.empty)){
          (addresses,currentAddress) => {
            for {
              sequenceOfAddressess <- addresses
              address <- currentAddress.validate[TolerantAddress](postCodeLookupAddressReads)
            } yield sequenceOfAddressess :+ address
          }
        }
      })
  }

  implicit lazy val formatsTolerantAddress: Format[TolerantAddress] = (
    (JsPath \ "addressLine1").formatNullable[String] and
    (JsPath \ "addressLine2").formatNullable[String] and
    (JsPath \ "addressLine3").formatNullable[String] and
    (JsPath \ "addressLine4").formatNullable[String] and
    (JsPath \ "postalCode").formatNullable[String] and
    (JsPath \ "countryCode").formatNullable[String]
  )(TolerantAddress.apply, unlift(TolerantAddress.unapply))

  implicit def convert(tolerant: TolerantAddress): Option[Address] = {
    for {
      addressLine1 <- tolerant.addressLine1
      addressLine2 <- tolerant.addressLine2
      country <- tolerant.country
    } yield {
      Address(
        addressLine1,
        addressLine2,
        tolerant.addressLine3,
        tolerant.addressLine4,
        tolerant.postcode,
        country
      )
    }
  }
}
