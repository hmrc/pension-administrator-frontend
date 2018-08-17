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

package forms

import java.time.LocalDate

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import models.PersonDetails
import org.scalatest.Matchers
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class PersonDetailsFormProviderSpec extends StringFieldBehaviours with Constraints with Matchers {

  val form = new PersonDetailsFormProvider()()

  // scalastyle:off magic.number
  private val johnDoe = PersonDetails("John Doherty", None, "Doe", LocalDate.of(1862, 6, 9))
  // scalastyle:on magic.number

  ".firstName" must {

    val fieldName = "firstName"
    val requiredKey = "personDetails.error.firstName.required"
    val lengthKey = "personDetails.error.firstName.length"
    val invalidKey = "personDetails.error.firstName.invalid"
    val maxLength = PersonDetailsFormProvider.firstNameLength

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(nameRegex)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      "1A",
      FormError(fieldName, invalidKey, Seq(nameRegex))
    )

    behave like fieldWithTransform(
      form,
      fieldName,
      Map(
        "firstName" -> "  John ",
        "lastName" -> "Doe",
        "dateOfBirth.day" -> "9",
        "dateOfBirth.month" -> "6",
        "dateOfBirth.year" -> "1862"
      ),
      "John",
      (model: PersonDetails) => model.firstName
    )

  }

  ".middleName" must {

    val fieldName = "middleName"
    val lengthKey = "personDetails.error.middleName.length"
    val invalidKey = "personDetails.error.middleName.invalid"
    val maxLength = PersonDetailsFormProvider.middleNameLength

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(nameRegex)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      "1A",
      FormError(fieldName, invalidKey, Seq(nameRegex))
    )

    behave like optionalField(
      form,
      fieldName,
      Map(
        "firstName" -> "John",
        "middleName" -> "J",
        "lastName" -> "Doe",
        "dateOfBirth.day" -> "9",
        "dateOfBirth.month" -> "6",
        "dateOfBirth.year" -> "1862"
      ),
      (model: PersonDetails) => model.middleName
    )

  }

  ".lastName" must {

    val fieldName = "lastName"
    val requiredKey = "personDetails.error.lastName.required"
    val lengthKey = "personDetails.error.lastName.length"
    val invalidKey = "personDetails.error.lastName.invalid"
    val maxLength = PersonDetailsFormProvider.lastNameLength

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(nameRegex)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      "1A",
      FormError(fieldName, invalidKey, Seq(nameRegex))
    )

    behave like fieldWithTransform(
      form,
      fieldName,
      Map(
        "firstName" -> "John",
        "lastName" -> " Doe  ",
        "dateOfBirth.day" -> "9",
        "dateOfBirth.month" -> "6",
        "dateOfBirth.year" -> "1862"
      ),
      "Doe",
      (model: PersonDetails) => model.lastName
    )
  }

  ".dateOfBirth" must {

    val fieldName = "dateOfBirth"
    val requiredKey = "common.error.dateOfBirth.required"
    val invalidKey = "common.error.dateOfBirth.invalid"

    behave like dateFieldThatBindsValidData(
      form,
      fieldName,
      historicDate()
    )

    behave like mandatoryDateField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "only accept numeric input" in {
      form.bind(
        Map(
          "firstName" -> johnDoe.firstName,
          "lastName" -> johnDoe.lastName,
          "dateOfBirth.day" -> "A",
          "dateOfBirth.month" -> "A",
          "dateOfBirth.year" -> "A"
        )
      ).errors should contain allOf(
        FormError("dateOfBirth.day", "error.date.day_invalid"),
        FormError("dateOfBirth.month", "error.date.month_invalid"),
        FormError("dateOfBirth.year", "error.date.year_invalid")
      )
    }

    "only accept inputs that are a valid date" in {
      form.bind(
        Map(
          "firstName" -> johnDoe.firstName,
          "lastName" -> johnDoe.lastName,
          "dateOfBirth.day" -> "32",
          "dateOfBirth.month" -> "13",
          "dateOfBirth.year" -> "0"
        )
      ).errors shouldBe Seq(FormError(fieldName, invalidKey))
    }

    val futureDate = LocalDate.now().plusDays(1)
    "not accept a future date" in {
      form.bind(
        Map(
          "firstName" -> johnDoe.firstName,
          "lastName" -> johnDoe.lastName,
          "dateOfBirth.day" -> futureDate.getDayOfMonth.toString,
          "dateOfBirth.month" -> futureDate.getMonthValue.toString,
          "dateOfBirth.year" -> futureDate.getYear.toString
        )
      ).errors shouldBe Seq(FormError(fieldName, "common.error.dateOfBirth.future"))
    }

  }

  "PersonDetailsFormProvider" must {
    "apply PersonDetails correctly" in {
      val details = form.bind(
        Map(
          "firstName" -> johnDoe.firstName,
          "lastName" -> johnDoe.lastName,
          "dateOfBirth.day" -> johnDoe.dateOfBirth.getDayOfMonth.toString,
          "dateOfBirth.month" -> johnDoe.dateOfBirth.getMonthValue.toString,
          "dateOfBirth.year" -> johnDoe.dateOfBirth.getYear.toString
        )
      ).get

      details.firstName shouldBe johnDoe.firstName
      details.lastName shouldBe johnDoe.lastName
      details.dateOfBirth shouldBe johnDoe.dateOfBirth
    }

    "unapply PersonDetails corectly" in {
      val filled = form.fill(johnDoe)
      filled("firstName").value.value shouldBe johnDoe.firstName
      filled("lastName").value.value shouldBe johnDoe.lastName
      filled("dateOfBirth.day").value.value shouldBe johnDoe.dateOfBirth.getDayOfMonth.toString
      filled("dateOfBirth.month").value.value shouldBe johnDoe.dateOfBirth.getMonthValue.toString
      filled("dateOfBirth.year").value.value shouldBe johnDoe.dateOfBirth.getYear.toString
    }
  }

}
