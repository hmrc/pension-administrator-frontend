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

import models.UniqueTaxReference
import play.api.data.Forms.tuple
import play.api.data.Mapping
import uk.gov.voa.play.form.ConditionalMappings.{mandatoryIfFalse, mandatoryIfTrue}

trait UtrMapping extends Mappings {

  protected def utrMapping(requiredKey: String,
                                   requiredUtrKey: String,
                                   utrLengthKey: String,
                                   utrInvalidKey: String,
                                   requiredReasonKey: String,
                                   reasonLengthKey : String,
                                   reasonInvalidKey: String = "common.error.reason.invalid"):
  Mapping[UniqueTaxReference] = {

    tuple("hasUtr" -> boolean(requiredKey),
      "utr" -> mandatoryIfTrue(
        "utr.hasUtr",
        text(requiredUtrKey)
          .verifying(
            firstError(
              maxLength(UtrMapping.utrMaxLength, utrLengthKey),
              uniqueTaxReference(utrInvalidKey)
            )
          )
      ),
      "reason" -> mandatoryIfFalse(
        "utr.hasUtr",
        text(requiredReasonKey)
          .verifying(firstError(maxLength(UtrMapping.reasonMaxLength, reasonLengthKey),
            safeText(reasonInvalidKey))
          )
      )
    ).transform(toUtr, fromUtr)
  }

  private[this] def toUtr(utrTuple: (Boolean, Option[String], Option[String])) = {
    utrTuple match {
      case (true, Some(utr), None) => UniqueTaxReference.Yes(utr)
      case (false, None, Some(reason)) => UniqueTaxReference.No(reason)
      case _ => throw new RuntimeException("Invalid selection")
    }
  }

  private[this] def fromUtr(uniqueTaxReference: UniqueTaxReference) ={
    uniqueTaxReference match {
      case UniqueTaxReference.Yes(utr) => (true, Some(utr), None)
      case UniqueTaxReference.No(reason) => (false, None, Some(reason))
    }
  }

}

object UtrMapping {
  val utrMaxLength: Int = 10
  val reasonMaxLength: Int = 160
}
