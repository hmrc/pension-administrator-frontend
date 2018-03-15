/*
 * Copyright 2018 HM Revenue & Customs
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

import identifiers.TypedIdentifier
import identifiers.register.company.directors._
import identifiers.register.company._
import models.register.company.{CompanyDetails, ContactDetails}
import models.register.company.directors.DirectorNino.{No, Yes}
import models.register.company.directors.{DirectorNino, DirectorUniqueTaxReference}
import models.{Address, AddressYears, CheckMode}
import play.api.libs.json.Reads
import viewmodels.AnswerRow

import scala.language.implicitConversions

class CheckYourAnswersHelper(userAnswers: UserAnswers, countryOptions: CountryOptions) {

  trait CYA[A] {
    def row(changeUrl: String)(implicit reads: Reads[A]): Seq[AnswerRow]
  }

  def cya[A](id: TypedIdentifier[A])(fn: A => Seq[AnswerRow])(implicit reads: Reads[A]): Seq[AnswerRow] = {
    userAnswers.get(id).fold(Seq.empty[AnswerRow])(fn)
  }

  implicit def rowString(id: TypedIdentifier[String]): CYA[String] = {
    new CYA[String] {
      override def row(changeUrl: String)(implicit reads: Reads[String]): Seq[AnswerRow] = {
        cya(id){ x =>
          Seq(AnswerRow(
            s"${id.toString}.checkYourAnswersLabel",
            Seq(s"$x"),
            false,
            changeUrl
          ))
        }
      }
    }
  }

  implicit def rowAddress(id: TypedIdentifier[Address]): CYA[Address] = {
    new CYA[Address] {
      override def row(changeUrl: String)(implicit reads: Reads[Address]): Seq[AnswerRow] = {
        cya(id){ x =>
          Seq(AnswerRow(
            s"${id.toString}.checkYourAnswersLabel",
            addressAnswer(x),
            false,
            changeUrl
          ))
        }
      }
    }
  }

  implicit def rowAddressYears(id: TypedIdentifier[AddressYears]): CYA[AddressYears] = {
    new CYA[AddressYears] {
      override def row(changeUrl: String)(implicit reads: Reads[AddressYears]): Seq[AnswerRow] = {
        cya(id){ x =>
          Seq(AnswerRow(
            s"${id.toString}.checkYourAnswersLabel",
            Seq(s"common.addressYears.$x"),
            true,
            controllers.register.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode).url
          ))
        }
      }
    }
  }

  def companyUniqueTaxReference: Seq[AnswerRow] =
    CompanyUniqueTaxReferenceId.row(
      controllers.register.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode).url
    )

  def companyRegistrationNumber: Seq[AnswerRow] =
    CompanyRegistrationNumberId.row(
      controllers.register.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url
    )

  def companyAddressYears: Seq[AnswerRow] =
    CompanyAddressYearsId.row(
      controllers.register.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode).url
    )

  def directorAddressYears(index: Int): Seq[AnswerRow] =
    DirectorAddressYearsId(index).row(
      controllers.register.company.directors.routes.DirectorAddressYearsController.onPageLoad(CheckMode, index).url
    )

  def directorAddress(index: Int): Seq[AnswerRow] =
    DirectorAddressId(index).row(
      controllers.register.company.directors.routes.DirectorAddressController.onPageLoad(CheckMode, index).url
    )

  def companyPreviousAddress: Seq[AnswerRow] =
    CompanyPreviousAddressId.row(
      controllers.register.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode).url
    )

  def directorPreviousAddress(index: Int): Seq[AnswerRow] =
    DirectorPreviousAddressId(index).row(
      controllers.register.company.directors.routes.DirectorPreviousAddressController.onPageLoad(CheckMode, index).url
    )

  def companyAddress: Seq[AnswerRow] =
    cya(identifiers.register.company.CompanyAddressId) { x =>
      x.toAddress match {
        case Some(address) =>
          Seq(AnswerRow(
            "cya.label.address",
            addressAnswer(address),
            false,
            controllers.register.company.routes.CompanyAddressController.onPageLoad().url
          ))
        case _ => Seq.empty[AnswerRow]
      }
    }

  def directorDetails(index: Int): Seq[AnswerRow] =
    cya(DirectorDetailsId(index)){ x =>
      Seq(
        AnswerRow(
          "cya.label.name",
          Seq(s"${x.firstName} ${x.lastName}"),
          false,
          controllers.register.company.directors.routes.DirectorDetailsController.onPageLoad(CheckMode, index).url
        ),
        AnswerRow(
          "cya.label.dob",
          Seq(s"${DateHelper.formatDate(x.dateOfBirth)}"),
          false,
          controllers.register.company.directors.routes.DirectorDetailsController.onPageLoad(CheckMode, index).url)
      )
    }

  def directorContactDetails(index: Int): Seq[AnswerRow] = {
    cya(DirectorContactDetailsId(index)){ x =>
      Seq(
        AnswerRow(
          "contactDetails.email",
          Seq(s"${x.email}"),
          false,
          controllers.register.company.directors.routes.DirectorContactDetailsController.onPageLoad(CheckMode, index).url
        ),
        AnswerRow(
          "contactDetails.phone",
          Seq(s"${x.phone}"),
          false,
          controllers.register.company.directors.routes.DirectorContactDetailsController.onPageLoad(CheckMode, index).url
        )
      )
    }
  }

  def directorUniqueTaxReference(index: Int): Seq[AnswerRow] =
    cya(DirectorUniqueTaxReferenceId(index)){
      case _@DirectorUniqueTaxReference.Yes(utr) => Seq(
        AnswerRow(
          "directorUniqueTaxReference.checkYourAnswersLabel",
          Seq(s"${DirectorUniqueTaxReference.Yes}"),
          true,
          controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, index).url
        ),
        AnswerRow(
          "directorUniqueTaxReference.checkYourAnswersLabel.utr",
          Seq(utr),
          true,
          controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, index).url
        )
      )
      case _@DirectorUniqueTaxReference.No(reason) => Seq(
        AnswerRow(
          "directorUniqueTaxReference.checkYourAnswersLabel",
          Seq(s"${DirectorUniqueTaxReference.No}"),
          true,
          controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, index).url
        ),
        AnswerRow(
          "directorUniqueTaxReference.checkYourAnswersLabel.reason",
          Seq(reason),
          true,
          controllers.register.company.directors.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, index).url
        )
      )
    }

  def directorNino(index: Int): Seq[AnswerRow] =
    cya(DirectorNinoId(index)){
      case _@Yes(nino) => Seq(
        AnswerRow(
          "directorNino.checkYourAnswersLabel",
          Seq(s"${DirectorNino.Yes}"),
          true,
          controllers.register.company.directors.routes.DirectorNinoController.onPageLoad(CheckMode, index).url
        ),
        AnswerRow(
          "directorNino.checkYourAnswersLabel.nino", Seq(nino),
          true,
          controllers.register.company.directors.routes.DirectorNinoController.onPageLoad(CheckMode, index).url
        )
      )
      case _@No(reason) => Seq(
        AnswerRow(
          "directorNino.checkYourAnswersLabel", Seq(s"${DirectorNino.No}"),
          true,
          controllers.register.company.directors.routes.DirectorNinoController.onPageLoad(CheckMode, index).url
        ),
        AnswerRow(
          "directorNino.checkYourAnswersLabel.reason", Seq(reason),
          true,
          controllers.register.company.directors.routes.DirectorNinoController.onPageLoad(CheckMode, index).url
        )
      )
    }

  def email: Seq[AnswerRow] =
    cya(ContactDetailsId) { x =>
      Seq(AnswerRow(
        "contactDetails.email.checkYourAnswersLabel",
        Seq(x.email),
        false,
        controllers.register.company.routes.ContactDetailsController.onPageLoad(CheckMode).url
      ))
    }

  def phone: Seq[AnswerRow] =
    cya(ContactDetailsId) { x =>
      Seq(AnswerRow(
        "contactDetails.phone.checkYourAnswersLabel",
        Seq(x.phone),
        false,
        controllers.register.company.routes.ContactDetailsController.onPageLoad(CheckMode).url
      ))
    }

  def companyDetails: Seq[AnswerRow] =
    cya(CompanyDetailsId){ x =>
      Seq(AnswerRow(
        "companyDetails.checkYourAnswersLabel",
        Seq(x.companyName),
        false,
        controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url
      ))
    }

  def vatRegistrationNumber: Seq[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDetailsId) match {
    case Some(CompanyDetails(_, Some(vatRegNo), _)) =>
      Seq(AnswerRow(
        "companyDetails.vatRegistrationNumber.checkYourAnswersLabel",
        Seq(vatRegNo),
        false,
        controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url
      ))
    case _ => Seq.empty[AnswerRow]
  }

  def payeEmployerReferenceNumber: Seq[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDetailsId) match {
    case Some(CompanyDetails(_, _, Some(payeRefNo))) =>
      Seq(AnswerRow(
        "companyDetails.payeEmployerReferenceNumber.checkYourAnswersLabel",
        Seq(payeRefNo),
        false,
        controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url
      ))
    case _ => Seq.empty[AnswerRow]
  }

  private def addressAnswer(address: Address): Seq[String] = {
    val country = countryOptions.options
      .find(_.value == address.country)
      .map(_.label)
      .getOrElse(address.country)

    Seq(
      Some(s"${address.addressLine1},"),
      Some(s"${address.addressLine2},"),
      address.addressLine3.map(line3 => s"$line3,"),
      address.addressLine4.map(line4 => s"$line4,"),
      address.postcode.map(postcode => s"$postcode,"),
      Some(country)
    ).flatten
  }

}
