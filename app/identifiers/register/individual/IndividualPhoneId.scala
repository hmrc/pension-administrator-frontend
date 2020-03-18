/*
 * Copyright 2020 HM Revenue & Customs
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

import identifiers.TypedIdentifier
import play.api.i18n.Messages
import play.api.libs.json.JsPath
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, StringCYA}
import viewmodels.{AnswerRow, Link}

case object IndividualPhoneId extends TypedIdentifier[String] {
  self =>
  override def path: JsPath = JsPath \ "individualContactDetails" \ IndividualPhoneId.toString

  override def toString: String = "phone"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[self.type] =
    new CheckYourAnswers[self.type] {
      override def row(id: self.type)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
        StringCYA[self.type](Some("individual.phone.title"), Some("individualPhone.visuallyHidden.text"))().row(id)(changeUrl, userAnswers)
      }
    }
}

