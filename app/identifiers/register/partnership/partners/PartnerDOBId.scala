/*
 * Copyright 2022 HM Revenue & Customs
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

package identifiers.register.partnership.partners

import java.time.LocalDate

import identifiers._
import models.Index
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersPartner, DateCYA}
import viewmodels.{AnswerRow, Link, Message}

case class PartnerDOBId(index: Int) extends TypedIdentifier[LocalDate] {
  override def path: JsPath = JsPath \ "partners" \ index \ PartnerDOBId.toString
}

object PartnerDOBId {
  override lazy val toString: String = "dateOfBirth"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[PartnerDOBId] =
    new CheckYourAnswersPartner[PartnerDOBId] {
      private def label(ua: UserAnswers, index: Index): String =
        dynamicMessage(ua, messageKey = "dob.heading", index)

      private def hiddenLabel(ua: UserAnswers, index: Index): Message =
        dynamicMessage(ua, messageKey = "dob.visuallyHidden.text", index)


      override def row(id: PartnerDOBId)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        DateCYA[PartnerDOBId](Some(label(userAnswers, id.index)), Some(hiddenLabel(userAnswers, id.index)))().row(id)(changeUrl, userAnswers)
        }
}
