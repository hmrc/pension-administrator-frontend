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

import forms.mappings.{Mappings, Transforms}
import models.register.company.directors.DirectorDetails
import play.api.data.Forms._
import play.api.data.{Form, Forms}

class DirectorDetailsFormProvider @Inject() extends Mappings with Transforms {

  def apply(): Form[DirectorDetails] = Form(
    mapping(
      "firstName" ->
        text("directorDetails.error.firstName.required")
          .transform(standardTextTransform, noTransform)
          .verifying(
            firstError(
              maxLength(DirectorDetailsFormProvider.firstNameLength,
                "directorDetails.error.firstName.length"
              ),
              name("directorDetails.error.firstName.invalid")
            )
          ),
      "middleName" -> optional(
        Forms.text
          .transform(standardTextTransform, noTransform)
          .verifying(
            firstError(
              maxLength(DirectorDetailsFormProvider.middleNameLength,
                "directorDetails.error.middleName.length"
              ),
              name("directorDetails.error.middleName.invalid")
            )
          )
      ),
      "lastName" ->
        text("directorDetails.error.lastName.required")
          .transform(standardTextTransform, noTransform)
          .verifying(
            firstError(
              maxLength(DirectorDetailsFormProvider.lastNameLength,
                "directorDetails.error.lastName.length"
              ),
              name("directorDetails.error.lastName.invalid")
            )
          ),
      "dateOfBirth" -> date("directorDetails.error.dateOfBirth.required", "directorDetails.error.dateOfBirth.invalid")
    )(DirectorDetails.apply)(DirectorDetails.unapply)
  )

}

object DirectorDetailsFormProvider {
  val firstNameLength: Int = 35
  val middleNameLength: Int = 35
  val lastNameLength: Int = 35
}
