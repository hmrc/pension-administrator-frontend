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

package identifiers.register

import identifiers.TypedIdentifier
import models.register.BusinessType.{LimitedCompany, OverseasCompany, UnlimitedCompany}
import play.api.i18n.Messages
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, StringCYA}
import viewmodels.{AnswerRow, Link, Message}

case object BusinessUTRId extends TypedIdentifier[String] {
  self =>

  override def toString: String = "utr"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[self.type] =
    new CheckYourAnswers[self.type] {

      override def row(id: self.type)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(AreYouInUKId) match {
          case Some(false) =>
            Nil
          case _ =>
            StringCYA(Some(label(userAnswers)))().row(id)(changeUrl, userAnswers)
        }
      }
    }

  private def label(userAnswers: UserAnswers): Message =
    userAnswers.get(BusinessTypeId).map(
      businessType =>
        if( businessType == LimitedCompany || businessType == UnlimitedCompany || businessType == OverseasCompany){
          Message("utr.heading", Message("theCompany"), Message("utr.company.hint"))
        } else {
          Message("utr.heading", Message("thePartnership"), Message("utr.partnership.hint"))
        }
    ).getOrElse(
      Message("utr.heading", Message("theCompany"), Message("utr.company.hint"))
    )

}
