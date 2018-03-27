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
import models.Nino
import models.Nino.{No, Yes}
import play.api.libs.json.{JsPath, Reads}
import utils.{CheckYourAnswers, UserAnswers}
import viewmodels.AnswerRow

case class DirectorNinoId(index: Int) extends TypedIdentifier[Nino] {
  override def path: JsPath = JsPath \ "directors" \ index \ DirectorNinoId.toString
}

object DirectorNinoId {
  override lazy val toString: String = "directorNino"

  implicit def checkYourAnswers[I <: TypedIdentifier[Nino]](implicit rds: Reads[String]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          case _@Yes(nino) => Seq(
            AnswerRow(
              "directorNino.checkYourAnswersLabel",
              Seq(s"${Nino.Yes}"),
              true,
              changeUrl
            ),
            AnswerRow(
              "directorNino.checkYourAnswersLabel.nino", Seq(nino),
              true,
              changeUrl
            )
          )
          case _@No(reason) => Seq(
            AnswerRow(
              "directorNino.checkYourAnswersLabel", Seq(s"${Nino.No}"),
              true,
              changeUrl
            ),
            AnswerRow(
              "directorNino.checkYourAnswersLabel.reason", Seq(reason),
              true,
              changeUrl
            )
          )
        }.getOrElse(Seq.empty)
    }
}


