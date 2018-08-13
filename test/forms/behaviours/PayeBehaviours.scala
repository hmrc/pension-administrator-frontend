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
import forms.mappings.{PayeMapping, RegexBehaviourSpec}
import models.Paye
import org.apache.commons.lang3.RandomStringUtils
import play.api.data.{Form, FormError}

class PayeBehaviours extends FormSpec with PayeMapping with RegexBehaviourSpec {

  def formWithPaye(
                    testForm: Form[Paye],
                    requiredKey: String,
                    keyPayeRequired: String,
                    keyPayeLength: String
                  ): Unit = {

    "fail to bind when yes is selected but paye is not provided" in {
      val result = testForm.bind(Map("paye.hasPaye" -> "true"))
      result.errors shouldBe Seq(FormError("paye.paye", keyPayeRequired))
    }

    "fail to bind when yes is selected and paye exceeds max length of 16" in {
      val testString = RandomStringUtils.randomAlphabetic(PayeMapping.maxPayeLength + 1)
      val result = testForm.bind(Map("paye.hasPaye" -> "true", "paye.paye" -> testString))
      result.errors shouldBe Seq(FormError("paye.paye", keyPayeLength, Seq(PayeMapping.maxPayeLength)))
    }

    "fail to bind when value is omitted" in {
      val expectedError = error("paye.hasPaye", requiredKey)
      checkForError(testForm, emptyForm, expectedError)
    }

    "successfully unbind `Paye.Yes`" in {
      val result = testForm.fill(Paye.Yes("paye")).data
      result should contain("paye.hasPaye" -> "true")
      result should contain("paye.paye" -> "paye")
    }

    "successfully unbind `Paye.No`" in {
      val result = testForm.fill(Paye.No).data
      result should contain("paye.hasPaye" -> "false")
    }

    val valid = Table(
      "data",
      Map("paye.hasPaye" -> "true", "paye.paye" -> " 123/AB56789 "),
      Map("paye.hasPaye" -> "true", "paye.paye" -> " 123AB56789 "),
      Map("paye.hasPaye" -> "true", "paye.paye" -> " 123\\AB56789 ")
    )

    val invalid = Table(
      "data",
      Map("paye.hasPaye" -> "true", "paye.paye" -> "A1_"),
      Map("paye.hasPaye" -> "true", "paye.paye" -> "ABC1234567897"),
      Map("paye.hasPaye" -> "true", "paye.paye" -> "123")
    )

    behave like formWithRegex(testForm, valid, invalid)
  }

}
