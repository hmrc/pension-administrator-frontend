/*
 * Copyright 2023 HM Revenue & Customs
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
import models.Address
import utils.checkyouranswers.CheckYourAnswers
import utils.countryOptions.CountryOptions
import utils.{UserAnswers, checkyouranswers}
import viewmodels.{AnswerRow, Link}

case object IndividualContactAddressId extends TypedIdentifier[Address] {
  self =>
  override def toString: String = "individualContactAddress"

  implicit def cya(implicit countryOptions: CountryOptions): CheckYourAnswers[self.type] =
    new CheckYourAnswers[self.type] {
      override def row(id: self.type)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id) match {
          case Some(_) =>
            val optionChangeUrl = userAnswers.get(IndividualSameContactAddressId) match {
              case Some(false) => changeUrl
              case _ => None
            }
            checkyouranswers.AddressCYA[self.type](label = "cya.label.individual.contact.address", None)().row(id)(optionChangeUrl, userAnswers)
          case _ =>
            checkyouranswers.AddressCYA[self.type](label = "cya.label.individual.contact.address", None)().row(id)(changeUrl, userAnswers)
        }

      }
    }

}
