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

package forms.behaviours

import forms.FormSpec
import forms.mappings.{Constraints, VatMappingString}
import play.api.data.{Form, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

trait VatBehavioursString extends FormSpec with StringFieldBehaviours with Constraints with VatMappingString {

  def formWithVatField(
                           form: Form[String],
                           fieldName: String,
                           keyVatRequired: String,
                           keyVatLength: String,
                           keyVatInvalid: String
                         ): Unit = {

    "behave like a form with a VAT number" should {

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        RegexpGen.from(vatRegex)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, keyVatRequired)
      )

      behave like fieldWithMaxLength(
        form,
        fieldName,
        maxLength = VatMappingString.maxVatLength,
        lengthError = FormError(fieldName, keyVatLength, Seq(VatMappingString.maxVatLength))
      )

      behave like fieldWithRegex(
        form,
        fieldName,
        "12345678A",
        FormError(fieldName, keyVatInvalid, Seq(vatRegex))
      )

      behave like formWithTransform(
        form,
        Map(fieldName -> "GB123456789"),
        "123456789"
      )
    }
  }

}
