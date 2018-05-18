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
import forms.mappings.{Constraints, NinoMapping}
import models.Nino
import org.apache.commons.lang3.RandomStringUtils
import play.api.data.{Form, FormError}

class NinoBehaviours extends FormSpec with NinoMapping with Constraints {

  def formWithNino(testForm: Form[Nino]): Unit = {
    "behvae like a form with a NINO mapping" should {
      "fail to bind when yes is selected but NINO is not provided" in {
        val result = testForm.bind(Map("nino.hasNino" -> "true"))
        result.errors shouldBe Seq(FormError("nino.nino", "common.error.nino.required"))
      }

      "fail to bind when no is selected but reason is not provided" in {
        val result = testForm.bind(Map("nino.hasNino" -> "false"))
        result.errors shouldBe Seq(FormError("nino.reason", "directorNino.error.reason.required"))
      }

      Seq("DE999999A", "AO111111B", "ORA12345C", "AB0202020", "AB0303030D", "AB040404E").foreach { nino =>
        s"fail to bind when NINO $nino is invalid" in {
          val result = testForm.bind(Map("nino.hasNino" -> "true", "nino.nino" -> nino))
          result.errors shouldBe Seq(FormError("nino.nino", "common.error.nino.invalid"))
        }
      }

      "fail to bind when no is selected and reason exceeds max length of 160" in {
        val testString = RandomStringUtils.randomAlphabetic(NinoMapping.reasonMaxLength + 1)
        val result = testForm.bind(Map("nino.hasNino" -> "false", "nino.reason" -> testString))
        result.errors shouldBe Seq(FormError("nino.reason", "directorNino.error.reason.length", Seq(NinoMapping.reasonMaxLength)))
      }

      "fail to bind when no is selected and reason contains invalid characters" in {
        val testString = "{invalid reason}"
        val result = testForm.bind(Map("nino.hasNino" -> "false", "nino.reason" -> testString))
        result.errors shouldBe Seq(FormError("nino.reason", "common.error.reason.invalid", Seq(safeTextRegex)))
      }
    }
  }

}
