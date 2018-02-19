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

import javax.inject.Inject

import forms.mappings.Mappings
import models.Address
import play.api.data.{Form, Forms}
import play.api.data.Forms.{mapping, optional}

class AddressFormProvider @Inject() extends Mappings {

  val addressLineMaxLength = 35

  def apply(): Form[Address] = Form(
    mapping(
      "addressLine1" -> text("error.address_line_1.required").verifying(maxLength(addressLineMaxLength, "error.address_line_1.length")),
      "addressLine2" -> text("error.address_line_2.required").verifying(maxLength(addressLineMaxLength, "error.address_line_2.length")),
      "addressLine3" -> optional(Forms.text.verifying(maxLength(addressLineMaxLength, "error.address_line_3.length"))),
      "addressLine4" -> optional(Forms.text.verifying(maxLength(addressLineMaxLength, "error.address_line_4.length"))),
      "postCode" -> postCode("error.postcode.required", "error.postcode.invalid"),
      "country" -> text("error.country.required")
    )(Address.apply)(Address.unapply)
  )
}
