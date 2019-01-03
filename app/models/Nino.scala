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

sealed trait Nino

object Nino {

  case class Yes(nino: String) extends Nino

  case class No(reason: String) extends Nino

  def options: Seq[InputOption] = Seq(
    InputOption("true", "site.yes", Some("nino_nino-form")),
    InputOption("false", "site.no", Some("nino_reason-form"))
  )

  implicit val reads: Reads[Nino] = {

    (JsPath \ "hasNino").read[Boolean].flatMap {

      case true =>
        (JsPath \ "nino").read[String]
          .map[Nino](Yes.apply)
          .orElse(Reads[Nino](_ => JsError("NINO Value expected")))

      case false =>
        (JsPath \ "reason").read[String]
          .map[Nino](No.apply)
          .orElse(Reads[Nino](_ => JsError("Reason expected")))
    }
  }

  //noinspection ConvertExpressionToSAM
  implicit val writes: Writes[Nino] = new Writes[Nino] {
    override def writes(o: Nino): JsValue = o match {
      case Nino.Yes(nino) => Json.obj("hasNino" -> true, "nino" -> nino)
      case Nino.No(reason) => Json.obj("hasNino" -> false, "reason" -> reason)
    }
  }

}
