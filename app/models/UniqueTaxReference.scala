/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.libs.json._
import utils.InputOption

sealed trait UniqueTaxReference

object UniqueTaxReference {

  case class Yes(utr: String) extends UniqueTaxReference

  case class No(reason: String) extends UniqueTaxReference

  val options: Seq[InputOption] = Seq(
    InputOption("true", "site.yes", Some("utr_utr-form")),
    InputOption("false", "site.no", Some("utr_reason-form"))
  )

  implicit val reads: Reads[UniqueTaxReference] = {
    (JsPath \ "hasUtr").read[Boolean].flatMap {
      case true =>
        (JsPath \ "utr").read[String]
          .map[UniqueTaxReference](Yes.apply)
          .orElse(Reads[UniqueTaxReference](_ => JsError("UTR expected")))
      case false =>
        (JsPath \ "reason").read[String]
          .map[UniqueTaxReference](No.apply)
          .orElse(Reads[UniqueTaxReference](_ => JsError("Reason expected")))
    }
  }

  //noinspection ConvertExpressionToSAM
  implicit lazy val writes: Writes[UniqueTaxReference] = new Writes[UniqueTaxReference] {
    def writes(directorUtr: UniqueTaxReference): JsObject = {
      directorUtr match {
        case UniqueTaxReference.Yes(utr) =>
          Json.obj("hasUtr" -> true, "utr" -> utr)
        case UniqueTaxReference.No(reason) =>
          Json.obj("hasUtr" -> false, "reason" -> reason)
      }
    }
  }

}
