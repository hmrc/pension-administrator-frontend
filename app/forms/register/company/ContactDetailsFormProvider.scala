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

import javax.inject.Inject

import forms.mappings.Mappings
import models.register.company.ContactDetails
import play.api.data.Form
import play.api.data.Forms._

class ContactDetailsFormProvider @Inject() extends Mappings {

   def apply(): Form[ContactDetails] = Form(
     mapping(
      "email" -> text("contactDetails.error.email.required")
        .verifying(returnOnFirstFailure(
          maxLength(132, "contactDetails.error.email.length"),
          emailAddress("contactDetails.error.email.invalid")
        )),
      "phone" -> text("contactDetails.error.phone.required")
        .verifying(returnOnFirstFailure(
          maxLength(24, "contactDetails.error.phone.length"),
          wholeNumber("contactDetails.error.phone.invalid")
        ))
    )(ContactDetails.apply)(ContactDetails.unapply)
   )
 }
