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
import forms.mappings.{Constraints, EmailMapping}
import play.api.data.{Form, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

trait EmailBehaviours extends FormSpec with StringFieldBehaviours with Constraints with EmailMapping {

  def formWithEmailField(
    form: Form[_],
    fieldName: String,
    keyEmailRequired: String,
    keyEmailLength: String,
    keyEmailInvalid: String
  ): Unit = {

    "behave like a form with an email field" should {

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        "ab@test.com"
      )

      behave like fieldWithMaxLength(
        form,
        fieldName,
        maxLength = EmailMapping.maxEmailLength,
        lengthError = FormError(fieldName, keyEmailLength, Seq(EmailMapping.maxEmailLength))
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, keyEmailRequired)
      )

      behave like fieldWithRegex(
        form,
        fieldName,
        "ABC",
        FormError(fieldName, keyEmailInvalid, Seq(emailRestrictiveRegex))
      )

    }
  }

}
