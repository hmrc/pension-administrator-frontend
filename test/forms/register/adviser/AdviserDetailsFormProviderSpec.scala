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

package forms.register.adviser

import forms.behaviours.{EmailBehaviours, PhoneNumberBehaviours, StringFieldBehaviours}
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class AdviserDetailsFormProviderSpec extends StringFieldBehaviours with EmailBehaviours with PhoneNumberBehaviours {

  val form = new AdviserDetailsFormProvider()()

  ".name" must {

    val fieldName = "name"
    val requiredKey = "adviserDetails.error.name.required"
    val lengthKey = "adviserDetails.error.name.length"
    val invalidKey = "adviserDetails.error.name.invalid"
    val maxLength = AdviserDetailsFormProvider.nameLength

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(adviserNameRegex)
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
      "test<name",
      FormError(fieldName, invalidKey, Seq(adviserNameRegex))
    )

  }

  ".email" must {

    val fieldName = "email"
    val keyEmailRequired = "contactDetails.error.email.required"
    val keyEmailLength = "contactDetails.error.email.length"
    val keyEmailInvalid = "contactDetails.error.email.invalid"

    behave like formWithEmailField(
      form,
      fieldName,
      keyEmailRequired,
      keyEmailLength,
      keyEmailInvalid
    )

  }

  ".phoneNumber" must {
    val fieldName = "phone"
    val keyPhoneNumberRequired = "contactDetails.error.phone.required"
    val keyPhoneNumberLength = "contactDetails.error.phone.length"
    val keyPhoneNumberInvalid = "contactDetails.error.phone.invalid"

    behave like formWithPhoneNumberField(
      form,
      fieldName,
      keyPhoneNumberRequired,
      keyPhoneNumberLength,
      keyPhoneNumberInvalid
    )

  }
}
