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

import forms.mappings._
import javax.inject.Inject
import models.register.company.CompanyDetails
import play.api.data.Form
import play.api.data.Forms._

class CompanyDetailsFormProvider @Inject() extends Mappings with PayeMappingString with VatMapping with Transforms {

  def apply(): Form[CompanyDetails] = Form(
    mapping(
      "vatRegistrationNumber" -> optional(
        vatMapping(
          "companyDetails.error.vatRegistrationNumber.length",
          "companyDetails.error.vatRegistrationNumber.invalid"
        )),
      "payeEmployerReferenceNumber" -> optional(
        payeMapping(
          "companyDetails.error.payeEmployerReferenceNumber.length",
          "companyDetails.error.payeEmployerReferenceNumber.invalid"
        )
      )
    )(CompanyDetails.apply)(CompanyDetails.unapply)
  )
}

object CompanyDetailsFormProvider {
  val companyNameLength: Int = 160
}
