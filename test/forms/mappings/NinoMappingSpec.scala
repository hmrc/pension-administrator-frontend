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

import forms.behaviours.NinoBehaviours
import models.Nino
import play.api.data.Form

class NinoMappingSpec extends NinoBehaviours {

  private val requiredKey = "error.required"
  private val requiredNinoKey = "error.nino.required"
  private val requiredReasonKey = "error.reason.required"
  private val reasonLengthKey = "error.reason.length"
  private val invalidNinoKey = "error.nino.invalid"
  private val invalidReasonKey = "error.reason.invalid"

  "A form with a Nino" should {
    val mapping = ninoMapping(
      requiredKey,
      requiredNinoKey,
      requiredReasonKey,
      reasonLengthKey,
      invalidNinoKey,
      invalidReasonKey
    )

    val testForm: Form[Nino] = Form("nino" -> mapping)

    behave like formWithNino(testForm,
      requiredKey,
      requiredNinoKey,
      requiredReasonKey,
      reasonLengthKey,
      invalidNinoKey,
      invalidReasonKey
    )
  }

}
