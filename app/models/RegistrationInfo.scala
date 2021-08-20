/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.{Format, Json}
import utils.{Enumerable, WithName}

case class RegistrationInfo(
                             legalStatus: RegistrationLegalStatus,
                             sapNumber: String,
                             noIdentifier: Boolean,
                             customerType: RegistrationCustomerType,
                             idType: Option[RegistrationIdType],
                             idNumber: Option[String]
                           )

object RegistrationInfo {
  implicit lazy val formatsRegistrationInfo: Format[RegistrationInfo] = Json.format[RegistrationInfo]
}

sealed trait RegistrationLegalStatus

object RegistrationLegalStatus extends Enumerable.Implicits {

  case object Individual extends WithName("Individual") with RegistrationLegalStatus

  case object Partnership extends WithName("Partnership") with RegistrationLegalStatus

  case object LimitedCompany extends WithName("Limited Company") with RegistrationLegalStatus

  val values = Seq(
    Individual,
    Partnership,
    LimitedCompany
  )

  implicit val enumerable: Enumerable[RegistrationLegalStatus] =
    Enumerable(values.map(v => v.toString -> v): _*)

}

sealed trait RegistrationCustomerType

object RegistrationCustomerType extends Enumerable.Implicits {

  case object UK extends WithName("UK") with RegistrationCustomerType

  case object NonUK extends WithName("NON-UK") with RegistrationCustomerType

  def fromAddress(address: TolerantAddress): RegistrationCustomerType = {
    address.countryOpt match {
      case Some("GB") | Some("UK") => UK
      case Some(_) => NonUK
      case _ => throw new IllegalArgumentException(s"Cannot determine RegistrationCustomerType for country: ${address.countryOpt}")
    }
  }

  val values = Seq(
    UK,
    NonUK
  )

  implicit val enumerable: Enumerable[RegistrationCustomerType] =
    Enumerable(values.map(v => v.toString -> v): _*)

}

sealed trait RegistrationIdType

object RegistrationIdType extends Enumerable.Implicits {

  case object Nino extends WithName("NINO") with RegistrationIdType

  case object UTR extends WithName("UTR") with RegistrationIdType

  val values = Seq(
    Nino,
    UTR
  )

  implicit val enumerable: Enumerable[RegistrationIdType] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
