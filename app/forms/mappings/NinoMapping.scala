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

import models.Nino
import play.api.data.Forms.tuple
import play.api.data.Mapping
import uk.gov.voa.play.form.ConditionalMappings.{mandatoryIfFalse, mandatoryIfTrue}

trait NinoMapping extends Mappings with Transforms {

  def ninoMapping(requiredKey: String,
                  requiredNinoKey: String = "common.error.nino.required",
                  requiredReasonKey: String,
                  reasonLengthKey: String,
                  invalidNinoKey: String = "common.error.nino.invalid",
                  invalidReasonKey: String = "common.error.reason.invalid"):
  Mapping[Nino] = {

    tuple("hasNino" -> boolean(requiredKey),
      "nino" -> mandatoryIfTrue(
        "nino.hasNino",
        text(requiredNinoKey)
          .transform(ninoTransform, noTransform)
          .verifying(validNino(invalidNinoKey))
      ),
      "reason" -> mandatoryIfFalse(
        "nino.hasNino",
        text(requiredReasonKey)
          .verifying(firstError(maxLength(NinoMapping.reasonMaxLength, reasonLengthKey),
            safeText(invalidReasonKey)
      )))
    ).transform(toNino, fromNino)
  }

  private[this] def fromNino(nino: Nino): (Boolean, Option[String], Option[String]) = {
    nino match {
      case Nino.Yes(ninoNo) => (true, Some(ninoNo), None)
      case Nino.No(reason) =>  (false, None, Some(reason))
    }
  }

  private[this] def toNino(ninoTuple: (Boolean, Option[String], Option[String])) = {
    ninoTuple match {
      case (true, Some(nino), None)  => Nino.Yes(nino)
      case (false, None, Some(reason))  => Nino.No(reason)
      case _ => throw new RuntimeException("Invalid selection")
    }
  }

}

object NinoMapping {
  val reasonMaxLength = 160
}
