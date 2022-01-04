/*
 * Copyright 2022 HM Revenue & Customs
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

package forms.address

import forms.mappings.AddressMapping
import javax.inject.Inject
import models.Address
import play.api.data.Form
import play.api.data.Forms.mapping
import utils.countryOptions.CountryOptions

class NonUKAddressFormProvider @Inject()(countryOptions: CountryOptions) extends AddressMapping {

  def apply(requiredCountry: String = "error.country.invalid"): Form[Address] = Form(
    mapping(
      "addressLine1" -> addressLineMapping("error.address_line_1.required", "error.address_line_1.length", "error.address_line_1.invalid"),
      "addressLine2" -> addressLineMapping("error.address_line_2.required", "error.address_line_2.length", "error.address_line_2.invalid"),
      "addressLine3" -> optionalAddressLineMapping("error.address_line_3.length", "error.address_line_3.invalid"),
      "addressLine4" -> optionalAddressLineMapping("error.address_line_4.length", "error.address_line_4.invalid"),
      "country" -> countryMapping(countryOptions, requiredCountry, "error.country.invalid")
    )(Address.applyNonUK)(Address.unapplyNonUK)
  )

}
