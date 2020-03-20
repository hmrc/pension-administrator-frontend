/*
 * Copyright 2020 HM Revenue & Customs
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
import identifiers.register.{AreYouInUKId, BusinessTypeId}
import models.register.BusinessType
import play.api.i18n.Messages
import play.api.libs.json.{JsResult, JsSuccess}
import utils.UserAnswers
import utils.checkyouranswers.{BooleanCYA, CheckYourAnswers, CheckYourAnswersBusiness}
import viewmodels.{AnswerRow, Link, Message}


case object HasCompanyCRNId extends TypedIdentifier[Boolean] {
  self =>
  override def toString: String = "hasCrn"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[self.type] =
    new CheckYourAnswersBusiness[self.type] {
      private def label(ua: UserAnswers): String =
        dynamicMessage(ua, messageKey = "hasCompanyNumber.heading")

      private def hiddenLabel(ua: UserAnswers): Message =
        dynamicMessage(ua, messageKey = "hasCompanyNumber.visuallyHidden.text")

      override def row(id: self.type)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
        (userAnswers.get(AreYouInUKId), userAnswers.get(BusinessTypeId)) match {
          case (Some(false), _) =>
            Nil
          case (Some(true), Some(BusinessType.UnlimitedCompany)) =>
            BooleanCYA[self.type](Some(label(userAnswers)), Some(hiddenLabel(userAnswers)))().row(id)(changeUrl, userAnswers)
          case _ =>
            BooleanCYA[self.type](Some(label(userAnswers)), Some(hiddenLabel(userAnswers)), isMandatory = false)().row(id)(changeUrl, userAnswers)
        }
      }
    }

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers.removeAllOf(List(CompanyRegistrationNumberId))
      case _ => JsSuccess(userAnswers)
    }
  }
}
