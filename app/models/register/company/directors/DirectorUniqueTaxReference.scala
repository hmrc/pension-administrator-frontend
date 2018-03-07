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

import play.api.libs.json._
import utils.InputOption

sealed trait DirectorUniqueTaxReference

object DirectorUniqueTaxReference {

  case class Yes(utr: String) extends DirectorUniqueTaxReference
  case class No(reason: String) extends DirectorUniqueTaxReference

  val options: Seq[InputOption] = Seq(
      InputOption("true", "site.yes", Some("directorUtr_utr-form")),
      InputOption("false", "site.no", Some("directorUtr_reason-form"))
  )

  implicit val reads: Reads[DirectorUniqueTaxReference] = {
    (JsPath \ "hasUtr").read[Boolean].flatMap {
      case true =>
        (JsPath \ "utr").read[String]
          .map[DirectorUniqueTaxReference](Yes.apply)
          .orElse(Reads[DirectorUniqueTaxReference](_ => JsError("Director UTR expected")))
      case false =>
        (JsPath \ "reason").read[String]
          .map[DirectorUniqueTaxReference](No.apply)
          .orElse(Reads[DirectorUniqueTaxReference](_ => JsError("Reason expected")))
    }
  }

  implicit lazy val writes = new Writes[DirectorUniqueTaxReference] {
    def writes(directorUtr: DirectorUniqueTaxReference) = {
      directorUtr match {
        case DirectorUniqueTaxReference.Yes(utr) =>
          Json.obj("hasUtr" -> true, "utr" -> utr)
        case DirectorUniqueTaxReference.No(reason) =>
          Json.obj("hasUtr" -> false, "reason" -> reason)
      }
    }
  }
}