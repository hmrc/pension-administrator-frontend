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

package forms.mappings

import forms.behaviours.StringFieldBehaviours
import play.api.data.{Form, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

class BusinessNameMappingSpec extends StringFieldBehaviours with BusinessNameMapping {

  private val requiredKey = "common.radio.error.required"
  private val businessNameLengthKey = "common.error.vat.length"
  private val invalidBusinessNameKey = "common.error.vat.invalid"

  "BusinessNameMapping " should {
    val mapping = nameMapping(
      requiredKey,
      invalidBusinessNameKey,
      businessNameLengthKey
    )
    val fieldName = "value"

    val form: Form[String] = Form("value" -> mapping)

    "behave like a mapping with a business name field" should {
      behave like fieldThatBindsValidData(
        form,
        fieldName,
        RegexpGen.from(businessNameRegex)
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, requiredKey)
      )

      behave like formWithTransform(
        form,
        Map(fieldName -> " test business name "),
        "test business name"
      )

      behave like fieldWithMaxLength(
        form,
        fieldName,
        maxLength = BusinessNameMapping.maxLength,
        lengthError = FormError(fieldName, businessNameLengthKey, Seq(BusinessNameMapping.maxLength))
      )

      behave like fieldWithRegex(
        form,
        fieldName,
        "[invalid]",
        FormError(fieldName, invalidBusinessNameKey, Seq(businessNameRegex))
      )
    }

  }

}
