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

sealed trait Vat

object Vat {

  case class Yes(vat: String) extends Vat

  case object No extends Vat

  def options: Seq[InputOption] = Seq(
    InputOption("true", "site.yes", Some("vat_vat-form")),
    InputOption("false", "site.no", None)
  )

  implicit val reads: Reads[Vat] = {

    (JsPath \ "hasVat").read[Boolean].flatMap {

      case true =>
        (JsPath \ "vat").read[String]
          .map[Vat](Yes.apply)
          .orElse(Reads[Vat](_ => JsError("Vat Value expected")))

      case false => Reads[Vat](_ => JsSuccess(No))
    }
  }

  //noinspection ConvertExpressionToSAM
  implicit val writes: Writes[Vat] = new Writes[Vat] {
    override def writes(o: Vat): JsValue = o match {
      case Vat.Yes(vat) => Json.obj("hasVat" -> true, "vat" -> vat)
      case Vat.No => Json.obj("hasVat" -> false)
    }
  }

}
