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

import base.SpecBase
import forms.FormSpec
import org.scalatest.{Matchers, WordSpec}
import play.api.data.validation.{Invalid, Valid}
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, InputOption}

class ConstraintsSpec extends FormSpec with Matchers with Constraints with RegexBehaviourSpec {

  // scalastyle:off magic.number

  "firstError" must {

    "return Valid when all constraints pass" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("foo")
      result shouldEqual Valid
    }

    "return Invalid when the first constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("a" * 11)
      result shouldEqual Invalid("error.length", 10)
    }

    "return Invalid when the second constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("")
      result shouldEqual Invalid("error.regexp", """^\w+$""")
    }

    "return Invalid for the first error when both constraints fail" in {
      val result = firstError(maxLength(-1, "error.length"), regexp("""^\w+$""", "error.regexp"))("")
      result shouldEqual Invalid("error.length", -1)
    }
  }

  "minimumValue" must {

    "return Valid for a number greater than the threshold" in {
      val result = minimumValue(1, "error.min").apply(2)
      result shouldEqual Valid
    }

    "return Valid for a number equal to the threshold" in {
      val result = minimumValue(1, "error.min").apply(1)
      result shouldEqual Valid
    }

    "return Invalid for a number below the threshold" in {
      val result = minimumValue(1, "error.min").apply(0)
      result shouldEqual Invalid("error.min", 1)
    }
  }

  "maximumValue" must {

    "return Valid for a number less than the threshold" in {
      val result = maximumValue(1, "error.max").apply(0)
      result shouldEqual Valid
    }

    "return Valid for a number equal to the threshold" in {
      val result = maximumValue(1, "error.max").apply(1)
      result shouldEqual Valid
    }

    "return Invalid for a number above the threshold" in {
      val result = maximumValue(1, "error.max").apply(2)
      result shouldEqual Invalid("error.max", 1)
    }
  }

  "regexp" must {

    "return Valid for an input that matches the expression" in {
      val result = regexp("""^\w+$""", "error.invalid")("foo")
      result shouldEqual Valid
    }

    "return Invalid for an input that does not match the expression" in {
      val result = regexp("""^\d+$""", "error.invalid")("foo")
      result shouldEqual Invalid("error.invalid", """^\d+$""")
    }
  }

  "maxLength" must {

    "return Valid for a string shorter than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 9)
      result shouldEqual Valid
    }

    "return Valid for an empty string" in {
      val result = maxLength(10, "error.length")("")
      result shouldEqual Valid
    }

    "return Valid for a string equal to the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 10)
      result shouldEqual Valid
    }

    "return Invalid for a string longer than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 11)
      result shouldEqual Invalid("error.length", 10)
    }
  }

  "companyRegistrationNumber" must {

    val validCrn = Table(
      "crn",
      "12345678",
      "ABCDEFGH",
      "A1B2C3D4"
    )

    val invalidCrn = Table(
      "crn",
      "123456",
      "1234567",
      "123456789",
      "ABCDEFG",
      "ABCDEF",
      "A1B2C3D",
      "ABCDEFGHI"
    )

    val invalidMsg = "companyRegistrationNumber.error.invalid"

    behave like regexWithValidAndInvalidExamples(companyRegistrationNumber, validCrn, invalidCrn, invalidMsg, crnRegex)

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

    behave like regexWithValidAndInvalidExamples(uniqueTaxReference, validUtr, invalidUtr, invalidMsg, utrRegex)
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

    behave like regexWithValidAndInvalidExamples(vatRegistrationNumber, validVat, invalidVat, invalidMsg, vatRegex)
  }

  "payeEmployerReferenceNumber" must {

    val validPaye = Table(
      "paye",
      "123A",
      "123abcdefghijklm",
      "123ABCDEFGHIJKLM",
      "0001234567890123",
      "121AB45CD67QWERT"
    )

    val invalidPaye = Table(
      "paye",
      "123",
      "123abcdefghijklmN",
      "12abcdefghijklm",
      "123***?."
    )

    val invalidMsg = "payeEmployerReferenceNumber.error.invalid"

    behave like regexWithValidAndInvalidExamples(payeEmployerReferenceNumber, validPaye, invalidPaye, invalidMsg, payeRegex)
  }

  "emailAddressRestrictive" must {

    val validEmail = Table(
      "ema.il@cd.com",
      "a@email.com",
      "1.2.3@4.5.6"
    )

    val invalidEmail = Table(
      "email@.com",
      "32..423423432423",
      "a@bc",
      "@@@@@@",
      ".df@com",
      "123 2@s.com",
      "xyz;a@v",
      "AÀ@v.com"
    )

    val invalidMsg = "contactDetails.error.email.valid"

    behave like regexWithValidAndInvalidExamples(emailAddressRestrictive, validEmail, invalidEmail, invalidMsg, emailRestrictiveRegex)
  }

  "phoneNumber" must {

    val validNumber = Table(
      "phoneNumber",
      "1",
      "99999999999999999999999",
      "123456"
    )

    val invalidNumber = Table(
      "phoneNumber",
      "324234.23432423",
      "123@23",
      "@@@@@@"
    )

    val invalidMsg = "Invalid test"

    behave like regexWithValidAndInvalidExamples(phoneNumber, validNumber, invalidNumber, invalidMsg, phoneNumberRegex)
  }

  "postCode" must {

    val validPostCode = Table(
      "postCode",
      "A12 1AB",
      "AB12 1AB",
      "AB1A 1AB",
      "AB121AB",
      "aB12 1AB",
      "Ab12 1AB",
      "AB1a 1AB"
    )

    val invalidPostCode = Table(
      "postCode",
      "0B12 1AB",
      "A012 1AB",
      "ABC2 1AB",
      "AB12 AAB",
      "AB12 11B",
      "AB12 1A1"
    )

    val invalidMsg = "Invalid post code"

    behave like regexWithValidAndInvalidExamples(postCode, validPostCode, invalidPostCode, invalidMsg, postCodeRegex)
  }

  "postCodeNonUk" must {

    val validPostCode = Table(
      "postCode",
      "1",
      "1234567890",
      "1-2-3",
      "98765434567898765"
    )

    val invalidPostCode = Table(
      "postCode",
      "-",
      "-1",
      "1-"
    )

    val invalidMsg = "Invalid post code"

    behave like regexWithValidAndInvalidExamples(postCodeNonUk, validPostCode, invalidPostCode, invalidMsg, postCodeNonUkRegex)

  }

  "name" must {

    val validName = Table(
      "name",
      "AÀ",
      "a"
    )

    val invalidName = Table(
      "name",
      " A",
      "_A",
      "1A",
      " a",
      "_a",
      "1a",
      "A/",
      "a\\"
    )

    val invalidMsg = "Invalid name"

    behave like regexWithValidAndInvalidExamples(name, validName, invalidName, invalidMsg, nameRegex)
  }

  "addressLine" must {

    val validAddress = Table(
      "address",
      "1 Main St.",
      "Apt/12",
      "—–‐-"
    )

    val invalidAddress = Table(
      "address",
      "Apt [12]",
      "Apt\\12",
      "Street À",
      "Street | 16"
    )

    val invalidMsg = "Invalid address"

    behave like regexWithValidAndInvalidExamples(addressLine, validAddress, invalidAddress, invalidMsg, addressLineRegex)
  }

  "safeText" must {

    val validText = Table(
      "text",
      "some valid text À ÿ",
      "!$%&*()[]@@'~#;:,./?^",
      "s\\as"
    )

    val invalidText = Table(
      "text",
      "{invalid text}",
      "<invalid>",
      "ltd ©"
    )

    val invalidMsg = "Invalid text"

    behave like regexWithValidAndInvalidExamples(safeText, validText, invalidText, invalidMsg, safeTextRegex)
  }

  "country" must {

    val keyInvalid = "error.invalid"

    val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

    "return valid when the country code exists" in {
      val result = country(countryOptions, keyInvalid).apply("GB")
      result shouldBe Valid
    }

    "return invalid when the country code does not exist" in {
      val result = country(countryOptions, keyInvalid).apply("XXX")
      result shouldBe Invalid(keyInvalid)
    }
  }

  "nonFutureDate" must {

    val keyInvalid = "error.invalid"
    "return valid when date is today's date" in {
      nonFutureDate(keyInvalid).apply(LocalDate.now) shouldBe Valid
    }

    "return valid when date is in the past" in {
      nonFutureDate(keyInvalid).apply(LocalDate.now.minusDays(1)) shouldBe Valid
    }

    "return invalid when date is in the future" in {
      nonFutureDate(keyInvalid).apply(LocalDate.now.plusDays(1)) shouldBe Invalid(keyInvalid)
    }

  }

  "companyName" must {
    val validText = Table(
      "text",
      "xyz 2nd's Ltd",
      "xyz/private"
    )

    val invalidText = Table(
      "text",
      "company\\Ltd",
      "[company ltd]",
      "company, Ltd",
      "company Ltd."
    )

    val invalidMsg = "Invalid text"

    behave like regexWithValidAndInvalidExamples(companyName, validText, invalidText, invalidMsg, companyNameRegex)
  }
}
