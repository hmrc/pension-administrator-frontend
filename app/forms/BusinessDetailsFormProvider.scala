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

import forms.mappings.{Mappings, Transforms}
import models.BusinessDetails
import play.api.data.Form
import play.api.data.Forms.mapping

class BusinessDetailsFormProvider(isUK: Boolean) extends Mappings with Transforms {
  def apply(model: BusinessDetailsFormModel): Form[BusinessDetails] = {
    val companyNameMapping = "companyName" -> text(model.companyNameRequiredMsg)
      .verifying(
        firstError(
          maxLength(
            model.companyNameMaxLength,
            model.companyNameLengthMsg
          ),
          businessName(model.companyNameInvalidMsg)
        )
      )
    Form(
      if (isUK) {
        mapping(
          companyNameMapping,
          "utr" -> text(model.utrRequiredMsg.getOrElse("businessDetails.error.utr.required"))
            .verifying(
              firstError(
                maxLength(model.utrMaxLength, model.utrLengthMsg),
                uniqueTaxReference(model.utrInvalidMsg)
              )
            )
        )(BusinessDetails.applyForMandatoryUTR)(BusinessDetails.unapplyForMandatoryUTR)
      } else {
        mapping(
          companyNameMapping,
          "utr" -> optionalText()
            .verifying(
              firstError(
                maxLength(model.utrMaxLength, model.utrLengthMsg),
                uniqueTaxReference(model.utrInvalidMsg)
              )
            )
        )(BusinessDetails.apply)(BusinessDetails.unapply)
      }
    )
  }
}

case class BusinessDetailsFormModel(
                                     companyNameMaxLength: Int,
                                     companyNameRequiredMsg: String,
                                     companyNameLengthMsg: String,
                                     companyNameInvalidMsg: String,
                                     utrMaxLength: Int,
                                     utrRequiredMsg: Option[String],
                                     utrLengthMsg: String,
                                     utrInvalidMsg: String
                                   )