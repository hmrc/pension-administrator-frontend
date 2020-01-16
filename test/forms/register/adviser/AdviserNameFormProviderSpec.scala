/*
 * Copyright 2020 HM Revenue & Customs
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

package forms.register.adviser

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class AdviserNameFormProviderSpec extends StringFieldBehaviours with Constraints {

  val form = new AdviserNameFormProvider()()

  ".adviserName" must {

    val fieldName = "adviserName"
    val requiredKey = "adviserName.error.required"
    val maxLenghtErrorKey = "adviserName.error.length"
    val invalidErrorKey = "adviserName.error.invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(adviserNameRegex)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      AdviserNameFormProvider.adviserNameLength,
      FormError(fieldName, maxLenghtErrorKey, Seq(AdviserNameFormProvider.adviserNameLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      "1£234",
      FormError(fieldName, invalidErrorKey, Seq(adviserNameRegex))
    )

    behave like formWithTransform[String](
      form,
      Map(fieldName -> " test "),
      "test"
    )

    behave like formWithRegex(form,
      Table(
        "valid",
        Map("adviserName" -> "Àtestâ -'‘’")
      ),
      Table(
        "invalid",
        Map("adviserName" -> "1234"),
        Map("adviserName" -> "[abcd]"),
        Map("adviserName" -> "{test}")
      )
    )
  }
}
