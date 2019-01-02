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

package forms.mappings

import models.Vat
import play.api.data.Forms.tuple
import play.api.data.Mapping
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfTrue

trait VatMapping extends Mappings with Transforms {

  def vatMapping(requiredKey: String,
                 vatLengthKey: String,
                 requiredVatKey: String = "common.error.vat.required",
                 invalidVatKey: String = "common.error.vat.invalid"):
  Mapping[Vat] = {

    tuple("hasVat" -> boolean(requiredKey),
      "vat" -> mandatoryIfTrue(
        "vat.hasVat",
        text(requiredVatKey)
          .transform(vatRegistrationNumberTransform, noTransform)
          .verifying(
            firstError(
              maxLength(VatMapping.maxVatLength, vatLengthKey),
              vatRegistrationNumber(invalidVatKey))
          )
      )
    ).transform(toVat, fromVat)
  }

  protected def vatRegistrationNumberTransform(value: String): String = {
    strip(value).replaceAll("^[gG][bB]", "")
  }

  private[this] def fromVat(vat: Vat): (Boolean, Option[String]) = {
    vat match {
      case Vat.Yes(vatNo) => (true, Some(vatNo))
      case Vat.No => (false, None)
    }
  }

  private[this] def toVat(vatTuple: (Boolean, Option[String])) = {
    vatTuple match {
      case (true, Some(vat)) => Vat.Yes(vat)
      case (false, None) => Vat.No
      case _ => throw new RuntimeException("Invalid selection")
    }
  }

}

object VatMapping {
  val maxVatLength = 9
}
