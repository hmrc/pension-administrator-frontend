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

import forms.FormErrorHelper
import forms.mappings.{Mappings, Transforms}
import javax.inject.Inject
import models.BusinessDetails
import play.api.data.Form
import play.api.data.Forms._

class BusinessDetailsFormProvider @Inject() extends FormErrorHelper with Mappings with Transforms {

  def apply(): Form[BusinessDetails] = Form(
    mapping(
      "companyName" -> text("businessDetails.error.companyName.required")
        .verifying(
          firstError(
            maxLength(
              BusinessDetailsFormProvider.BusinessNameLength,
              "businessDetails.error.companyName.length"
            ),
            companyName("businessDetails.error.companyName.invalid")
          )
        ),

      "utr" -> text("businessDetails.error.utr.required")
        .transform(standardTextTransform, noTransform)
        .verifying(firstError(
          maxLength(BusinessDetailsFormProvider.utrMaxLength, "businessDetails.error.utr.length"),
          uniqueTaxReference("businessDetails.error.utr.invalid")
        )
        )
    )(BusinessDetails.apply)(BusinessDetails.unapply)
  )
}

object BusinessDetailsFormProvider {
  val BusinessNameLength: Int = 105
  val utrMaxLength: Int = 10
}
