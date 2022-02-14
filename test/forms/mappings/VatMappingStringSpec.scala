/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.behaviours.VatBehavioursString
import play.api.data.Form

class VatMappingStringSpec extends VatBehavioursString {

  case class VatTestModel(vat: String)

  "VatMapping" must {
    val fieldName = "value"
    val requiredKey = "error.required"
    val lengthKey = "error.length"
    val invalidKey = "error.invalid"
    val form = Form(
      fieldName -> vatMapping(
        "error.required",
        "error.length",
        "error.invalid"
      )
    )
    behave like formWithVatField(
      form,
      fieldName,
      requiredKey,
      lengthKey,
      invalidKey
    )
  }

  "vatRegistrationNumberTransform" must {
    "strip leading, trailing ,and internal spaces" in {
      val actual = vatRegistrationNumberTransform("  123 456 789  ")
      actual shouldBe "123456789"
    }

    "remove leading GB and 2 alpha characters" in {
      val gb = Table(
        "vat",
        "GB123456789",
        "Gb123456789",
        "gB123456789",
        "gb123456789",
        "GBHA123456789"
      )

      forAll(gb) { vat =>
        vatRegistrationNumberTransform(vat) shouldBe "123456789"
      }
    }
  }

}
