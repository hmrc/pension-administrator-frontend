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

package utils.checkyouranswers

import java.time.LocalDate

import identifiers.TypedIdentifier
import identifiers.register.BusinessNameId
import identifiers.register.adviser.AdviserNameId
import identifiers.register.company.directors.DirectorNameId
import identifiers.register.partnership.partners.PartnerNameId
import models._
import play.api.libs.json.Reads
import utils.countryOptions.CountryOptions
import utils.{DateHelper, UserAnswers}
import viewmodels.{AnswerRow, Link, Message}

trait CheckYourAnswers[I <: TypedIdentifier.PathDependent] {
  def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow]
}

trait CheckYourAnswersBusiness[I <: TypedIdentifier.PathDependent] extends CheckYourAnswers[I] {
  protected def dynamicMessage(ua: UserAnswers, messageKey: String): Message =
    ua.get(BusinessNameId).map(Message(messageKey, _)).getOrElse(Message(messageKey, Message("theBusiness")))
}

trait CheckYourAnswersPartnership[I <: TypedIdentifier.PathDependent] extends CheckYourAnswers[I] {
  protected def dynamicMessage(ua: UserAnswers, messageKey: String): Message =
    ua.get(BusinessNameId).map(name => Message(messageKey, name)).getOrElse(Message(messageKey, Message("thePartnership")))
}

trait CheckYourAnswersPartner[I <: TypedIdentifier.PathDependent] extends CheckYourAnswers[I] {
  protected def dynamicMessage(ua: UserAnswers, messageKey: String, index: Index): Message =
    ua.get(PartnerNameId(index)).map(name => Message(messageKey, name.fullName)).getOrElse(Message(messageKey, Message("thePartner")))
}

trait CheckYourAnswersDirector[I <: TypedIdentifier.PathDependent] extends CheckYourAnswers[I] {
  protected def dynamicMessage(ua: UserAnswers, messageKey: String, index: Index): Message =
    ua.get(DirectorNameId(index)).map(name => Message(messageKey, name.fullName)).getOrElse(Message(messageKey, Message("theDirector")))
}

trait CheckYourAnswersAdviser[I <: TypedIdentifier.PathDependent] extends CheckYourAnswers[I] {
  protected def dynamicMessage(ua: UserAnswers, messageKey: String): Message =
    ua.get(AdviserNameId).map(name => Message(messageKey, name)).getOrElse(Message(messageKey, Message("theAdviser")))
}

object CheckYourAnswers {

  implicit def reference[I <: TypedIdentifier[ReferenceValue]]
  (implicit rds: Reads[ReferenceValue]): CheckYourAnswers[I] = ReferenceValueCYA()()

  implicit def personName[I <: TypedIdentifier[PersonName]]
  (implicit rds: Reads[PersonName]): CheckYourAnswers[I] = PersonNameCYA()()

  implicit def date[I <: TypedIdentifier[LocalDate]]
  (implicit rds: Reads[LocalDate]): CheckYourAnswers[I] = DateCYA()()

  implicit def tolerantIndividual[I <: TypedIdentifier[TolerantIndividual]]
  (implicit rds: Reads[TolerantIndividual]): CheckYourAnswers[I] = TolerantIndividualCYA()()

}

case class AddressCYA[I <: TypedIdentifier[Address]](label: String = "cya.label.address", hiddenLabel: Option[Message] = None,
                                                     isMandatory: Boolean = true) {
  def apply()(implicit rds: Reads[Address], countryOptions: CountryOptions): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id).map { address =>
          Seq(AnswerRow(
            label,
            address.lines(countryOptions),
            answerIsMessageKey = false,
            changeUrl,
            visuallyHiddenText = hiddenLabel
          ))
        } getOrElse {
          if (isMandatory) {
            Seq(AnswerRow(label, Seq("site.not_entered"),
              answerIsMessageKey = true, changeUrl.map(link => Link(link.url, "site.add")), hiddenLabel))
          } else {
            Seq.empty[AnswerRow]
          }
        }
      }
    }
  }
}

case class TolerantAddressCYA[I <: TypedIdentifier[TolerantAddress]](label: String = "cya.label.address", hiddenLabel: Option[Message] = None,
                                                     isMandatory: Boolean = true) {
  def apply()(implicit rds: Reads[TolerantAddress], countryOptions: CountryOptions): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id).map { address =>
          Seq(AnswerRow(
            label,
            address.lines(countryOptions),
            answerIsMessageKey = false,
            changeUrl,
            visuallyHiddenText = hiddenLabel
          ))
        } getOrElse {
          if (isMandatory) {
            Seq(AnswerRow(label, Seq("site.not_entered"),
              answerIsMessageKey = true, changeUrl.map(link => Link(link.url, "site.add")), hiddenLabel))
          } else {
            Seq.empty[AnswerRow]
          }
        }
      }
    }
  }
}

case class AddressYearsCYA[I <: TypedIdentifier[AddressYears]](label: String = "checkyouranswers.partnership.address.years",
                                                               hiddenLabel: Option[Message] = None,
                                                               isMandatory: Boolean = true) {
  def apply()(implicit r: Reads[AddressYears]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map(addressYears =>
          Seq(AnswerRow(
            label,
            Seq(s"common.addressYears.$addressYears"),
            answerIsMessageKey = true,
            changeUrl,
            visuallyHiddenText = hiddenLabel
          ))) getOrElse {
          if (isMandatory) {
            Seq(AnswerRow(label, Seq("site.not_entered"),
              answerIsMessageKey = true, changeUrl.map(link => Link(link.url, "site.add")), hiddenLabel))
          } else {
            Seq.empty[AnswerRow]
          }
        }
    }
  }
}

case class StringCYA[I <: TypedIdentifier[String]](label: Option[String] = None, hiddenLabel: Option[Message] = None, isMandatory: Boolean = true) {
  def apply()(implicit rds: Reads[String]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          string =>
            Seq(AnswerRow(
              label getOrElse s"${id.toString}.checkYourAnswersLabel",
              answer = Seq(string),
              answerIsMessageKey = false,
              changeUrl = changeUrl,
              visuallyHiddenText = hiddenLabel
            ))
        } getOrElse {
          if (isMandatory) {
            Seq(AnswerRow(label getOrElse s"${id.toString}.checkYourAnswersLabel", Seq("site.not_entered"),
              answerIsMessageKey = true, changeUrl.map(link => Link(link.url, "site.add")), hiddenLabel))
          } else {
            Seq.empty[AnswerRow]
          }
        }
    }
}

case class BooleanCYA[I <: TypedIdentifier[Boolean]](label: Option[String] = None, hiddenLabel: Option[Message] = None, isMandatory: Boolean = true) {
  implicit def apply()(implicit rds: Reads[Boolean]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          flag =>
            Seq(AnswerRow(
              label getOrElse s"${id.toString}.checkYourAnswersLabel",
              Seq(if (flag) "site.yes" else "site.no"),
              answerIsMessageKey = true,
              changeUrl,
              hiddenLabel
            ))
        } getOrElse {
          if (isMandatory) {
            Seq(AnswerRow(label getOrElse s"${id.toString}.checkYourAnswersLabel", Seq("site.not_entered"),
              answerIsMessageKey = true, changeUrl.map(link => Link(link.url, "site.add")), hiddenLabel))
          } else {
            Seq.empty[AnswerRow]
          }
        }
    }
  }
}

case class ReferenceValueCYA[I <: TypedIdentifier[ReferenceValue]](
                                                                    nameLabel: Option[String] = None,
                                                                    hiddenNameLabel: Option[Message] = None,
                                                                    isMandatory: Boolean = true) {
  def apply()(implicit rds: Reads[ReferenceValue]): CheckYourAnswers[I] = {
    new CheckYourAnswers[I] {

      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map { reference =>
          Seq(AnswerRow(nameLabel getOrElse s"${id.toString}.heading",
            Seq(s"${reference.value}"),
            answerIsMessageKey = false,
            changeUrl,
            hiddenNameLabel
          ))
        } getOrElse {
          if (isMandatory) {
            Seq(AnswerRow(nameLabel getOrElse s"${id.toString}.heading", Seq("site.not_entered"),
              answerIsMessageKey = true, changeUrl.map(link => Link(link.url, "site.add")), hiddenNameLabel))
          } else {
            Seq.empty[AnswerRow]
          }
        }
    }
  }
}

case class PersonNameCYA[I <: TypedIdentifier[PersonName]](label: Option[String] = None, hiddenLabel: Option[Message] = None) {
  def apply()(implicit rds: Reads[PersonName]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          personName =>
            Seq(AnswerRow(
              label getOrElse s"${id.toString}.heading",
              answer = Seq(personName.fullName),
              answerIsMessageKey = false,
              changeUrl = changeUrl,
              visuallyHiddenText = hiddenLabel
            ))
        } getOrElse Seq.empty[AnswerRow]
    }
}

case class DateCYA[I <: TypedIdentifier[LocalDate]](label: Option[String] = None, hiddenLabel: Option[Message] = None, isMandatory: Boolean = true) {
  def apply()(implicit rds: Reads[LocalDate]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          date =>
            Seq(AnswerRow(
              label getOrElse s"${id.toString}.heading",
              answer = Seq(DateHelper.formatDate(date)),
              answerIsMessageKey = false,
              changeUrl = changeUrl,
              visuallyHiddenText = hiddenLabel
            ))
        } getOrElse {
          if (isMandatory) {
            Seq(AnswerRow(label getOrElse s"${id.toString}.checkYourAnswersLabel", Seq("site.not_entered"),
              answerIsMessageKey = true, changeUrl.map(link => Link(link.url, "site.add")), hiddenLabel))
          } else {
            Seq.empty[AnswerRow]
          }
        }
    }
}

case class TolerantIndividualCYA[I <: TypedIdentifier[TolerantIndividual]](label: Option[String] = None,
                                                                           hiddenLabel: Option[Message] = None, isMandatory: Boolean = true) {
  def apply()(implicit rds: Reads[TolerantIndividual]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          individual =>
            Seq(AnswerRow(
              label getOrElse s"${id.toString}.heading",
              answer = Seq(individual.fullName),
              answerIsMessageKey = false,
              changeUrl = changeUrl,
              visuallyHiddenText = hiddenLabel
            ))
        } getOrElse {
          if (isMandatory) {
            Seq(AnswerRow(label getOrElse s"${id.toString}.checkYourAnswersLabel", Seq("site.not_entered"),
              answerIsMessageKey = true, changeUrl.map(link => Link(link.url, "site.add")), hiddenLabel))
          } else {
            Seq.empty[AnswerRow]
          }
        }
    }
}
