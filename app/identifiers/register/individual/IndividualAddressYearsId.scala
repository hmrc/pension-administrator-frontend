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

package identifiers.register.individual

import identifiers._
import models.AddressYears
import play.api.libs.json.JsResult
import utils.UserAnswers

case object IndividualAddressYearsId extends TypedIdentifier[AddressYears] {
  override def toString: String = "individualAddressYears"

  override def cleanup(value: Option[AddressYears], answers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        answers
          .remove(IndividualPreviousAddressPostCodeLookupId)
          .flatMap(_.remove(IndividualPreviousAddressId))
      case _ => super.cleanup(value, answers)
    }
  }
}
