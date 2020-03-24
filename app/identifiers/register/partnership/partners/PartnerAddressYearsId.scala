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

package identifiers.register.partnership.partners

import identifiers._
import identifiers.register.DirectorsOrPartnersChangedId
import models.AddressYears
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{AddressYearsCYA, CheckYourAnswers, CheckYourAnswersPartner}
import viewmodels.{AnswerRow, Link, Message}

case class PartnerAddressYearsId(index: Int) extends TypedIdentifier[AddressYears] {
  override def path: JsPath = JsPath \ "partners" \ index \ PartnerAddressYearsId.toString

  override def cleanup(value: Option[AddressYears], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(AddressYears.OverAYear) =>
        userAnswers.set(DirectorsOrPartnersChangedId)(true).asOpt.getOrElse(userAnswers)
          .removeAllOf(List(PartnerPreviousAddressPostCodeLookupId(index), PartnerPreviousAddressId(index)))
      case _ => super.cleanup(value, userAnswers)
    }
  }
}

object PartnerAddressYearsId {
  override lazy val toString: String = "partnerAddressYears"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[PartnerAddressYearsId] = {

    new CheckYourAnswersPartner[PartnerAddressYearsId] {

      private def label(index: Int, ua: UserAnswers): String =
        dynamicMessage(ua, messageKey = "addressYears.heading", index)


      private def hiddenLabel(index: Int, ua: UserAnswers): Message =
        dynamicMessage(ua, messageKey = "addressYears.visuallyHidden.text", index)

      override def row(id: PartnerAddressYearsId)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(PartnerAddressId(id.index)) match {
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


