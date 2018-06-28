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

package models.register.company.directors

import java.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class DirectorDetails(firstName: String, middleName: Option[String], lastName: String, dateOfBirth: LocalDate, isDeleted: Boolean = false) {
  def fullName: String = middleName match {
    case Some(middle) => s"$firstName $middle $lastName"
    case _ => s"$firstName $lastName"
  }
}

object DirectorDetails {

  implicit val reads: Reads[DirectorDetails] =
    ((JsPath \ "firstName").read[String] and
      (JsPath \ "middleName").readNullable[String] and
      (JsPath \ "lastName").read[String] and
      (JsPath \ "dateOfBirth").read[LocalDate] and
      ((JsPath \ "isDeleted").read[Boolean] orElse (Reads.pure(false)))
      ) (DirectorDetails.apply _)

  implicit val writes: Writes[DirectorDetails] = Json.writes[DirectorDetails]


  def applyDelete(firstName: String, middleName: Option[String], lastName: String, dateOfBirth: LocalDate): DirectorDetails = {
    DirectorDetails(firstName, middleName, lastName, dateOfBirth, false)
  }

  def unapplyDelete(directorDetails: DirectorDetails): Option[(String, Option[String], String, LocalDate)] = {
    Some((directorDetails.firstName, directorDetails.middleName, directorDetails.lastName, directorDetails.dateOfBirth))
  }
}
