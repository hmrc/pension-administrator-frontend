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

package forms.register.company.directors

import forms.behaviours.NinoBehaviours

class DirectorNinoFormProviderSpec extends NinoBehaviours {
  private val requiredKey = "directorNino.error.required"
  private val requiredNinoKey = "common.error.nino.required"
  private val requiredReasonKey = "directorNino.error.reason.required"
  private val reasonLengthKey = "directorNino.error.reason.length"
  private val invalidNinoKey = "common.error.nino.invalid"
  private val invalidReasonKey = "common.error.reason.invalid"

  "DirectorNinoFormProviderSpec" should {

    val testForm = new DirectorNinoFormProvider().apply()

    behave like formWithNino(testForm,
      requiredKey,
      requiredNinoKey,
      requiredReasonKey,
      reasonLengthKey,
      invalidNinoKey,
      invalidReasonKey)
  }

}
