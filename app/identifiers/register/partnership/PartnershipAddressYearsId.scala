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

package identifiers.register.partnership

import identifiers.TypedIdentifier
import models.AddressYears
import models.AddressYears.OverAYear
import play.api.i18n.Messages
import play.api.libs.json.JsResult
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers, CheckYourAnswersPartnership}
import viewmodels.{AnswerRow, Link, Message}

case object PartnershipAddressYearsId extends TypedIdentifier[AddressYears] {
  self =>
  override lazy val toString: String = "partnershipAddressYears"

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = value match {
    case Some(OverAYear) =>
      userAnswers.set(PartnershipPreviousAddressChangedId)(value = true).asOpt.getOrElse(userAnswers)
        .removeAllOf(List(PartnershipPreviousAddressPostCodeLookupId, PartnershipPreviousAddressListId,
          PartnershipPreviousAddressId, PartnershipTradingOverAYearId))
    case _ => super.cleanup(value, userAnswers)
  }

  implicit def cya(implicit messages: Messages): CheckYourAnswers[self.type] =
    new CheckYourAnswersPartnership[self.type] {
      private def label(ua: UserAnswers): String =
        dynamicMessage(ua, messageKey = "addressYears.heading")

      private def hiddenLabel(ua: UserAnswers): Message =
        dynamicMessage(ua, messageKey = "addressYears.visuallyHidden.text")

      override def row(id: self.type)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(PartnershipContactAddressId) match {
          case Some(_) =>
            AddressYearsCYA[self.type](label(userAnswers), Some(hiddenLabel(userAnswers)))().row(id)(changeUrl, userAnswers)
          case _ =>
            AddressYearsCYA[self.type](label(userAnswers), Some(hiddenLabel(userAnswers)), isMandatory = false)().row(id)(changeUrl, userAnswers)
        }

      }
    }
}
