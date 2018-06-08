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

import forms.behaviours.CrnBehaviours
import play.api.data.Form

class CompanyRegistrationNumberFormProviderSpec extends CrnBehaviours {
  val form: Form[String] = new CompanyRegistrationNumberFormProvider().apply()

  ".value" must {

    val fieldName = "value"
    val keyCrnRequired = "companyRegistrationNumber.error.required"
    val keyCrnLength = "companyRegistrationNumber.error.length"
    val keyCrnInvalid = "companyRegistrationNumber.error.invalid"

    behave like formWithCrnField(
      form,
      fieldName,
      keyCrnRequired,
      keyCrnLength,
      keyCrnInvalid
    )
  }

  "form" must {
    val rawData = Map("value" -> " 1234567 ")
    val expectedData = "1234567"

    behave like formWithTransform(
      form,
      rawData,
      expectedData
    )
  }

}
