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

package identifiers.register.company

import identifiers._
import models.AddressYears
import play.api.i18n.Messages
import play.api.libs.json.JsResult
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers, CheckYourAnswersBusiness}
import viewmodels.{AnswerRow, Link, Message}

case object CompanyAddressYearsId extends TypedIdentifier[AddressYears] {
  self =>
  override def toString: String = "companyAddressYears"

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers.set(CompanyPreviousAddressChangedId)(true).asOpt.getOrElse(userAnswers)
          .removeAllOf(List(CompanyPreviousAddressPostCodeLookupId, CompanyPreviousAddressId, CompanyTradingOverAYearId))
      case _ => super.cleanup(value, userAnswers)
    }
  }

  implicit def cya(implicit messages: Messages): CheckYourAnswers[self.type] =
    new CheckYourAnswersBusiness[self.type] {
      private def label(ua: UserAnswers): String =
        dynamicMessage(ua, "addressYears.heading")

      private def hiddenLabel(ua: UserAnswers): Message =
        dynamicMessage(ua, "addressYears.visuallyHidden.text")

      override def row(id: self.type)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(CompanyContactAddressId) match {
          case Some(_) =>
              AddressYearsCYA[self.type](label(userAnswers), Some(hiddenLabel(userAnswers)))().row(id)(changeUrl, userAnswers)
          case _ =>
            AddressYearsCYA[self.type](label(userAnswers), Some(hiddenLabel(userAnswers)), isMandatory = false)().row(id)(changeUrl, userAnswers)
        }
      }
    }
}
