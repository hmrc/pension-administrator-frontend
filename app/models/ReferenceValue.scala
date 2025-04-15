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
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class ReferenceValue(value: String, isEditable: Boolean = false)

object ReferenceValue {
  implicit val reads: Reads[ReferenceValue] =
    ((JsPath \ "value").read[String] and
      ((JsPath \ "isEditable").read[Boolean] orElse Reads.pure(false))
      ) (ReferenceValue.apply)

  implicit val writes: Writes[ReferenceValue] = Json.writes[ReferenceValue]

  def applyEditable(value: String): ReferenceValue = {
    ReferenceValue(value)
  }

  def unapplyEditable(reference: ReferenceValue): Option[String] = {
    Some(reference.value)
  }
}


