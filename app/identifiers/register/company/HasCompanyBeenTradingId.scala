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

package identifiers.register.company

import identifiers.TypedIdentifier
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{BooleanCYA, CheckYourAnswers, CheckYourAnswersCompany}
import viewmodels.{AnswerRow, Link, Message}

case object HasCompanyBeenTradingId extends TypedIdentifier[Boolean] {
  self =>
  override def path: JsPath = JsPath \ HasCompanyBeenTradingId.toString

  override def toString: String = "hasCompanyBeenTrading"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[self.type] =
    new CheckYourAnswersCompany[self.type] {
      private def label(ua: UserAnswers): String =
        dynamicMessage(ua, "trading.title")

      private def hiddenLabel(ua: UserAnswers): Message =
        dynamicMessage(ua, "trading.visuallyHidden.text")

      override def row(id: self.type)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        BooleanCYA[self.type](Some(label(userAnswers)), Some(hiddenLabel(userAnswers)))().row(id)(changeUrl, userAnswers)
    }

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers
          .remove(CompanyPreviousAddressPostCodeLookupId)
          .flatMap(_.remove(CompanyPreviousAddressId))
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}
