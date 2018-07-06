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

package forms.mappings

import forms.behaviours.UtrBehaviours
import models.UniqueTaxReference
import play.api.data.Form

class UtrMappingSpec extends UtrBehaviours {

  "A form with a UTR" should {
    val mapping = utrMapping(
      requiredKey = "directorUniqueTaxReference.error.required",
      requiredUtrKey = "common.error.utr.required",
      utrLengthKey = "common.error.utr.length",
      utrInvalidKey = "common.error.utr.invalid",
      requiredReasonKey = "directorUniqueTaxReference.error.reason.required",
      reasonLengthKey = "common.error.utr.reason.length"
    )

    val testForm: Form[UniqueTaxReference] = Form("utr" -> mapping)

    behave like formWithUtr(
      testForm,
      keyRequired = "directorUniqueTaxReference.error.required",
      keyUtrRequired = "common.error.utr.required",
      keyReasonRequired = "directorUniqueTaxReference.error.reason.required",
      keyUtrLength = "common.error.utr.length",
      keyReasonLength = "common.error.utr.reason.length"
    )
  }

}
