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

package identifiers.register.partnership.partners

import identifiers.TypedIdentifier
import models.Index
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult, JsSuccess}
import utils.UserAnswers
import utils.checkyouranswers.{BooleanCYA, CheckYourAnswers, CheckYourAnswersPartner}
import viewmodels.{AnswerRow, Link, Message}

case class HasPartnerNINOId(index: Int) extends TypedIdentifier[Boolean] {
  self =>
  override def path: JsPath = JsPath \ "partners" \ index \ HasPartnerNINOId.toString

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers.remove(PartnerEnterNINOId(index))
      case Some(true) =>
        userAnswers.remove(PartnerNoNINOReasonId(index))
      case _ => JsSuccess(userAnswers)
    }
  }
}
object HasPartnerNINOId {
  override def toString: String = "hasNino"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[HasPartnerNINOId] =
    new CheckYourAnswersPartner[HasPartnerNINOId] {
      private def label(ua: UserAnswers, index: Index): String =
        dynamicMessage(ua, messageKey = "hasNINO.heading", index)

      private def hiddenLabel(ua: UserAnswers, index: Index): Message =
        dynamicMessage(ua, messageKey = "hasNINO.visuallyHidden.text", index)


      override def row(id: HasPartnerNINOId)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        BooleanCYA[HasPartnerNINOId](Some(label(userAnswers, id.index)), Some(hiddenLabel(userAnswers, id.index)))().row(id)(changeUrl, userAnswers)
    }
}


