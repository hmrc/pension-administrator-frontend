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

import play.api.data.{FieldMapping, Mapping}
import play.api.data.Forms.{of, optional, tuple}
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIfEqual
import utils.Enumerable

trait Mappings extends Formatters with Constraints {

  protected def text(errorKey: String = "error.required"): FieldMapping[String] =
    of(stringFormatter(errorKey))

  protected def int(requiredKey: String = "error.required",
                    wholeNumberKey: String = "error.wholeNumber",
                    nonNumericKey: String = "error.nonNumeric"): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey))

  protected def boolean(requiredKey: String = "error.required",
                        invalidKey: String = "error.boolean"): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey))


  protected def enumerable[A](requiredKey: String = "error.required",
                              invalidKey: String = "error.invalid")(implicit ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey))

  protected def postCode(requiredKey: String, invalidKey: String): Mapping[Option[String]] = {
    val postCodeRegex = "^(?i)[A-Z]{1,2}[0-9][0-9A-Z]?[ ]?[0-9][A-Z]{2}"

    def toPostCode(data: (Option[String], Option[String])): Option[String] = data._2

    def fromPostCode(data: Option[String]): (Option[String], Option[String]) = (data, data)

    tuple(
      "postCode" -> mandatoryIfEqual[String]("country", "GB", text(requiredKey).verifying(regexp(postCodeRegex, invalidKey))),
      "postCode" -> optional(text(requiredKey))
    ).transform(toPostCode, fromPostCode)

  }
}
