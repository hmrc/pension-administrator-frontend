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
import play.api.libs.json._

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
      (JsPath \ "address" \ "country" \ "code").read[String] and
      (JsPath \ "address" \ "town").readNullable[String] and
      (JsPath \ "address" \ "county").readNullable[String]
    )((lines,postCode,countryCode,town,county) => {
    val linesWithNoAmpersand = lines.map(line => line.replace("&","and"))
    val addressLines : (Option[String],Option[String],Option[String],Option[String]) = {
      lines.size match {
        case 1 => {
          val townOrCounty = getTownOrCounty(town, county, linesWithNoAmpersand)
          (Some(linesWithNoAmpersand(0)),townOrCounty._1,townOrCounty._2,None)
        }
        case 2 => {
          val townOrCounty = getTownOrCounty(town, county, linesWithNoAmpersand)

          (Some(linesWithNoAmpersand(0)),Some(linesWithNoAmpersand(1)),townOrCounty._1,townOrCounty._2)
        }
        case 3 => {
          val townOrCounty = getTownOrCounty(town, county, linesWithNoAmpersand)
          val townOrCountyValue = if (townOrCounty._2.isDefined) townOrCounty._2 else townOrCounty._1

          (Some(linesWithNoAmpersand(0)),Some(linesWithNoAmpersand(1)),Some(linesWithNoAmpersand(2)),townOrCountyValue)
        }
        case numberOfLines if (numberOfLines >= 4) => (Some(linesWithNoAmpersand(0)),Some(linesWithNoAmpersand(1)),Some(linesWithNoAmpersand(2)),Some(linesWithNoAmpersand(3)))
      }
    }
    TolerantAddress(addressLines._1, addressLines._2, addressLines._3, addressLines._4, Some(postCode),Some(countryCode))
  })


  private def checkIfElementAlreadyExistsInLines(linesWithNoAmpersand: List[String], elementToCheck: String) = {
    linesWithNoAmpersand.mkString("").toLowerCase().contains(elementToCheck.trim().toLowerCase())
  }

  private def getTownOrCounty(town: Option[String], county: Option[String], addressLines: List[String]) = {

    (town, county) match {
      case (Some(town), None) => {
        val formattedTown = replaceAmpersanWithAnd(town)
        (if (checkIfElementAlreadyExistsInLines(addressLines,formattedTown)) None else Some(formattedTown), None)
      }
      case (None, Some(county)) => {
        val formattedCounty = replaceAmpersanWithAnd(county)
        (if (checkIfElementAlreadyExistsInLines(addressLines,formattedCounty)) None else Some(formattedCounty), None)
      }
      case (Some(town), Some(county)) => {
        val formattedTown = replaceAmpersanWithAnd(town)
        val formattedCounty = replaceAmpersanWithAnd(county)
        val townAlreadyExists = checkIfElementAlreadyExistsInLines(addressLines,formattedTown)
        val countyAlreadyExists = checkIfElementAlreadyExistsInLines(addressLines,formattedCounty)

        (townAlreadyExists,countyAlreadyExists) match {
          case (true,false) => (Some(replaceAmpersanWithAnd(county)), None)
          case (false,true) => (Some(replaceAmpersanWithAnd(town)), None)
          case (true,true) => (None, None)
          case _ => (Some(replaceAmpersanWithAnd(town)), Some(replaceAmpersanWithAnd(county)))
        }
      }
      case _ => (None, None)
    }
  }

  private def replaceAmpersanWithAnd(data : String) : String = (data.replace("&","and"))


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
