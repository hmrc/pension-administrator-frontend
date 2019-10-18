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

package forms

import forms.mappings.UtrMapping
import javax.inject.Inject
import models.UniqueTaxReference
import play.api.data.Form

class UniqueTaxReferenceFormProvider @Inject() extends FormErrorHelper with UtrMapping {

  def apply(requiredKey: String, requiredReasonKey: String): Form[UniqueTaxReference] = {
    val mapping = uniqueTaxReferenceMapping(
      requiredKey = requiredKey,
      requiredUtrKey = "common.error.utr.required",
      utrLengthKey = "common.error.utr.length",
      utrInvalidKey = "common.error.utr.invalid",
      requiredReasonKey = requiredReasonKey,
      reasonLengthKey = "common.error.utr.reason.length"
    )

    Form(
      "utr" -> mapping
    )
  }
}
