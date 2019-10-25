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

import forms.mappings.{Mappings, Transforms}
import javax.inject.Inject
import models.PersonDetails
import play.api.data.Form
import play.api.data.Forms._

class PersonDetailsFormProvider @Inject() extends Mappings with Transforms {

  def apply(): Form[PersonDetails] = Form(
    mapping(
      "firstName" ->
        text("personName.error.firstName.required")
          .verifying(
            firstError(
              maxLength(PersonDetailsFormProvider.firstNameLength,
                "personName.error.firstName.length"
              ),
              name("personName.error.firstName.invalid")
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
        text("personName.error.lastName.required")
          .verifying(
            firstError(
              maxLength(PersonDetailsFormProvider.lastNameLength,
                "personName.error.lastName.length"
              ),
              name("personName.error.lastName.invalid")
            )
          ),
      "dateOfBirth" -> date("common.error.dateOfBirth.required", "common.error.dateOfBirth.invalid")
        .verifying(firstError(
          nonFutureDate("common.error.dateOfBirth.future"),
          notBeforeYear("common.error.dateOfBirth.past", PersonDetailsFormProvider.startYear)
        )
          )
    )(PersonDetails.applyDelete)(PersonDetails.unapplyDelete)
  )

}

object PersonDetailsFormProvider {
  val firstNameLength: Int = 35
  val middleNameLength: Int = 35
  val lastNameLength: Int = 35
  val startYear: Int = 1900
}
