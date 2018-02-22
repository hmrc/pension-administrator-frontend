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

package models.register.company

import play.api.libs.json._
import utils.InputOption

sealed trait DirectorNino

object DirectorNino {

  case class Yes(nino: String) extends DirectorNino
  case class No(reason: String) extends DirectorNino

  def options: Seq[InputOption] = Seq(
    InputOption("true", "site.yes", Some("directorNino_nino-form")),
    InputOption("false", "site.no", Some("directorNino_reason-form"))
  )

  implicit val reads: Reads[DirectorNino] = {

    (JsPath \ "hasNino").read[Boolean].flatMap {

      case true =>
        (JsPath \ "nino").read[String]
          .map[DirectorNino](Yes.apply)
          .orElse(Reads[DirectorNino](_ => JsError("NINO Value expected")))

      case false =>
        (JsPath \ "reason").read[String]
          .map[DirectorNino](No.apply)
          .orElse(Reads[DirectorNino](_ => JsError("Reason expected")))
    }
  }

  implicit lazy val writes = new Writes[DirectorNino] {
    def writes(o: DirectorNino) = {
      o match {
        case DirectorNino.Yes(nino) =>
          Json.obj("hasNino" -> true, "nino" -> nino)
        case DirectorNino.No(reason) =>
          Json.obj("hasNino" -> false, "reason" -> reason)
      }
    }
  }
}