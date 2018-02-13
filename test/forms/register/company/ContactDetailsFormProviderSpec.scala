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

package forms.register.company

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class ContactDetailsFormProviderSpec extends StringFieldBehaviours with Constraints{

  val form = new ContactDetailsFormProvider()()

  ".email" must {

    val fieldName = "email"
    val requiredKey = "contactDetails.error.email.required"
    val lengthKey = "contactDetails.error.email.length"
    val maxLength = 132
    val testRegexString = """^[^@<>]{1,65}@[^@<>]{1,65}$"""

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(testRegexString)
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
      "ABC",
      FormError(fieldName, "contactDetails.error.email.invalid", Seq(email))
    )

  }

  ".phone" must {

    val fieldName = "phone"
    val requiredKey = "contactDetails.error.phone.required"
    val lengthKey = "contactDetails.error.phone.length"
    val maxLength = 24

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      numbersWithMaxLength(maxLength)
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
      "ABC",
      FormError(fieldName, "contactDetails.error.phone.invalid", Seq(number))
    )
  }
}
