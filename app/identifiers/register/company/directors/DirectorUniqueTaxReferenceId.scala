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

package identifiers.register.company.directors

import identifiers._
import models.register.company.directors.DirectorUniqueTaxReference
import play.api.libs.json.{JsPath, Reads}
import utils.{CheckYourAnswers, UserAnswers}
import viewmodels.AnswerRow

case class DirectorUniqueTaxReferenceId(index: Int) extends TypedIdentifier[DirectorUniqueTaxReference] {
  override def path: JsPath = JsPath \ "directors" \ index \ DirectorUniqueTaxReferenceId.toString
}

object DirectorUniqueTaxReferenceId {
  override lazy val toString: String = "directorUtr"

  implicit def checkYourAnswers[I <: TypedIdentifier[DirectorUniqueTaxReference]](implicit rds: Reads[String]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          case _@DirectorUniqueTaxReference.Yes(utr) => Seq(
            AnswerRow(
              "directorUniqueTaxReference.checkYourAnswersLabel",
              Seq(s"${DirectorUniqueTaxReference.Yes}"),
              true,
              changeUrl
            ),
            AnswerRow(
              "directorUniqueTaxReference.checkYourAnswersLabel.utr",
              Seq(utr),
              true,
              changeUrl
            )
          )
          case _@DirectorUniqueTaxReference.No(reason) => Seq(
            AnswerRow(
              "directorUniqueTaxReference.checkYourAnswersLabel",
              Seq(s"${DirectorUniqueTaxReference.No}"),
              true,
              changeUrl
            ),
            AnswerRow(
              "directorUniqueTaxReference.checkYourAnswersLabel.reason",
              Seq(reason),
              true,
              changeUrl
            )
          )
        }.getOrElse(Seq.empty)
    }

}
