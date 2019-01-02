/*
 * Copyright 2019 HM Revenue & Customs
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

import java.time.LocalDate

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class IndividualDateOfBirthFormProviderSpec extends StringFieldBehaviours {
  val form = new IndividualDateOfBirthFormProvider()()

  "dateOfBirth" must {

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
          "dateOfBirth.day" -> futureDate.getDayOfMonth.toString,
          "dateOfBirth.month" -> futureDate.getMonthValue.toString,
          "dateOfBirth.year" -> futureDate.getYear.toString
        )
      ).errors shouldBe Seq(FormError(fieldName, "common.error.dateOfBirth.future"))
    }
  }
}
