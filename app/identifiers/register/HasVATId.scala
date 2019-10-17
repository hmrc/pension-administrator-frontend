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

package identifiers.register

import identifiers.TypedIdentifier
import play.api.i18n.Messages
import play.api.libs.json.{JsResult, JsSuccess}
import utils.UserAnswers
import utils.checkyouranswers.{BooleanCYA, CheckYourAnswers, CheckYourAnswersCompany}
import viewmodels.{AnswerRow, Link}

case object HasVATId extends TypedIdentifier[Boolean] {
  self =>
  override def toString: String = "hasVat"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[self.type] =
    new CheckYourAnswersCompany[self.type] {
      private def label(ua: UserAnswers): String =
        dynamicMessage(ua, messageKey = "hasVAT.heading")

      private def hiddenLabel(index: Int, ua: UserAnswers): String =
        dynamicMessage(ua, messageKey = "hasVAT.visuallyHidden.text")


      override def row(id: self.type)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        BooleanCYA[self.type](Some(label(userAnswers)))().row(id)(changeUrl, userAnswers)
    }

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers.remove(EnterVATId)
      case _ => JsSuccess(userAnswers)
    }
  }
}
