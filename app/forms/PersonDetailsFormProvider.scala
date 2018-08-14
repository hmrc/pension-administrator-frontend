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

package forms

import forms.mappings.{Mappings, Transforms}
import javax.inject.Inject
import models.PersonDetails
import play.api.data.Form
import play.api.data.Forms._

class PersonDetailsFormProvider @Inject() extends Mappings with Transforms {

  def apply(): Form[PersonDetails] = Form(
    mapping(
      "firstName" ->
        text("personDetails.error.firstName.required")
          .verifying(
            firstError(
              maxLength(PersonDetailsFormProvider.firstNameLength,
                "personDetails.error.firstName.length"
              ),
              name("personDetails.error.firstName.invalid")
            )
          ),
      "middleName" ->
        optionalText()
          .verifying(
            firstError(
              maxLength(PersonDetailsFormProvider.middleNameLength,
                "personDetails.error.middleName.length"
              ),
              name("personDetails.error.middleName.invalid")
            )
          ),
      "lastName" ->
        text("personDetails.error.lastName.required")
          .verifying(
            firstError(
              maxLength(PersonDetailsFormProvider.lastNameLength,
                "personDetails.error.lastName.length"
              ),
              name("personDetails.error.lastName.invalid")
            )
          ),
      "dateOfBirth" -> date("common.error.dateOfBirth.required", "common.error.dateOfBirth.invalid")
        .verifying(nonFutureDate("common.error.dateOfBirth.future"))
    )(PersonDetails.applyDelete)(PersonDetails.unapplyDelete)
  )

}

object PersonDetailsFormProvider {
  val firstNameLength: Int = 35
  val middleNameLength: Int = 35
  val lastNameLength: Int = 35
}
