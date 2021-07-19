/*
 * Copyright 2021 HM Revenue & Customs
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
import forms.mappings.{RegexBehaviourSpec, UtrMapping}
import play.api.data.{Form, FormError}

class UtrBehaviours extends FormSpec with UtrMapping with RegexBehaviourSpec {

  def formWithUtr(
                   testForm: Form[String],
                   keyUtrRequired: String,
                   keyUtrInvalid: String
                 ): Unit = {

    "fail to bind when form is empty" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors shouldBe Seq(FormError("utr", keyUtrRequired))
    }

    val valid = Table(
      "data",
      Map("utr" -> " 1234567890 ")
    )

    val invalid = Table(
      "data",
      Map("utr" -> "1234567"),
      Map("utr" -> "k12345678901234"),
      Map("utr" -> "A234567890")
    )

    "remove spaces" in {
      val actual = testForm.bind(Map(
        "utr" -> "k  123 456 7890 "))
      actual.errors shouldBe empty
    }

    behave like formWithRegex(testForm, valid, invalid)
  }

}
