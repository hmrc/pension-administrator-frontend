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

import forms.behaviours.StringFieldBehaviours
import forms.mappings.Constraints
import models.BusinessDetails
import play.api.data.{Form, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

//scalastyle:off magic.number

class BusinessDetailsFormProviderSpec extends StringFieldBehaviours with Constraints {

  import BusinessDetailsFormProviderSpec._

  "companyName" must {

    val fieldName = "companyName"
    val maxLength = formModel.companyNameMaxLength

    behave like fieldThatBindsValidData(
      form(),
      fieldName,
      RegexpGen.from(businessNameRegex)
    )

    behave like fieldWithMaxLength(
      form(),
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, formModel.companyNameLengthMsg, Seq(maxLength))
    )

    behave like mandatoryField(
      form(),
      fieldName,
      requiredError = FormError(fieldName, formModel.companyNameRequiredMsg)
    )

    behave like fieldWithRegex(
      form(),
      fieldName,
      "[invalid]",
      error = FormError(fieldName, formModel.companyNameInvalidMsg, Seq(businessNameRegex))
    )

  }

  "utr" must {

    val fieldName = "utr"
    val maxLength = formModel.utrMaxLength

    behave like fieldThatBindsValidData(
      form(),
      fieldName,
      RegexpGen.from(utrRegex)
    )

    behave like mandatoryField(
      form(),
      fieldName,
      requiredError = FormError(fieldName, formModel.utrRequiredMsg.getOrElse(""))
    )

    behave like fieldWithRegex(
      form(),
      fieldName,
      "ABC",
      FormError(fieldName, formModel.utrInvalidMsg, Seq(utrRegex))
    )

    behave like fieldWithMaxLength(
      form(),
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, formModel.utrLengthMsg, Seq(maxLength))
    )

  }

  "form" must {
    val rawData = Map("companyName" -> "test", "utr" -> " 1234567890 ")
    val expectedData = BusinessDetails("test", Some("1234567890"))

    behave like formWithTransform[BusinessDetails](
      form(),
      rawData,
      expectedData
    )
  }

}

object BusinessDetailsFormProviderSpec {

  def form(isUK:Boolean = true): Form[BusinessDetails] = {
    new BusinessDetailsFormProvider(isUK).apply(formModel)
  }

  val formModel: BusinessDetailsFormModel =
    BusinessDetailsFormModel(
      companyNameMaxLength = 105,
      companyNameRequiredMsg = "businessDetails.error.companyName.required",
      companyNameLengthMsg = "businessDetails.error.companyName.length",
      companyNameInvalidMsg = "businessDetails.error.companyName.invalid",
      utrMaxLength = 10,
      utrRequiredMsg = Some("businessDetails.error.utr.required"),
      utrLengthMsg = "businessDetails.error.utr.length",
      utrInvalidMsg = "businessDetails.error.utr.invalid"
    )

}
