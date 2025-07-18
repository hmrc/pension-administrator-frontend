/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.mappings.{Constraints, CrnMapping}
import play.api.data.{Form, FormError}

trait CrnBehaviours extends FormSpec with StringFieldBehaviours with Constraints with CrnMapping {

  def formWithCrnField(
                        form: Form[?],
                        fieldName: String,
                        keyCrnRequired: String,
                        keyCrnInvalid: String
                      ): Unit = {

    "behave like a form with a CRN field" should {

      behave like fieldThatBindsValidData(
        form,
        fieldName,
        " 1234 5678 "
      )

      behave like mandatoryField(
        form,
        fieldName,
        requiredError = FormError(fieldName, keyCrnRequired)
      )

      behave like fieldWithRegex(
        form,
        fieldName,
        "ABC",
        FormError(fieldName, keyCrnInvalid, Seq(crnRegex))
      )

      "Remove spaces and convert to upper case" in {
        val result = form.bind(Map(fieldName -> " 12 3a5 6 78 "))
        result.errors shouldBe empty
        result.value shouldBe Some("123A5678")
      }

    }

  }

}
