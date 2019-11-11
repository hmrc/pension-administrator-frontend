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

package identifiers.register.partnership.partners

import identifiers._
import models.Index
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersPartner, StringCYA}
import viewmodels.{AnswerRow, Link, Message}

case class PartnerNoUTRReasonId(index: Int) extends TypedIdentifier[String] {
  override def path: JsPath = JsPath \ "partners" \ index \ PartnerNoUTRReasonId.toString
}
object PartnerNoUTRReasonId {
  override lazy val toString: String = "noUtrReason"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[PartnerNoUTRReasonId] =
    new CheckYourAnswersPartner[PartnerNoUTRReasonId] {
      private def label(ua: UserAnswers, index: Index): String =
        dynamicMessage(ua, messageKey = "whyNoUTR.heading", index)

      private def hiddenLabel(ua: UserAnswers, index: Index): Message =
        dynamicMessage(ua, messageKey = "whyNoUTR.visuallyHidden.text", index)


      override def row(id: PartnerNoUTRReasonId)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA[PartnerNoUTRReasonId](Some(label(userAnswers, id.index)), Some(hiddenLabel(userAnswers, id.index)))().row(id)(changeUrl, userAnswers)
    }
}







