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

package identifiers.register.company

import identifiers._
import play.api.i18n.Messages
import utils.UserAnswers
import utils.checkyouranswers.{BooleanCYA, CheckYourAnswers, CheckYourAnswersCompany, StringCYA}
import viewmodels.{AnswerRow, Link}

case object CompanyRegistrationNumberId extends TypedIdentifier[String] {
  self =>
  override def toString: String = "companyRegistrationNumber"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[self.type] =
    new CheckYourAnswersCompany[self.type] {
      private def label(ua: UserAnswers): String =
        dynamicMessage(ua, "companyRegistrationNumber.heading")

      private def hiddenLabel(index: Int, ua: UserAnswers): String =
        dynamicMessage(ua, "messages__visuallyhidden__dynamic_hasCrn")


      override def row(id: self.type)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        StringCYA[self.type](Some(label(userAnswers)))().row(id)(changeUrl, userAnswers)
    }

}
