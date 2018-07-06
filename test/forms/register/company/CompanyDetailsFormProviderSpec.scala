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

import forms.behaviours.{PayeStringBehaviours, StringFieldBehaviours, VatBehavioursString}
import forms.mappings.Constraints
import models.register.company.CompanyDetails
import org.scalatest.OptionValues

class CompanyDetailsFormProviderSpec extends StringFieldBehaviours with Constraints with OptionValues with PayeStringBehaviours with VatBehavioursString {

  val form = new CompanyDetailsFormProvider()()

  ".vatRegistrationNumber" must {
    val fieldName = "vatRegistrationNumber"
    val keyVatLength = "companyDetails.error.vatRegistrationNumber.length"
    val keyVatInvalid = "companyDetails.error.vatRegistrationNumber.invalid"

    behave like formWithVatField(
      form,
      fieldName,
      keyVatLength,
      keyVatInvalid
    )

  }

  ".payeEmployerReferenceNumber" must {

    val fieldName = "payeEmployerReferenceNumber"
    val keyPayeLength = "companyDetails.error.payeEmployerReferenceNumber.length"
    val keyPayeInvalid = "companyDetails.error.payeEmployerReferenceNumber.invalid"

    behave like formWithPayeField(
      form,
      fieldName,
      keyPayeLength,
      keyPayeInvalid
    )

  }

  "form" must {
    val rawData = Map("vatRegistrationNumber" -> " GB1 2 3 456789 ", "payeEmployerReferenceNumber" -> " 123\\/4567898765 ")
    val expectedData = CompanyDetails(Some("123456789"), Some("1234567898765"))

    behave like formWithTransform[CompanyDetails](
      form,
      rawData,
      expectedData
    )
  }

}
