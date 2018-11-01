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

package identifiers.register

import identifiers._
import identifiers.register.company.{BusinessDetailsId, CompanyAddressId}
import identifiers.register.partnership.{PartnershipDetailsId, PartnershipRegisteredAddressId}
import identifiers.register.individual.{IndividualAddressId, IndividualDateOfBirthId, IndividualDetailsCorrectId, IndividualDetailsId}
import play.api.libs.json.JsResult
import utils.UserAnswers

case object AreYouInUKId extends TypedIdentifier[Boolean] {
  override def toString: String = "areYouInUK"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        userAnswers.removeAllOf(List(BusinessDetailsId, BusinessTypeId,
          PartnershipDetailsId,
          IndividualDetailsId, IndividualAddressId, IndividualDetailsCorrectId))
      case Some(true) =>
        userAnswers.removeAllOf(List(BusinessDetailsId, NonUKBusinessTypeId, CompanyAddressId,
          PartnershipDetailsId, PartnershipRegisteredAddressId,
          IndividualDetailsId, IndividualDateOfBirthId, IndividualAddressId))
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }
}
