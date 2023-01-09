/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.data.FormError

import java.time.LocalDate

class DOBFormProviderSpec extends StringFieldBehaviours with Constraints {

  val form = new DOBFormProvider()()

  // scalastyle:off magic.number
  private val date = LocalDate.of(1947, 6, 9)
  // scalastyle:on magic.number

  ".dateOfBirth" must {

    val fieldName = "value"
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
          "value.day" -> "A",
          "value.month" -> "A",
          "value.year" -> "A"
        )
      ).errors should contain.allOf(
        FormError("value.day", "error.date.day_invalid"),
        FormError("value.month", "error.date.month_invalid"),
        FormError("value.year", "error.date.year_invalid")
      )
    }

    "only accept inputs that are a valid date" in {
      form.bind(
        Map(
          "value.day" -> "32",
          "value.month" -> "13",
          "value.year" -> "0"
        )
      ).errors shouldBe Seq(FormError(fieldName, invalidKey))
    }

    val futureDate = LocalDate.now().plusDays(1)
    "not accept a future date" in {
      form.bind(
        Map(
          "value.day" -> futureDate.getDayOfMonth.toString,
          "value.month" -> futureDate.getMonthValue.toString,
          "value.year" -> futureDate.getYear.toString
        )
      ).errors shouldBe Seq(FormError(fieldName, "common.error.dateOfBirth.future"))
    }

    "not accept a year before 1900" in {
      form.bind(
        Map(
          "value.day" -> "1",
          "value.month" -> "1",
          "value.year" -> "1899"
        )
      ).errors shouldBe Seq(FormError(fieldName, "common.error.dateOfBirth.past"))
    }
  }

  "DOBFormProvider" must {
    "apply PersonDetails correctly" in {
      val details = form.bind(
        Map(
          "value.day" -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year" -> date.getYear.toString
        )
      ).get

      details shouldBe date
    }

    "unapply PersonDetails corectly" in {
      val filled = form.fill(date)
      filled("value.day").value.value shouldBe date.getDayOfMonth.toString
      filled("value.month").value.value shouldBe date.getMonthValue.toString
      filled("value.year").value.value shouldBe date.getYear.toString
    }
  }

}
