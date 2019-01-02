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

sealed trait Paye

object Paye {

  case class Yes(paye: String) extends Paye

  case object No extends Paye

  def options: Seq[InputOption] = Seq(
    InputOption("true", "site.yes", Some("paye_paye-form")),
    InputOption("false", "site.no")
  )

  implicit val reads: Reads[Paye] = {
    (JsPath \ "hasPaye").read[Boolean].flatMap {
      case true =>
        (JsPath \ "paye").read[String]
          .map[Paye](Yes.apply)
          .orElse(Reads[Paye](_ => JsError("Paye Value expected")))

      case false =>
        Reads(_ => JsSuccess(No))
    }
  }

  //noinspection ConvertExpressionToSAM
  implicit val writes: Writes[Paye] = new Writes[Paye] {
    override def writes(o: Paye): JsValue = o match {
      case Paye.Yes(paye) => Json.obj("hasPaye" -> true, "paye" -> paye)
      case Paye.No => Json.obj("hasPaye" -> false)
    }
  }
}


