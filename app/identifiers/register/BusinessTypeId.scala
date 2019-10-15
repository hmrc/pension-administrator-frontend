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

package identifiers.register

import identifiers._
import identifiers.register.company.HasCompanyCRNId
import models.register.BusinessType
import play.api.libs.json.{JsResult, JsSuccess}
import utils.UserAnswers

case object BusinessTypeId extends TypedIdentifier[BusinessType] {
  override def toString: String = "businessType"

  override def cleanup(value: Option[BusinessType], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(BusinessType.LimitedCompany) =>
        userAnswers.removeAllOf(List(HasCompanyCRNId))
      case _ => JsSuccess(userAnswers)
    }
  }

}
