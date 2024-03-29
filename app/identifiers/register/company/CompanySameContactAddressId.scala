/*
 * Copyright 2024 HM Revenue & Customs
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

package identifiers.register.company

import identifiers.TypedIdentifier
import play.api.libs.json.JsResult
import utils.UserAnswers
import utils.checkyouranswers.{BooleanCYA, CheckYourAnswers}

case object CompanySameContactAddressId extends TypedIdentifier[Boolean] {
  self =>
  override def toString = "companySameContactAddressId"

  override def cleanup(value: Option[Boolean], answers: UserAnswers): JsResult[UserAnswers] = {
    answers
      .remove(CompanyContactAddressId)
      .flatMap(_.remove(CompanyContactAddressPostCodeLookupId))
      .flatMap(_.remove(CompanyAddressYearsId))
      .flatMap(_.remove(CompanyPreviousAddressPostCodeLookupId))
      .flatMap(_.remove(CompanyPreviousAddressId))
  }

  implicit def cya: CheckYourAnswers[identifiers.register.company.CompanySameContactAddressId.type] = {
    BooleanCYA[self.type](Some("cya.label.company.same.contact.address"))()
  }

}
