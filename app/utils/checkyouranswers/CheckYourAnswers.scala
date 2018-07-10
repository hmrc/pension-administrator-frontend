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

package utils.checkyouranswers

import identifiers.TypedIdentifier
import models._
import models.register.adviser.AdviserDetails
import play.api.libs.json.Reads
import utils.UserAnswers
import utils.checkyouranswers.CheckYourAnswers.addressAnswer
import utils.countryOptions.CountryOptions
import viewmodels.AnswerRow

import scala.language.implicitConversions

trait CheckYourAnswers[I <: TypedIdentifier.PathDependent] {
  def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow]
}

object CheckYourAnswers {

  implicit def businessDetails[I <: TypedIdentifier[BusinessDetails]](implicit r: Reads[BusinessDetails]): CheckYourAnswers[I] = BusinessDetailsCYA()()

  implicit def tolerantAddress[I <: TypedIdentifier[TolerantAddress]](implicit r: Reads[TolerantAddress], countryOptions: CountryOptions): CheckYourAnswers[I] = TolerantAddressCYA()()

  implicit def address[I <: TypedIdentifier[Address]](implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] = AddressCYA()()

  implicit def addressYears[I <: TypedIdentifier[AddressYears]](implicit r: Reads[AddressYears]): CheckYourAnswers[I] = AddressYearsCYA()()

  implicit def adviserDetails[I <: TypedIdentifier[AdviserDetails]](implicit r: Reads[AdviserDetails]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id).map{ adviserDetails =>
          Seq(
            AnswerRow("cya.label.name", Seq(adviserDetails.name), false, changeUrl),
            AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq(adviserDetails.email), false, changeUrl),
            AnswerRow("contactDetails.phone.checkYourAnswersLabel", Seq(adviserDetails.phone), false, changeUrl)
          )
        } getOrElse Seq.empty[AnswerRow]
      }
    }
  }

  implicit def paye[I <: TypedIdentifier[Paye]](implicit r: Reads[Paye]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map{
          case Paye.Yes(paye) => Seq(
            AnswerRow(
              "commom.paye.label",
              Seq(paye),
              false,
              changeUrl
            )
          )
          case Paye.No => Seq(
            AnswerRow(
              "commom.paye.label",
              Seq("site.no"),
              true,
              changeUrl
          ))
        } getOrElse Seq.empty[AnswerRow]
    }
  }

  implicit def vat[I <: TypedIdentifier[Vat]](implicit r: Reads[Vat]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map{
          case Vat.Yes(vat) => Seq(
            AnswerRow(
              "common.vatRegistrationNumber.checkYourAnswersLabel",
              Seq(vat),
              false,
              changeUrl
            )
          )
          case Vat.No => Seq(
            AnswerRow(
              "common.vatRegistrationNumber.checkYourAnswersLabel",
              Seq("site.no"),
              true,
              changeUrl
          ))
        } getOrElse Seq.empty[AnswerRow]
    }
  }

  def addressAnswer(address: Address)(implicit countryOptions: CountryOptions): Seq[String] = {
    val country = countryOptions.options.find(_.value == address.country).map(_.label).getOrElse(address.country)
    Seq(
      Some(s"${address.addressLine1},"),
      Some(s"${address.addressLine2},"),
      address.addressLine3.map(line3 => s"$line3,"),
      address.addressLine4.map(line4 => s"$line4,"),
      address.postcode.map(postCode => s"$postCode,"),
      Some(country)
    ).flatten
  }

  implicit def contactDetails[I <: TypedIdentifier[ContactDetails]](implicit r: Reads[ContactDetails]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id).map{ contactDetails =>
          Seq(
            AnswerRow(
              "contactDetails.email",
              Seq(s"${contactDetails.email}"),
              false,
              changeUrl
            ),
            AnswerRow(
              "contactDetails.phone",
              Seq(s"${contactDetails.phone}"),
              false,
              changeUrl
            ))
        } getOrElse Seq.empty[AnswerRow]
      }
    }
  }

  implicit def string[I <: TypedIdentifier[String]](implicit rds: Reads[String]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          string =>
            Seq(AnswerRow(
              s"${id.toString}.checkYourAnswersLabel",
              Seq(string),
              answerIsMessageKey = false,
              changeUrl
            ))
        } getOrElse Seq.empty[AnswerRow]
    }

  implicit def boolean[I <: TypedIdentifier[Boolean]](implicit rds: Reads[Boolean]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          flag =>
            Seq(AnswerRow(
              s"${id.toString}.checkYourAnswersLabel",
              Seq(if (flag) "site.yes" else "site.no"),
              answerIsMessageKey = true,
              changeUrl
            ))
        } getOrElse Seq.empty[AnswerRow]
    }
  }

}

case class AddressCYA[I <: TypedIdentifier[Address]](label: String = "cya.label.address") {
  def apply()(implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) = {

        def addressAnswer(address: Address): Seq[String] = {
          val country = countryOptions.options.find(_.value == address.country).map(_.label).getOrElse(address.country)
          Seq(
            Some(s"${address.addressLine1},"),
            Some(s"${address.addressLine2},"),
            address.addressLine3.map(line3 => s"$line3,"),
            address.addressLine4.map(line4 => s"$line4,"),
            address.postcode.map(postCode => s"$postCode,"),
            Some(country)
          ).flatten
        }

        userAnswers.get(id).map { address =>
          Seq(AnswerRow(
            label,
            addressAnswer(address),
            false,
            changeUrl
          ))
        } getOrElse Seq.empty[AnswerRow]
      }
    }
  }
}

case class BusinessDetailsCYA[I <: TypedIdentifier[BusinessDetails]](nameLabel: String = "cya.label.name", utrLabel: String = "businessDetails.utr") {
  def apply()(implicit rds: Reads[BusinessDetails]): CheckYourAnswers[I] = new CheckYourAnswers[I] {
    override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
      userAnswers.get(id).map{ businessDetails =>
        val nameRow = AnswerRow(
          nameLabel,
          Seq(businessDetails.companyName),
          false,
          None
        )
        val utrRow = AnswerRow(
          utrLabel,
          Seq(businessDetails.uniqueTaxReferenceNumber),
          false,
          None
        )
        Seq(nameRow, utrRow)
      } getOrElse Seq.empty[AnswerRow]
  }
}

case class TolerantAddressCYA[I <: TypedIdentifier[TolerantAddress]](label: String = "common.manual.address.checkyouranswers") {
  def apply()(implicit r: Reads[TolerantAddress], countryOptions: CountryOptions): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id).map { address =>
          Seq(AnswerRow(
            label,
            addressAnswer(address.toAddress),
            false,
            None
          ))
        } getOrElse Seq.empty[AnswerRow]
      }
    }
  }
}

case class AddressYearsCYA[I <: TypedIdentifier[AddressYears]](label: String = "checkyouranswers.partnership.address.years") {
  def apply()(implicit r: Reads[AddressYears]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers) =
        userAnswers.get(id).map(addressYears =>
          Seq(AnswerRow(
            label,
            Seq(s"common.addressYears.$addressYears"),
            true,
            changeUrl
          ))) getOrElse Seq.empty[AnswerRow]
    }
  }
}