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

import forms.mappings.{Mappings, Transforms}
import javax.inject.Inject
import models.TolerantIndividual
import play.api.data.Form
import play.api.data.Forms._

class IndividualNameFormProvider @Inject() extends Mappings with Transforms {

  def apply(): Form[TolerantIndividual] = Form(
    mapping(
      "firstName" ->
        text("individualName.error.firstName.required")
          .verifying(
            firstError(
              maxLength(IndividualNameFormProvider.firstNameLength,
                "individualName.error.firstName.length"
              ),
              name("individualName.error.firstName.invalid")
            )
          ),
      "lastName" ->
        text("individualName.error.lastName.required")
          .verifying(
            firstError(
              maxLength(IndividualNameFormProvider.lastNameLength,
                "individualName.error.lastName.length"
              ),
              name("individualName.error.lastName.invalid")
            )
          )
    )(TolerantIndividual.applyNonUK)(TolerantIndividual.unapplyNonUK)
  )

}

object IndividualNameFormProvider {
  val firstNameLength: Int = 35
  val lastNameLength: Int = 35
}

