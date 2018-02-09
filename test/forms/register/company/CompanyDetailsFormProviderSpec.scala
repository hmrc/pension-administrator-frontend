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
import org.scalatest.OptionValues
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class CompanyDetailsFormProviderSpec extends StringFieldBehaviours with Constraints with OptionValues {

  val form = new CompanyDetailsFormProvider()()

  ".companyName" must {

    val fieldName = "companyName"
    val requiredKey = "companyDetails.error.companyName.required"
    val lengthKey = "companyDetails.error.companyName.length"
    val maxLength = CompanyDetailsFormProvider.companyNameLength

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
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
  }

  ".vatRegistrationNumber" must {

    val fieldName = "vatRegistrationNumber"
    val lengthKey = "companyDetails.error.vatRegistrationNumber.length"
    val maxLength = CompanyDetailsFormProvider.vatRegistrationNumberLength
    val invalid = "companyDetails.error.vatRegistrationNumber.invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(vat)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      "12345678A",
      FormError(fieldName, invalid, Seq(vat))
    )

    "transform VAT Registration Number" in {
      val data = Map(
        "companyName" -> "MyCo Ltd",
        "vatRegistrationNumber" -> "  GB123456789  "
      )

      val result = form.bind(data)
      result.errors.size shouldBe 0
      result.get.vatRegistrationNumber.value shouldBe "123456789"
    }
  }

  ".payeEmployerReferenceNumber" must {

    val fieldName = "payeEmployerReferenceNumber"
    val lengthKey = "companyDetails.error.payeEmployerReferenceNumber.length"
    val maxLength = CompanyDetailsFormProvider.payeEmployerReferenceNumberLength
    val invalid = "companyDetails.error.payeEmployerReferenceNumber.invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      RegexpGen.from(paye)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like fieldWithRegex(
      form,
      fieldName,
      "A1_",
      FormError(fieldName, invalid, Seq(paye))
    )
  }

}
