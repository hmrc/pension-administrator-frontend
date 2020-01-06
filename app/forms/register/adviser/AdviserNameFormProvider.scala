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

package forms.register.adviser

import forms.mappings.{Mappings, Transforms}
import javax.inject.Inject
import play.api.data.Form

class AdviserNameFormProvider @Inject() extends Mappings with Transforms {

  import AdviserNameFormProvider._

  def apply(): Form[String] = Form(
    "adviserName" -> text(requiredKey).transform(
      standardTextTransform,
      noTransform
    ).verifying(
      firstError(
        maxLength(AdviserNameFormProvider.adviserNameLength, maxLengthKey),
        adviserName(invalidKey)
      )
    )
  )
}

object AdviserNameFormProvider {
  val adviserNameLength = 107

  val requiredKey = "adviserName.error.required"
  val maxLengthKey = "adviserName.error.length"
  val invalidKey = "adviserName.error.invalid"
}



