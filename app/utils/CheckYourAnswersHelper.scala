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

package utils

import identifiers.register.individual.{IndividualAddressId, IndividualDetailsId, IndividualPreviousAddressId}
import models._
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, Link, Message}

class CheckYourAnswersHelper(userAnswers: UserAnswers, countryOptions: CountryOptions) {

  def individualContactAddress: Option[AnswerRow] = {
    userAnswers.get(identifiers.register.individual.IndividualContactAddressId) map { answer =>
      AnswerRow("cya.label.individual.contact.address", addressAnswer(answer), false, None)
    }
  }

  def individualSameContactAddress: Option[AnswerRow] = {
    userAnswers.get(identifiers.register.individual.IndividualSameContactAddressId) map { answer =>
      AnswerRow("cya.label.individual.same.contact.address", Seq(if (answer) "site.yes" else "site.no"), true, Some(
        Link(controllers.register.individual.routes.IndividualSameContactAddressController.onPageLoad(CheckMode).url)), Some(Message("individualContactAddress.visuallyHidden.text")))
    }
  }

  def individualDateOfBirth: Option[AnswerRow] = userAnswers.get(identifiers.register.individual.IndividualDateOfBirthId) map { x =>
    AnswerRow("individualDateOfBirth.heading", Seq(s"${DateHelper.formatDate(x)}"), false,
      Some(Link(controllers.register.individual.routes.IndividualDateOfBirthController.onPageLoad(CheckMode).url)), Some(Message("individualDateOfBirth.visuallyHidden.text")))
  }

  def individualEmail: Option[AnswerRow] = userAnswers.get(identifiers.register.individual.IndividualEmailId) map { x =>
    AnswerRow("individual.email.title", Seq(x), false,
      Some(Link(controllers.register.individual.routes.IndividualEmailController.onPageLoad(CheckMode).url)), Some(Message("individualEmail.visuallyHidden.text")))
  }

  def individualPhone: Option[AnswerRow] = userAnswers.get(identifiers.register.individual.IndividualPhoneId) map { x =>
    AnswerRow("individual.phone.title", Seq(x), false,
      Some(Link(controllers.register.individual.routes.IndividualPhoneController.onPageLoad(CheckMode).url)), Some(Message("individualPhone.visuallyHidden.text")))
  }

  def individualPreviousAddress: Option[AnswerRow] = {
    userAnswers.get(IndividualPreviousAddressId) map { x =>
      AnswerRow("individualPreviousAddress.checkYourAnswersLabel", addressAnswer(x), false,
        Some(Link(controllers.register.individual.routes.IndividualPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url)),
        Some(Message("individualPreviousAddress.visuallyHidden.text")))
    }
  }

  def individualAddressYears(message: String): Option[AnswerRow] = {
    userAnswers.get(identifiers.register.individual.IndividualAddressYearsId) map { x =>
      AnswerRow(message, Seq(s"common.addressYears.$x"), true,
        Some(Link(controllers.register.individual.routes.IndividualAddressYearsController.onPageLoad(CheckMode).url)), Some(Message("individualAddressYears.visuallyHidden.text")))
    }
  }

  def individualDetails: Option[AnswerRow] = {
    userAnswers.get(IndividualDetailsId) map { x =>
      AnswerRow("individualDetailsCorrect.name", Seq(s"${x.fullName}"), false, None)
    }
  }

  def individualAddress: Option[AnswerRow] = {
    userAnswers.get(IndividualAddressId) map { x =>
      AnswerRow("individualDetailsCorrect.address", x.lines(countryOptions), false, None)
    }
  }

  def moreThanTenDirectors: Option[AnswerRow] = userAnswers.get(identifiers.register.company.MoreThanTenDirectorsId) map {
    x => AnswerRow("moreThanTenDirectors.checkYourAnswersLabel", Seq(if (x) "site.yes" else "site.no"), true,
      Some(Link(controllers.register.company.routes.MoreThanTenDirectorsController.onPageLoad(CheckMode).url)), None)
  }

  private def addressAnswer(address: Address): Seq[String] = {
    address.lines(countryOptions)
  }

}
