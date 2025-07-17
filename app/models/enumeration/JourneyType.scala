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

package models.enumeration

import play.api.mvc.PathBindable

enum JourneyType extends Enum[JourneyType] {
  case PSA
  case INVITE
  case VARIATION

  override def toString: String = this.name() match {
    case "PSA" => "PSA"
    case "INVITE" => "PSAInvite"
    case "VARIATION" => "Variation"
  }
}

object JourneyType {
  implicit def pathBinder(implicit stringBinder: PathBindable[String]): PathBindable[JourneyType] =
    new PathBindable[JourneyType] {
      override def bind(key: String, value: String): Either[String, JourneyType] =
        value match {
          case "PSA" => Right(JourneyType.PSA)
          case "PSAInvite" => Right(JourneyType.INVITE)
          case "Variation" => Right(JourneyType.VARIATION)
          case _ => Left("Invalid JourneyType")
        }

      override def unbind(key: String, value: JourneyType): String =
        value match {
          case JourneyType.PSA => "PSA"
          case JourneyType.INVITE => "PSAInvite"
          case JourneyType.VARIATION => "Variation"
        }
    }

}
