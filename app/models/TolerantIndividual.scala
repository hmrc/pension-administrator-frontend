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

import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath}

case class TolerantIndividual(firstName: Option[String], middleName: Option[String], lastName: Option[String]) {
  def fullName: String = Seq(firstName, middleName, lastName).flatten(s => s).mkString(" ")
}

object TolerantIndividual {

  implicit lazy val formatsTolerantIndividual: Format[TolerantIndividual] = (
    (JsPath \ "firstName").formatNullable[String] and
      (JsPath \ "middleName").formatNullable[String] and
      (JsPath \ "lastName").formatNullable[String]
    ) (TolerantIndividual.apply, unlift(TolerantIndividual.unapply))

  def applyNonUK(firstName: String, lastName: String): TolerantIndividual = {
    new TolerantIndividual(Some(firstName), None, Some(lastName))
  }

  def unapplyNonUK(individual: TolerantIndividual): Option[(String, String)] = {
    Some((individual.firstName.getOrElse(""), individual.lastName.getOrElse("")))
  }

}
