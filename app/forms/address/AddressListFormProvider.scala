/*
 * Copyright 2024 HM Revenue & Customs
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

import forms.mappings.{Constraints, Mappings}
import javax.inject.Inject
import play.api.data.Form

class AddressListFormProvider @Inject() extends Mappings with Constraints {

  def apply(addresses: Seq[?], requiredError: String): Form[Int] =
    Form(
      "value" -> int(requiredError)
        .verifying(minimumValue(0, "error.invalid"))
        .verifying(maximumValue(addresses.length - 1, "error.invalid"))
    )

}
