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

import forms.mappings.{Mappings, Transforms}
import javax.inject.Inject

import models.register.company.CompanyDetails
import play.api.data.{Form, Forms}
import play.api.data.Forms._

class CompanyDetailsFormProvider @Inject() extends Mappings with Transforms {

  def apply(): Form[CompanyDetails] = Form(
    mapping(
      "companyName" -> text("companyDetails.error.companyName.required")
        .verifying(maxLength(CompanyDetailsFormProvider.companyNameLength, "companyDetails.error.companyName.length")),
      "vatRegistrationNumber" -> optional(
        Forms.text
          .transform(vatRegistrationNumberTransform, noTransform)
          .verifying(
            firstError(
              maxLength(CompanyDetailsFormProvider.vatRegistrationNumberLength, "companyDetails.error.vatRegistrationNumber.length"),
              vatRgistrationNumber("companyDetails.error.vatRegistrationNumber.invalid"))
          )),
      "payeEmployerReferenceNumber" -> optional(
        Forms.text.verifying(
          firstError(
            maxLength(CompanyDetailsFormProvider.payeEmployerReferenceNumberLength, "companyDetails.error.payeEmployerReferenceNumber.length"),
            payeEmployerReferenceNumber("companyDetails.error.payeEmployerReferenceNumber.invalid"))
        ))
    )(CompanyDetails.apply)(CompanyDetails.unapply)
  )
}

object CompanyDetailsFormProvider {
  val companyNameLength: Int = 160
  val vatRegistrationNumberLength: Int = 9
  val payeEmployerReferenceNumberLength: Int = 13
}
