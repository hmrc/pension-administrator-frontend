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

package forms.register.company

import forms.mappings.Mappings
import javax.inject.Inject

import models.register.company.CompanyDirector
import play.api.data.{Form, Forms}
import play.api.data.Forms._

class DirectorDetailsFormProvider @Inject() extends Mappings {

  def apply(): Form[CompanyDirector] = Form(
    mapping(
      "firstName" ->
        text("directorDetails.error.firstName.required")
          .verifying(
            maxLength(DirectorDetailsFormProvider.firstNameLength,
              "directorDetails.error.firstName.length"
            )
          ),
      "middleName" -> optional(Forms.text),
      "lastName" ->
        text("directorDetails.error.lastName.required")
          .verifying(
            maxLength(DirectorDetailsFormProvider.lastNameLength,
              "directorDetails.error.lastName.length"
            )
          ),
      "dateOfBirth" -> Forms.localDate
    )(CompanyDirector.apply)(CompanyDirector.unapply)
  )

}

object DirectorDetailsFormProvider {
  val firstNameLength: Int = 35
  val lastNameLength: Int = 35
}
