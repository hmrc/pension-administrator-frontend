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

import identifiers._
import models.{Index, ReferenceValue}
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersPartner, ReferenceValueCYA}
import viewmodels.{AnswerRow, Link, Message}

case class PartnerEnterUTRId(index: Int) extends TypedIdentifier[ReferenceValue] {
  override def path: JsPath = JsPath \ "partners" \ index \ PartnerEnterUTRId.toString

  override def cleanup(value: Option[ReferenceValue], userAnswers: UserAnswers): JsResult[UserAnswers] =
    userAnswers.remove(PartnerNoUTRReasonId(this.index))
}

object PartnerEnterUTRId {
  override lazy val toString: String = "utr"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[PartnerEnterUTRId] =
    new CheckYourAnswersPartner[PartnerEnterUTRId] {
      private def label(ua: UserAnswers, index: Index): String =
        dynamicMessage(ua, messageKey = "enterUTR.heading", index)

      private def hiddenLabel(ua: UserAnswers, index: Index): Message =
        dynamicMessage(ua, messageKey = "enterUTR.visuallyHidden.text", index)


      override def row(id: PartnerEnterUTRId)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(HasPartnerUTRId(id.index)) match {
          case Some(true) =>
            ReferenceValueCYA[PartnerEnterUTRId](Some(label(userAnswers, id.index)),
              Some(hiddenLabel(userAnswers, id.index)))().row(id)(changeUrl, userAnswers)
          case _ =>
            ReferenceValueCYA[PartnerEnterUTRId](Some(label(userAnswers, id.index)),
              Some(hiddenLabel(userAnswers, id.index)), isMandatory = false)().row(id)(changeUrl, userAnswers)
        }
    }
}






