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

package identifiers.register.partnership.partners

import identifiers._
import identifiers.register.partnership.MoreThanTenPartnersId
import models.{Index, PersonName}
import play.api.i18n.Messages
import play.api.libs.json.{JsPath, JsResult}
import utils.UserAnswers
import utils.checkyouranswers.{CheckYourAnswers, CheckYourAnswersPartner, PersonNameCYA}
import viewmodels.{AnswerRow, Link, Message}

case class PartnerNameId(index: Int) extends TypedIdentifier[PersonName] {
  override def path: JsPath = JsPath \ "partners" \ index \ PartnerNameId.toString

  override def cleanup(value: Option[PersonName], userAnswers: UserAnswers): JsResult[UserAnswers] =
    value match {
      case Some(PersonName(_, _, true, _)) => userAnswers.remove(MoreThanTenPartnersId)
      case _ => super.cleanup(value, userAnswers)
    }
}

object PartnerNameId {

  def collectionPath: JsPath = JsPath \ "partners" \\ PartnerNameId.toString

  override def toString: String = "partnerDetails"

  implicit def cya(implicit messages: Messages): CheckYourAnswers[PartnerNameId] =
    new CheckYourAnswersPartner[PartnerNameId] {
      private def label(ua: UserAnswers, index: Index): String =
        dynamicMessage(ua, messageKey = "cya.label.name", index)

      private def hiddenLabel(ua: UserAnswers, index: Index): Message =
        dynamicMessage(ua, messageKey = "partnerName.visuallyHidden.text", index)


      override def row(id: PartnerNameId)(changeUrl: Option[Link], userAnswers: UserAnswers): Seq[AnswerRow] =
        PersonNameCYA[PartnerNameId](Some(label(userAnswers, id.index)), Some(hiddenLabel(userAnswers, id.index)))().row(id)(changeUrl, userAnswers)
    }

}

