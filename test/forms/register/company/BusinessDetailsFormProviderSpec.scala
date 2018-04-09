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

class BusinessDetailsFormProviderSpec extends StringFieldBehaviours with Constraints {

  val form = new BusinessDetailsFormProvider()()


  ".companyName" must {

    val fieldName = "companyName"
    val requiredKey = "businessDetails.error.companyName.required"
    val lengthKey = "businessDetails.error.companyName.length"
    val invalidKey = "businessDetails.error.companyName.invalid"
    val maxLength = BusinessDetailsFormProvider.BusinessNameLength

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(safeTextRegex)
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
      "[invalid]",
      error = FormError(fieldName, invalidKey, Seq(safeTextRegex))
    )
  }

  ".utr" must {

    val requiredKey = "businessDetails.error.utr.required"
    val invalid = "businessDetails.error.utr.invalid"
    val fieldName = "utr"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(utrRegex)
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
      FormError(fieldName, invalid, Seq(utrRegex))
    )
  }
}
