/*
 * Copyright 2021 HM Revenue & Customs
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

package forms.register.individual

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import models.TolerantIndividual
import org.scalatest.Matchers
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class IndividualNameFormProviderSpec extends StringFieldBehaviours with Constraints with Matchers {

  val form = new IndividualNameFormProvider()()

  private val johnDoe = TolerantIndividual(Some("John Doherty"), None, Some("Doe"))

  ".firstName" must {

    val fieldName = "firstName"
    val requiredKey = "individualName.error.firstName.required"
    val lengthKey = "individualName.error.firstName.length"
    val invalidKey = "individualName.error.firstName.invalid"
    val maxLength = IndividualNameFormProvider.firstNameLength

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
      Some("John"),
      (model: TolerantIndividual) => model.firstName
    )

  }

  ".lastName" must {

    val fieldName = "lastName"
    val requiredKey = "individualName.error.lastName.required"
    val lengthKey = "individualName.error.lastName.length"
    val invalidKey = "individualName.error.lastName.invalid"
    val maxLength = IndividualNameFormProvider.lastNameLength

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
      Some("Doe"),
      (model: TolerantIndividual) => model.lastName
    )
  }

  "IndividualNameFormProvider" must {

    "applyNonUK TolerantIndividual correctly" in {
      val details = form.bind(
        Map(
          "firstName" -> johnDoe.firstName.get,
          "lastName" -> johnDoe.lastName.get
        )
      ).get

      details.firstName shouldBe johnDoe.firstName
      details.lastName shouldBe johnDoe.lastName
    }

    "unapplyNonUK TolerantIndividual corectly" in {
      val filled = form.fill(johnDoe)
      filled("firstName").value shouldBe johnDoe.firstName
      filled("lastName").value shouldBe johnDoe.lastName
    }
  }

}
