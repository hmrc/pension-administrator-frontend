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

import org.scalatest.{MustMatchers, WordSpec}
import play.api.data.validation.{Invalid, Valid}

class ConstraintsSpec extends WordSpec with MustMatchers with Constraints with RegexBehaviourSpec {

  "firstError" must {

    "return Valid when all constraints pass" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("foo")
      result mustEqual Valid
    }

    "return Invalid when the first constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("a" * 11)
      result mustEqual Invalid("error.length", 10)
    }

    "return Invalid when the second constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("")
      result mustEqual Invalid("error.regexp", """^\w+$""")
    }

    "return Invalid for the first error when both constraints fail" in {
      val result = firstError(maxLength(-1, "error.length"), regexp("""^\w+$""", "error.regexp"))("")
      result mustEqual Invalid("error.length", -1)
    }
  }

  "minimumValue" must {

    "return Valid for a number greater than the threshold" in {
      val result = minimumValue(1, "error.min").apply(2)
      result mustEqual Valid
    }

    "return Valid for a number equal to the threshold" in {
      val result = minimumValue(1, "error.min").apply(1)
      result mustEqual Valid
    }

    "return Invalid for a number below the threshold" in {
      val result = minimumValue(1, "error.min").apply(0)
      result mustEqual Invalid("error.min", 1)
    }
  }

  "maximumValue" must {

    "return Valid for a number less than the threshold" in {
      val result = maximumValue(1, "error.max").apply(0)
      result mustEqual Valid
    }

    "return Valid for a number equal to the threshold" in {
      val result = maximumValue(1, "error.max").apply(1)
      result mustEqual Valid
    }

    "return Invalid for a number above the threshold" in {
      val result = maximumValue(1, "error.max").apply(2)
      result mustEqual Invalid("error.max", 1)
    }
  }

  "regexp" must {

    "return Valid for an input that matches the expression" in {
      val result = regexp("""^\w+$""", "error.invalid")("foo")
      result mustEqual Valid
    }

    "return Invalid for an input that does not match the expression" in {
      val result = regexp("""^\d+$""", "error.invalid")("foo")
      result mustEqual Invalid("error.invalid", """^\d+$""")
    }
  }

  "maxLength" must {

    "return Valid for a string shorter than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 9)
      result mustEqual Valid
    }

    "return Valid for an empty string" in {
      val result = maxLength(10, "error.length")("")
      result mustEqual Valid
    }

    "return Valid for a string equal to the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 10)
      result mustEqual Valid
    }

    "return Invalid for a string longer than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 11)
      result mustEqual Invalid("error.length", 10)
    }
  }

  "companyRegistrationNumber" must {

    val validCrn = Table(
      "crn",
      "1234567",
      "A123456",
      "AB123456"
    )

    val invalidCrn = Table(
      "crn",
      "123456",
      "12345678",
      "ABC123456"
    )

    val invalidMsg = "companyRegistrationNumber.error.invalid"

    behave like regexWithValidAndInvalidExamples(companyRegistrationNumber, validCrn, invalidCrn, invalidMsg, crn)

  }

  "companyUniqueTaxReference" must {

    val validUtr = Table(
      "utr",
      "0123456789",
      "9876543210",
      "5432112345"
    )

    val invalidUtr = Table(
      "utr",
      "32423423432423",
      "12323",
      "ABC123456"
    )

    val invalidMsg = "companyUniqueTaxReference.error.invalid"

    behave like regexWithValidAndInvalidExamples(companyUniqueTaxReference, validUtr, invalidUtr, invalidMsg, utr)
  }

  "vatRgistrationNumber" must {

    val validVat = Table(
      "vat",
      "123456789"
    )

    val invalidVat = Table(
      "vat",
      "12345678",
      "1234567890",
      "12345678A",
      "12345678_"
    )

    val invalidMsg = "vatRgistrationNumber.error.invalid"

    behave like regexWithValidAndInvalidExamples(vatRgistrationNumber, validVat, invalidVat, invalidMsg, vat)
  }

  "payeEmployerReferenceNumber" must {

    val validPaye = Table(
      "paye",
      "1",
      "A",
      "1234567890123",
      "1234567890ABC",
      "ABCDEFGHIJKLM"
    )

    val invalidPaye = Table(
      "paye",
      "12345678901234",
      "1234567890ABCD",
      "A1._-"
    )

    val invalidMsg = "payeEmployerReferenceNumber.error.invalid"

    behave like regexWithValidAndInvalidExamples(payeEmployerReferenceNumber, validPaye, invalidPaye, invalidMsg, paye)
  }


  "email" must {

    val validEmail = Table(
      "email",
      "a@email.com",
      "a@bc",
      "123@456"
    )

    val invalidEmail = Table(
      "email",
      "32423423432423",
      "12323",
      "@@@@@@"
    )

    val invalidMsg = "contactDetails.error.email.valid"

    behave like regexWithValidAndInvalidExamples(emailAddress, validEmail, invalidEmail, invalidMsg, email)
  }

  "wholeNumber" must {

    val validNumber = Table(
      "wholeNumber",
      "1",
      "99999999999999999999999",
      "123456"
    )

    val invalidNumber = Table(
      "wholeNumber",
      "324234.23432423",
      "123@23",
      "@@@@@@"
    )

    val invalidMsg = "Invalid test"

    behave like regexWithValidAndInvalidExamples(wholeNumber, validNumber, invalidNumber, invalidMsg, number)
  }

  "postCode" must {

    val validPostCode = Table(
      "postCode",
      "A12 1AB",
      "AB12 1AB",
      "AB1A 1AB",
      "AB121AB"
    )

    val invalidPostCode = Table(
      "postCode",
      "aB12 1AB",
      "Ab12 1AB",
      "0B12 1AB",
      "A012 1AB",
      "ABC2 1AB",
      "AB1a 1AB",
      "AB12 AAB",
      "AB12 11B",
      "AB12 1A1"
    )

    val invalidMsg = "Invalid post code"

    behave like regexWithValidAndInvalidExamples(postalCode, validPostCode, invalidPostCode, invalidMsg, postcode)
  }

  "name" must {

    val validName = Table(
      "name",
      "A",
      "A_",
      "A1",
      "a",
      "a_",
      "a1"
    )

    val invalidName = Table(
      "name",
      " A",
      "_A",
      "1A",
      " a",
      "_a",
      "1a"
    )

    val invalidMsg = "Invalid name"

    behave like regexWithValidAndInvalidExamples(name, validName, invalidName, invalidMsg, nameRegex)
  }

}
