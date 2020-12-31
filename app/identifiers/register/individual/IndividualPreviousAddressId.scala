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

import identifiers.TypedIdentifier
import models.{Address, AddressYears}
import utils.checkyouranswers.CheckYourAnswers
import utils.countryOptions.CountryOptions
import utils.{UserAnswers, checkyouranswers}
import viewmodels.{AnswerRow, Link}

case object IndividualPreviousAddressId extends TypedIdentifier[Address] {
  self =>
  override def toString: String = "individualPreviousAddress"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[self.type] =
    new CheckYourAnswers[self.type] {
      override def row(id: self.type)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
        (userAnswers.get(IndividualAddressYearsId), userAnswers.get(IndividualPreviousAddressId)) match {
          case (Some(AddressYears.UnderAYear), None) =>
            checkyouranswers.AddressCYA[self.type](label = "individualPreviousAddress.checkYourAnswersLabel",
              Some("individualPreviousAddress.visuallyHidden.text"))().row(id)(changeUrl, userAnswers)
          case _ =>
            checkyouranswers.AddressCYA[self.type](label = "individualPreviousAddress.checkYourAnswersLabel",
              Some("individualPreviousAddress.visuallyHidden.text"), isMandatory = false)().row(id)(changeUrl, userAnswers)
        }
      }
    }
}
