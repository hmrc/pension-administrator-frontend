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

package forms.mappings

import java.time.LocalDate

import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.domain.Nino
import utils.countryOptions.CountryOptions

trait Constraints {

  protected val crnRegex = """^[A-Za-z0-9 -]{7,8}$"""
  protected val utrRegex = """^\d{10}$"""
  protected val emailRegex = """^[^@'<>"]+@[^@'<>"]+$"""
  protected val regexEmailRestrictive = "^(?:[a-z0-9!#$%&*+\\/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&*+\\/=?^_`{|}~-]+)*)" +
    "@(?:[a-z0-9!#$%&*+\\/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&*+\\/=?^_`{|}~-]+)*)$"

  protected val phoneNumberRegex = """^[0-9 ()+--]{1,24}$"""
  protected val vatRegex = """^\d{9}$"""
  protected val payeRegex = """^[0-9]{3}[0-9A-Za-z]{1,13}$"""
  protected val postCodeRegex = """^[A-Za-z]{1,2}[0-9][0-9A-Za-z]?[ ]?[0-9][A-Za-z]{2}$"""
  protected val postCodeNonUkRegex = """^([0-9]+-)*[0-9]+$"""
  protected val nameRegex = """^[a-zA-Z\u00C0-\u00FF'‘’\u2014\u2013\u2010\u002d]{1,35}$"""
  protected val safeTextRegex = """^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,160}$"""
  protected val addressLineRegex = """^[A-Za-z0-9 !'‘’"“”(),./\u2014\u2013\u2010\u002d]{1,35}$"""

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint {
      input =>
        constraints
          .map(_.apply(input))
          .find(_ != Valid)
          .getOrElse(Valid)
    }

  protected def returnOnFirstFailure[T](constraints: Constraint[T]*): Constraint[T] =
    Constraint {
      field =>
        constraints
          .map(_.apply(field))
          .filterNot(_ == Valid)
          .headOption.getOrElse(Valid)
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input >= minimum) {
          Valid
        } else {
          Invalid(errorKey, minimum)
        }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, maximum)
        }
    }

  protected def inRange[A](minimum: A, maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input >= minimum && input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, minimum, maximum)
        }
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) =>
        Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum)
    }

  protected def companyRegistrationNumber(errorKey: String): Constraint[String] = regexp(crnRegex, errorKey)

  protected def uniqueTaxReference(errorKey: String): Constraint[String] = regexp(utrRegex, errorKey)

  protected def emailAddress(errorKey: String): Constraint[String] = regexp(emailRegex, errorKey)

  protected def emailAddressRestrictive(errorKey: String): Constraint[String] = regexp(regexEmailRestrictive, errorKey)

  protected def phoneNumber(errorKey: String): Constraint[String] = regexp(phoneNumberRegex, errorKey)

  protected def vatRegistrationNumber(errorKey: String): Constraint[String] = regexp(vatRegex, errorKey)

  protected def payeEmployerReferenceNumber(errorKey: String): Constraint[String] = regexp(payeRegex, errorKey)

  protected def postCode(errorKey: String): Constraint[String] = regexp(postCodeRegex, errorKey)

  protected def postCodeNonUk(errorKey: String): Constraint[String] = regexp(postCodeNonUkRegex, errorKey)

  protected def safeText(errorKey: String): Constraint[String] = regexp(safeTextRegex, errorKey)

  protected def validNino(invalidKey: String) : Constraint[String] = {
    Constraint {
      case nino if Nino.isValid(nino.replaceAll(" ", "").toUpperCase) => Valid
      case _ => Invalid(invalidKey)
    }
  }

  protected def name(errorKey: String): Constraint[String] = regexp(nameRegex, errorKey)

  protected def addressLine(errorKey: String): Constraint[String] = regexp(addressLineRegex, errorKey)

  protected def country(countryOptions: CountryOptions, errorKey: String): Constraint[String] =
    Constraint {
      input =>
        val x = countryOptions.options
        countryOptions.options
          .find(_.value == input)
          .map(_ => Valid)
          .getOrElse(Invalid(errorKey))
    }

  protected def nonFutureDate(errorKey: String): Constraint[LocalDate] =
    Constraint {
      case date if !LocalDate.now().isBefore(date) => Valid
      case _ => Invalid(errorKey)
    }

}
