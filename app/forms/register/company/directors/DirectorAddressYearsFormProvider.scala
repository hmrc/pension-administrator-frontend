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

package forms.register.company.directors

import javax.inject.Inject

import forms.FormErrorHelper
import forms.mappings.Mappings
import models.AddressYears
import play.api.data.Form

class DirectorAddressYearsFormProvider @Inject() extends FormErrorHelper with Mappings {

  def apply(): Form[AddressYears] =
    Form(
      "value" -> enumerable[AddressYears]("directorAddressYears.error.required")
    )
}
