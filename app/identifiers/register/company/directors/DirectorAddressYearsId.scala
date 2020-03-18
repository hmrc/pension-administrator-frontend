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

package identifiers.register.company.directors

import identifiers._
import identifiers.register.DirectorsOrPartnersChangedId
import models.AddressYears
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers, CheckYourAnswersDirector}
import viewmodels.{AnswerRow, Link, Message}


case class DirectorAddressYearsId(index: Int) extends TypedIdentifier[AddressYears] {

  override def path: JsPath = JsPath \ "directors" \ index \ DirectorAddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers.set(DirectorsOrPartnersChangedId)(true).asOpt.getOrElse(userAnswers)
          .removeAllOf(List(DirectorPreviousAddressPostCodeLookupId(index), DirectorPreviousAddressListId(index), DirectorPreviousAddressId(index)))
      case Some(AddressYears.UnderAYear) =>
        userAnswers.set(IsDirectorCompleteId(index))(false)
      case _ => super.cleanup(value, userAnswers)
    }
  }

}

object DirectorAddressYearsId {
  override lazy val toString: String = "directorAddressYears"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[DirectorAddressYearsId] = {

    new CheckYourAnswersDirector[DirectorAddressYearsId] {

      private def label(index: Int, ua: UserAnswers): String =
        dynamicMessage(ua, "addressYears.heading", index)


      private def hiddenLabel(index: Int, ua: UserAnswers): Message =
        dynamicMessage(ua, "addressYears.visuallyHidden.text", index)

      override def row(id: DirectorAddressYearsId)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(DirectorAddressId(id.index)) match {
          case Some(_) =>
            AddressYearsCYA(label(id.index, userAnswers), Some(hiddenLabel(id.index, userAnswers)))()
              .row(id)(changeUrl, userAnswers)
          case _ =>
            AddressYearsCYA(label(id.index, userAnswers), Some(hiddenLabel(id.index, userAnswers)), isMandatory = false)()
              .row(id)(changeUrl, userAnswers)
        }
    }
  }
}

