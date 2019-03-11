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

package utils

import identifiers.register.company.directors.DirectorAddressId
import identifiers.register.individual.{IndividualAddressId, IndividualContactDetailsId, IndividualDetailsId, IndividualPreviousAddressId}
import models._
import models.Mode._
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, Link}

class CheckYourAnswersHelper(userAnswers: UserAnswers, countryOptions: CountryOptions) {

  def individualContactAddress: Option[AnswerRow] = {
    userAnswers.get(identifiers.register.individual.IndividualContactAddressId) map { answer =>
      AnswerRow("cya.label.individual.contact.address", addressAnswer(answer), false, None)
    }
  }

  def individualSameContactAddress: Option[AnswerRow] = {
    userAnswers.get(identifiers.register.individual.IndividualSameContactAddressId) map { answer =>
      AnswerRow("cya.label.individual.same.contact.address", Seq(if (answer) "site.yes" else "site.no"), true, Some(
        Link(controllers.register.individual.routes.IndividualSameContactAddressController.onPageLoad(CheckMode).url)))
    }
  }

  def individualDateOfBirth: Option[AnswerRow] = userAnswers.get(identifiers.register.individual.IndividualDateOfBirthId) map { x =>
    AnswerRow("cya.label.dob", Seq(s"${DateHelper.formatDate(x)}"), false,
      Link(controllers.register.individual.routes.IndividualDateOfBirthController.onPageLoad(CheckMode).url))
  }

  def individualPhoneNumber: Option[AnswerRow] = {
    userAnswers.get(IndividualContactDetailsId) map { x =>
      AnswerRow("contactDetails.phone.checkYourAnswersLabel", Seq(x.phone), false,
        Link(controllers.register.individual.routes.IndividualContactDetailsController.onPageLoad(CheckMode).url))
    }
  }

  def individualEmailAddress: Option[AnswerRow] = {
    userAnswers.get(IndividualContactDetailsId) map { x =>
      AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq(x.email), false,
        Link(controllers.register.individual.routes.IndividualContactDetailsController.onPageLoad(CheckMode).url))
    }
  }

  def individualPreviousAddress: Option[AnswerRow] = {
    userAnswers.get(IndividualPreviousAddressId) map { x =>
      AnswerRow("individualPreviousAddress.checkYourAnswersLabel", addressAnswer(x), false,
        Link(controllers.register.individual.routes.IndividualPreviousAddressController.onPageLoad(CheckMode).url))
    }
  }

  def individualAddressYears(message: String): Option[AnswerRow] = {
    userAnswers.get(identifiers.register.individual.IndividualAddressYearsId) map { x =>
      AnswerRow(message, Seq(s"common.addressYears.$x"), true,
        Link(controllers.register.individual.routes.IndividualAddressYearsController.onPageLoad(CheckMode).url))
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

  def directorContactDetails(index: Int, mode: Mode): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.directors.DirectorContactDetailsId(index)) match {
    case Some(x) => Seq(
      AnswerRow("contactDetails.email", Seq(s"${x.email}"), false,
        Link(controllers.register.company.directors.routes.DirectorContactDetailsController.onPageLoad(checkMode(mode), index).url)),
      AnswerRow("contactDetails.phone", Seq(s"${x.phone}"), false,
        Link(controllers.register.company.directors.routes.DirectorContactDetailsController.onPageLoad(checkMode(mode), index).url))
    )

    case _ => Nil
  }

  def companyDirectorAddressPostCodeLookup(index: Int): Option[AnswerRow] =
    userAnswers.get(identifiers.register.company.directors.CompanyDirectorAddressPostCodeLookupId(index)) map {
      x =>
        AnswerRow("companyDirectorAddressPostCodeLookup.checkYourAnswersLabel", Seq(s"$x"), false,
          Link(controllers.register.company.directors.routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(CheckMode, index).url))
    }

  def directorPreviousAddressPostCodeLookup(index: Int, mode: Mode): Option[AnswerRow] =
    userAnswers.get(identifiers.register.company.directors.DirectorPreviousAddressPostCodeLookupId(index)) map {
      x =>
        AnswerRow("directorPreviousAddressPostCodeLookup.checkYourAnswersLabel", Seq(s"$x"), false,
          Link(controllers.register.company.directors.routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(checkMode(mode), index).url))
    }

  def directorAddress(index: Int, mode: Mode): Seq[AnswerRow] = userAnswers.get(DirectorAddressId(index)) match {
    case Some(x) => Seq(AnswerRow("cya.label.address", addressAnswer(x), false,
      Link(controllers.register.company.directors.routes.DirectorAddressController.onPageLoad(checkMode(mode), index).url)))
    case _ => Nil
  }

  def directorPreviousAddress(index: Int, mode: Mode): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.directors.DirectorPreviousAddressId(index)) match {
    case Some(x) => Seq(AnswerRow("directorPreviousAddress.checkYourAnswersLabel", addressAnswer(x), false,
      Link(controllers.register.company.directors.routes.DirectorPreviousAddressController.onPageLoad(checkMode(mode), index).url)))
    case _ => Nil
  }

  def directorUniqueTaxReference(index: Int, mode: Mode): Seq[AnswerRow] = userAnswers.get(
    identifiers.register.company.directors.DirectorUniqueTaxReferenceId(index)) match {
    case Some(UniqueTaxReference.Yes(utr)) => Seq(
      AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel", Seq(s"${UniqueTaxReference.Yes}"), true,
        Link(controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(checkMode(mode), index).url)),
      AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel.utr", Seq(utr), true,
        Link(controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(checkMode(mode), index).url))
    )

    case Some(UniqueTaxReference.No(reason)) => Seq(
      AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel", Seq(s"${UniqueTaxReference.No}"), true,
        Link(controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(checkMode(mode), index).url)),
      AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel.reason", Seq(reason), true,
        Link(controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(checkMode(mode), index).url))
    )

    case _ => Nil
  }

  def directorAddressYears(index: Int, mode: Mode): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.directors.DirectorAddressYearsId(index)) match {
    case Some(x) => Seq(AnswerRow("directorAddressYears.checkYourAnswersLabel", Seq(s"common.addressYears.$x"), true,
      Link(controllers.register.company.directors.routes.DirectorAddressYearsController.onPageLoad(checkMode(mode), index).url)))

    case _ => Nil
  }

  def directorDetails(index: Int, mode: Mode): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.directors.DirectorDetailsId(index)) match {
    case Some(x) => Seq(AnswerRow("cya.label.name", Seq(s"${x.firstName} ${x.lastName}"), false,
      Link(controllers.register.company.directors.routes.DirectorDetailsController.onPageLoad(checkMode(mode), index).url)),
      AnswerRow("cya.label.dob", Seq(s"${DateHelper.formatDate(x.dateOfBirth)}"), false,
        Link(controllers.register.company.directors.routes.DirectorDetailsController.onPageLoad(checkMode(mode), index).url)))
    case _ => Nil
  }

  def directorNino(index: Int, mode: Mode): Seq[AnswerRow] = userAnswers.get(identifiers.register.company.directors.DirectorNinoId(index)) match {
    case Some(Nino.Yes(nino)) => Seq(
      AnswerRow("directorNino.checkYourAnswersLabel", Seq(s"${Nino.Yes}"), true,
        Link(controllers.register.company.directors.routes.DirectorNinoController.onPageLoad(checkMode(mode), index).url)),
      AnswerRow("directorNino.checkYourAnswersLabel.nino", Seq(nino), true,
        Link(controllers.register.company.directors.routes.DirectorNinoController.onPageLoad(checkMode(mode), index).url))
    )

    case Some(Nino.No(reason)) => Seq(
      AnswerRow("directorNino.checkYourAnswersLabel", Seq(s"${Nino.No}"), true,
        Link(controllers.register.company.directors.routes.DirectorNinoController.onPageLoad(checkMode(mode), index).url)),
      AnswerRow("directorNino.checkYourAnswersLabel.reason", Seq(reason), true,
        Link(controllers.register.company.directors.routes.DirectorNinoController.onPageLoad(checkMode(mode), index).url))
    )

    case _ => Nil
  }

  def moreThanTenDirectors: Option[AnswerRow] = userAnswers.get(identifiers.register.company.MoreThanTenDirectorsId) map {
    x => AnswerRow("moreThanTenDirectors.checkYourAnswersLabel", Seq(if (x) "site.yes" else "site.no"), true,
      Link(controllers.register.company.routes.MoreThanTenDirectorsController.onPageLoad(CheckMode).url))
  }

  def vatRegistrationNumber: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDetailsId) flatMap { x =>
    x.vatRegistrationNumber map { vatRegNo =>
      AnswerRow(
        "companyDetails.vatRegistrationNumber.checkYourAnswersLabel",
        Seq(vatRegNo),
        false,
        Link(controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url)
      )
    }
  }

  def payeEmployerReferenceNumber: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDetailsId) flatMap { x =>
    x.payeEmployerReferenceNumber map { payeRefNo =>
      AnswerRow(
        "companyDetails.payeEmployerReferenceNumber.checkYourAnswersLabel",
        Seq(payeRefNo),
        false,
        Link(controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url)
      )
    }
  }

  def companyRegistrationNumber: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyRegistrationNumberId) map {
    x => AnswerRow("companyRegistrationNumber.checkYourAnswersLabel", Seq(s"$x"), false,
      Link(controllers.register.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url))
  }

  private def addressAnswer(address: Address): Seq[String] = {
    address.lines(countryOptions)
  }

}
