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

import forms.FormErrorHelper
import forms.mappings.NinoMapping
import javax.inject.Inject
import models.Nino
import play.api.data.Form

class DirectorNinoFormProvider @Inject() extends FormErrorHelper with NinoMapping {

  def apply(): Form[Nino] =
    Form(
      "nino" -> ninoMapping(
        requiredKey = "directorNino.error.required",
        requiredReasonKey = "directorNino.error.reason.required",
        reasonLengthKey = "directorNino.error.reason.length"
      )
    )

}
