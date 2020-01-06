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

package forms.register.individual

import java.time.LocalDate

import forms.FormErrorHelper
import forms.mappings.Mappings
import javax.inject.Inject
import play.api.data.Form

class IndividualDateOfBirthFormProvider @Inject() extends FormErrorHelper with Mappings {

  def apply(): Form[LocalDate] =
    Form(
      "dateOfBirth" -> date("common.error.dateOfBirth.required", "common.error.dateOfBirth.invalid")
        .verifying(firstError(
          nonFutureDate("common.error.dateOfBirth.future"),
          notBeforeYear("common.error.dateOfBirth.past", IndividualDateOfBirthFormProvider.startYear)
        ))
    )
}

object IndividualDateOfBirthFormProvider {
  val startYear: Int = 1900
}