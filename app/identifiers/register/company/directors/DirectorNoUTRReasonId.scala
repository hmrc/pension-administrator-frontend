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

package identifiers.register.company.directors

import identifiers._
import models.Index
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersDirector, StringCYA}
import viewmodels.{AnswerRow, Link, Message}

case class DirectorNoUTRReasonId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = JsPath \ "directors" \ index \ DirectorNoUTRReasonId.toString
}
object DirectorNoUTRReasonId {
  override lazy val toString: String = "noUtrReason"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[DirectorNoUTRReasonId] =
    new CheckYourAnswersDirector[DirectorNoUTRReasonId] {
      private def label(ua: UserAnswers, index: Index): String =
        dynamicMessage(ua, messageKey = "whyNoUTR.heading", index)

      private def hiddenLabel(ua: UserAnswers, index: Index): Message =
        dynamicMessage(ua, messageKey = "whyNoUTR.visuallyHidden.text", index)


      override def row(id: DirectorNoUTRReasonId)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(HasDirectorUTRId(id.index)) match {
          case Some(false) =>
            StringCYA[DirectorNoUTRReasonId](Some(label(userAnswers, id.index)), Some(hiddenLabel(userAnswers, id.index)))().
              row(id)(changeUrl, userAnswers)
          case _ =>
            StringCYA[DirectorNoUTRReasonId](Some(label(userAnswers, id.index)), Some(hiddenLabel(userAnswers, id.index)), isMandatory = false)().
              row(id)(changeUrl, userAnswers)
        }
    }
}






