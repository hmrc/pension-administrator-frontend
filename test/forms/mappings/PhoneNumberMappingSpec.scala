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

package forms.mappings

import forms.behaviours.PhoneNumberBehaviours
import play.api.data.{Form, Mapping}

class PhoneNumberMappingSpec extends PhoneNumberBehaviours {

  "Phone number mapping" should {
    val fieldName = "phoneNumber"
    val keyPhoneNumberRequired = "contactDetails.error.phone.required"
    val keyPhoneNumberLength = "contactDetails.error.phone.length"
    val keyPhoneNumberInvalid = "contactDetails.error.phone.invalid"

    val mapping: Mapping[String] = phoneNumberMapping(keyPhoneNumberRequired, keyPhoneNumberLength, keyPhoneNumberInvalid)
    val form: Form[String] = Form(fieldName -> mapping)

    behave like formWithPhoneNumberField(
      form,
      fieldName,
      keyPhoneNumberRequired,
      keyPhoneNumberLength,
      keyPhoneNumberInvalid
    )

  }

}
