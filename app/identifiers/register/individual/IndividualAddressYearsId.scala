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

package identifiers.register.individual

import identifiers._
import models.AddressYears
import play.api.i18n.Messages
import play.api.libs.json.JsResult
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers}
import viewmodels.{AnswerRow, Link}

case object IndividualAddressYearsId extends TypedIdentifier[AddressYears] {
  self =>
  override def toString: String = "individualAddressYears"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[self.type] = {
    new CheckYourAnswers[self.type] {
      override def row(id: self.type)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(IndividualContactAddressId) match {
          case Some(_) =>
            AddressYearsCYA("individualAddressYears.title", Some("individualAddressYears.visuallyHidden.text"))().
              row(id)(changeUrl, userAnswers)
          case _ =>
            AddressYearsCYA("individualAddressYears.title", Some("individualAddressYears.visuallyHidden.text"), isMandatory = false)().
              row(id)(changeUrl, userAnswers)
        }
    }
  }

  override def cleanup(value: Option[AddressYears], answers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        answers.set(IndividualPreviousAddressChangedId)(true).asOpt.getOrElse(answers)
          .removeAllOf(List(IndividualPreviousAddressPostCodeLookupId, IndividualPreviousAddressListId, IndividualPreviousAddressId))
      case _ => super.cleanup(value, answers)
    }
  }
}
