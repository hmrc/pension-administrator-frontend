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

package utils.checkyouranswers

import identifiers.TypedIdentifier
import models.requests.DataRequest
import play.api.libs.json.Reads
import play.api.mvc.AnyContent
import viewmodels.{AnswerRow, Link}

import scala.language.implicitConversions

trait Ops[A] {
  def row(changeUrl: Option[Link])(implicit request: DataRequest[AnyContent], reads: Reads[A]): Seq[AnswerRow]
}

object Ops {
  implicit def toOps[I <: TypedIdentifier.PathDependent](id: I)(implicit ev: CheckYourAnswers[I]): Ops[id.Data] =
    new Ops[id.Data] {
      override def row(changeUrl: Option[Link])(implicit request: DataRequest[AnyContent], reads: Reads[id.Data]): Seq[AnswerRow] = {
        ev.row(id)(changeUrl, request.userAnswers)
      }
    }
}
