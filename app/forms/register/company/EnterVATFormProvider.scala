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

package forms.register.company

import forms.FormErrorHelper
import forms.mappings.VatMappingString
import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.Messages
import viewmodels.Message

class EnterVATFormProvider @Inject() extends FormErrorHelper with VatMappingString {

  def apply(name: String)(implicit messages: Messages): Form[String] =
    Form(
      "value" -> vatMapping(
        keyVatRequired = "enterVAT.error.required",
        keyVatLength = Message("enterVAT.error.length", name),
        keyVatInvalid = Message("enterVAT.error.invalid", name)
      )
    )

}