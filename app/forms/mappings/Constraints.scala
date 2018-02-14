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

import play.api.data.validation.{Constraint, Invalid, Valid}

trait Constraints {

  protected val crn = """^\d{7}|[a-zA-Z]{1,2}\d{6}$"""
  protected val utr = """^\d{10}$"""
  protected val email = """^[^@<>]+@[^@<>]+$"""
  protected val number = """^[0-9]+$"""
  protected val vat = """^\d{9}$"""
  protected val paye = """^[a-zA-Z\d]{1,13}$"""

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

  protected def companyRegistrationNumber(errorKey: String): Constraint[String] = regexp(crn, errorKey)

  protected def companyUniqueTaxReference(errorKey: String): Constraint[String] = regexp(utr, errorKey)

  protected def emailAddress(errorKey: String): Constraint[String] = regexp(email, errorKey)

  protected def wholeNumber(errorKey: String): Constraint[String] = regexp(number, errorKey)

  protected def vatRgistrationNumber(errorKey: String): Constraint[String] = regexp(vat, errorKey)

  protected def payeEmployerReferenceNumber(errorKey: String): Constraint[String] = regexp(paye, errorKey)

  protected def postcode(errorKey: String): Constraint[String] = ???
}
