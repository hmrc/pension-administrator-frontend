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

package forms.behaviours

import forms.FormSpec
import forms.mappings.{Constraints, PhoneNumberMapping}
import play.api.data.{Form, FormError}

trait PhoneNumberBehaviours extends FormSpec with StringFieldBehaviours with Constraints with PhoneNumberMapping {

  def formWithPhoneNumberField(
                          form: Form[_],
                          fieldName: String,
                          keyPhoneNumberRequired: String,
                          keyPhoneNumberLength: String,
                          keyPhoneNumberInvalid: String
                        ): Unit = {

    "behave like a form with a phone number field" should {
      behave like fieldThatBindsValidData(
        form,
        fieldName,
        numbersWithMaxLength(PhoneNumberMapping.maxPhoneNumberLength)
      )

      behave like fieldWithMaxLength(
        form,
        fieldName,
        maxLength = PhoneNumberMapping.maxPhoneNumberLength,
        lengthError = FormError(fieldName, keyPhoneNumberLength, Seq(PhoneNumberMapping.maxPhoneNumberLength))
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, keyPhoneNumberRequired)
      )

      behave like fieldWithRegex(
        form,
        fieldName,
        "ABC",
        FormError(fieldName, keyPhoneNumberInvalid, Seq(phoneNumberRegex))
      )

    }
  }

}
