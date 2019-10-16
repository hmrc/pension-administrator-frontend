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

import forms.behaviours.{PhoneBehaviours, StringFieldBehaviours}
import forms.mappings.Constraints

class PhoneFormProviderSpec extends StringFieldBehaviours with PhoneBehaviours with Constraints {

  val form = new PhoneFormProvider()

  "phone" must {

    val fieldName = "value"
    val keyPhoneRequired = "contactDetails.error.phone.required"
    val keyPhoneLength = "contactDetails.error.phone.length"
    val keyPhoneInvalid = "contactDetails.error.phone.invalid"

    behave like formWithPhoneField(
      form(),
      fieldName,
      keyPhoneRequired,
      keyPhoneLength,
      keyPhoneInvalid
    )
  }
}
