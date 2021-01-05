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

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class PersonName(firstName: String, lastName: String, isDeleted: Boolean = false, isNew: Boolean = false) {
  def fullName: String = s"$firstName $lastName"
}

object PersonName {

  implicit val reads: Reads[PersonName] =
    ((JsPath \ "firstName").read[String] and
      (JsPath \ "lastName").read[String] and
      ((JsPath \ "isDeleted").read[Boolean] orElse Reads.pure(false)) and
      ((JsPath \ "isNew").read[Boolean] orElse Reads.pure(false))
      ) (PersonName.apply _)

  implicit val writes: Writes[PersonName] = Json.writes[PersonName]

  def applyDelete(firstName: String, lastName: String): PersonName = {
    PersonName(firstName, lastName)
  }

  def unapplyDelete(personDetails: PersonName): Option[(String, String)] = {
    Some((personDetails.firstName, personDetails.lastName))
  }

}

