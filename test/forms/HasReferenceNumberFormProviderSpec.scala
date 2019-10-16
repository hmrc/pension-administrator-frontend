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

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError
import viewmodels.Message

class HasReferenceNumberFormProviderSpec extends BooleanFieldBehaviours {

  private val companyName = "ABC"
  private val requiredKey = Message("error.required", companyName).resolve
  private val invalidKey = "error.boolean"
  private val fieldName = "value"

  private def formProvider(companyName:String) = new HasReferenceNumberFormProvider()("error.required", companyName)

  "HasReferenceNumberFormProvider" must {

    behave like booleanField(
      formProvider(companyName),
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      formProvider(companyName),
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
