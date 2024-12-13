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

package models.register

import play.api.mvc.PathBindable

sealed trait RegistrationStatus

object RegistrationStatus {
  case object LimitedCompany extends RegistrationStatus
  case object Partnership extends RegistrationStatus
  case object Individual extends RegistrationStatus

  implicit def pathBindable: PathBindable[RegistrationStatus] = new PathBindable[RegistrationStatus] {
    override def bind(key: String, value: String): Either[String, RegistrationStatus] = {
      value match {
        case "company" => Right(RegistrationStatus.LimitedCompany)
        case "partnership" => Right(RegistrationStatus.Partnership)
        case "individual" => Right(RegistrationStatus.Individual)
        case _ => Left("Invalid RegistrationLegalStatus")
      }
    }

    override def unbind(key: String, value: RegistrationStatus): String = {
      value match {
        case RegistrationStatus.LimitedCompany => "company"
        case RegistrationStatus.Partnership => "partnership"
        case RegistrationStatus.Individual => "individual"
      }
    }
  }
}

