/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import models.PersonName
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class PersonNameFormProviderSpec extends StringFieldBehaviours with Constraints with AnyWordSpecLike {

  val form = new PersonNameFormProvider()()

  // scalastyle:off magic.number
  private val johnDoe = PersonName("John Doherty", "Doe")
  // scalastyle:on magic.number

  ".firstName" must {

    val fieldName = "firstName"
    val requiredKey = "personName.error.firstName.required"
    val lengthKey = "personName.error.firstName.length"
    val invalidKey = "personName.error.firstName.invalid"
    val maxLength = PersonNameFormProvider.firstNameLength

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
        "lastName" -> "Doe"
      ),
      "John",
      (model: PersonName) => model.firstName
    )

  }

  ".lastName" must {

    val fieldName = "lastName"
    val requiredKey = "personName.error.lastName.required"
    val lengthKey = "personName.error.lastName.length"
    val invalidKey = "personName.error.lastName.invalid"
    val maxLength = PersonNameFormProvider.lastNameLength

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
        "lastName" -> " Doe  "
      ),
      "Doe",
      (model: PersonName) => model.lastName
    )
  }

  "PersonNameFormProvider" must {
    "apply PersonName correctly" in {
      val details = form.bind(
        Map(
          "firstName" -> johnDoe.firstName,
          "lastName" -> johnDoe.lastName
        )
      ).get

      details.firstName shouldBe johnDoe.firstName
      details.lastName shouldBe johnDoe.lastName
    }

    "unapply PersonName corectly" in {
      val filled = form.fill(johnDoe)
      filled("firstName").value.value shouldBe johnDoe.firstName
      filled("lastName").value.value shouldBe johnDoe.lastName
    }
  }

}
