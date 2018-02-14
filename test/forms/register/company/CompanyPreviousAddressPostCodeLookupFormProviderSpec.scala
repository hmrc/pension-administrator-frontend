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

class CompanyPreviousAddressPostCodeLookupFormProviderSpec extends StringFieldBehaviours with Constraints {

  val requiredKey = "companyPreviousAddressPostCodeLookup.error.required"
  val lengthKey = "companyPreviousAddressPostCodeLookup.error.length"
  val invalid = "companyPreviousAddressPostCodeLookup.error.invalid"
  val maxLength = CompanyPreviousAddressPostCodeLookupFormProvider.PostCodeLength
  val fieldName = "value"

  val form = new CompanyPreviousAddressPostCodeLookupFormProvider()()

  ".value" must {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(postcode)
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
      "value",
      "1B12 1AB",
      FormError(fieldName, invalid, Seq(postcode))
    )

    behave like fieldWithTransform(
      form,
      s"$fieldName pre-validate",
      Map(fieldName -> " AB12 1AB "),
      "AB12 1AB",
      (actual: String) => actual
    )

    behave like fieldWithTransform(
      form,
      s"$fieldName post-validate",
      Map(fieldName -> "AB121AB"),
      "AB12 1AB",
      (actual: String) => actual
    )
  }

}
