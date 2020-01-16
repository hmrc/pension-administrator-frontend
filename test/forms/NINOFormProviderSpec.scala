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

package forms

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import forms.register.NINOFormProvider
import models.ReferenceValue
import play.api.data.FormError
import viewmodels.Message

class NINOFormProviderSpec extends FormSpec with StringFieldBehaviours with Constraints {

  private val entityName = "Test Name"
  private val form = new NINOFormProvider()(entityName)

  "nino" must {

    val fieldName = "value"
    val requiredKey = Message("enterNINO.error.required").withArgs(entityName)
    val invalidKey = Message("enterNINO.error.invalid").withArgs(entityName)

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like formWithTransform(
      form,
      Map(fieldName -> "ab 1001 00a "),
      expectedData = ReferenceValue("AB100100A")
    )

    "successfully bind for a valid nino" in {
      val res = form.bind(Map(fieldName -> "AB020202A"))
      res.get.value shouldEqual "AB020202A"
    }

    Seq("DE999999A", "AO111111B", "ORA12345C", "AB0202020", "AB0303030D", "AB040404E").foreach { nino =>
      s"fail to bind when NINO $nino is invalid" in {
        val result = form.bind(Map(fieldName -> nino))
        result.errors shouldBe Seq(FormError(fieldName, invalidKey))
      }
    }
  }
}
