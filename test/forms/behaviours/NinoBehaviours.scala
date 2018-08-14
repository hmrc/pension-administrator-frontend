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
import generators.Generators
import models.Nino
import org.scalatest.prop.PropertyChecks
import play.api.data.{Form, FormError}

class NinoBehaviours extends FormSpec with NinoMapping with Constraints with Generators with PropertyChecks {
  val reasonMaxLength = 160

  def formWithNino(testForm: Form[Nino],
                   requiredKey: String,
                   requiredNinoKey: String,
                   requiredReasonKey: String,
                   reasonLengthKey: String,
                   invalidNinoKey: String,
                   invalidReasonKey: String
                  ): Unit = {
    "behave like a form with a NINO Mapping" should {

      "fail to bind when yes is selected but NINO is not provided" in {
        val result = testForm.bind(Map("nino.hasNino" -> "true"))
        result.errors shouldBe Seq(FormError("nino.nino", requiredNinoKey))
      }

      "fail to bind when no is selected but reason is not provided" in {
        val result = testForm.bind(Map("nino.hasNino" -> "false"))
        result.errors shouldBe Seq(FormError("nino.reason", requiredReasonKey))
      }

      Seq("DE999999A", "AO111111B", "ORA12345C", "AB0202020", "AB0303030D", "AB040404E").foreach { nino =>
        s"fail to bind when NINO $nino is invalid" in {
          val result = testForm.bind(Map("nino.hasNino" -> "true", "nino.nino" -> nino))
          result.errors shouldBe Seq(FormError("nino.nino", invalidNinoKey))
        }
      }

      "fail to bind when no is selected and reason exceeds max length of 160" in {
        forAll(stringsLongerThan(reasonMaxLength) -> "longerString") {
          string =>
            val result = testForm.bind(Map("nino.hasNino" -> "false", "nino.reason" -> string))
            result.errors shouldBe Seq(FormError("nino.reason", reasonLengthKey, Seq(reasonMaxLength)))
        }
      }

      "fail to bind when reason is invalid" in {
        val result = testForm.bind(Map("nino.hasNino" -> "false", "nino.reason" -> "{reason}"))
        result.errors shouldBe Seq(FormError("nino.reason", invalidReasonKey, Seq(safeTextRegex)))
      }


      Seq("AB020202A", " a b 0 2 0 2 0 2 a ").foreach {
        validNino =>
          s"successfully bind when yes is selected and valid NINO $validNino is provided" in {
            val form = testForm.bind(Map("nino.hasNino" -> "true", "nino.nino" -> validNino))
            form.get shouldEqual Nino.Yes("AB020202A")
          }
      }

      "successfully bind when no is selected and reason is provided" in {
        val form = testForm.bind(Map("nino.hasNino" -> "false", "nino.reason" -> "haven't got Nino"))
        form.get shouldBe Nino.No("haven't got Nino")
      }

      "fail to bind when value is omitted" in {
        val expectedError = error("nino.hasNino", requiredKey)
        checkForError(testForm, emptyForm, expectedError)
      }

      "successfully unbind `Nino.Yes`" in {
        val result = testForm.fill(Nino.Yes("nino")).data
        result should contain("nino.hasNino" -> "true")
        result should contain("nino.nino" -> "nino")
      }

      "successfully unbind `Nino.No`" in {
        val result = testForm.fill(Nino.No("reason")).data
        result should contain("nino.hasNino" -> "false")
        result should contain("nino.reason" -> "reason")
      }
    }
  }

}
