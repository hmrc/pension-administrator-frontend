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

package forms.register.advisor

import javax.inject.Inject

import forms.mappings.{EmailMapping, PhoneNumberMapping}
import models.register.advisor.AdvisorDetails
import play.api.data.Form
import play.api.data.Forms._

class AdvisorDetailsFormProvider @Inject() extends EmailMapping with PhoneNumberMapping {

  def apply(): Form[AdvisorDetails] = Form(
    mapping(
      "name" -> text("advisorDetails.error.name.required")
        .verifying(
          maxLength(
            AdvisorDetailsFormProvider.nameLength,
            "advisorDetails.error.name.length"
          )
        ),

      "email" -> emailMapping(
        "contactDetails.error.email.required",
        "contactDetails.error.email.length",
        "contactDetails.error.email.invalid"
      ),

      "phone" -> phoneNumberMapping(
        "contactDetails.error.phone.required",
        "contactDetails.error.phone.length",
        "contactDetails.error.phone.invalid"
      )
    )(AdvisorDetails.apply)(AdvisorDetails.unapply)
  )
}

object AdvisorDetailsFormProvider {
  val nameLength: Int = 107
}
