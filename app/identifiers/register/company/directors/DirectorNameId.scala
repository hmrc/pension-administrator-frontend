/*
 * Copyright 2021 HM Revenue & Customs
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
import identifiers.register.company.MoreThanTenDirectorsId
import models.PersonName
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, PersonNameCYA}
import viewmodels.{AnswerRow, Link, Message}

case class DirectorNameId(index: Int) extends TypedIdentifier[PersonName] {
  override def path: JsPath = JsPath \ "directors" \ index \ DirectorNameId.toString

  override def cleanup(value: Option[PersonName], userAnswers: UserAnswers): JsResult[UserAnswers] =
    value match {
      case Some(PersonName(_, _, true, _)) => userAnswers.remove(MoreThanTenDirectorsId)
      case _ => super.cleanup(value, userAnswers)
    }
}

object DirectorNameId {

  def collectionPath: JsPath = JsPath \ "directors" \\ DirectorNameId.toString

  override def toString: String = "directorDetails"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[DirectorNameId] =
    new CheckYourAnswers[DirectorNameId] {

      override def row(id: DirectorNameId)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        PersonNameCYA[DirectorNameId](
          Some(Message("directorName.cya.label")),
          Some(Message("directorName.visuallyHidden.text")))().row(id)(changeUrl, userAnswers)
    }

}

