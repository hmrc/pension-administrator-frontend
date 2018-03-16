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

package identifiers.register.company

import identifiers.TypedIdentifier
import models.{Address, TolerantAddress}
import play.api.libs.json.Reads
import utils.{CheckYourAnswers, UserAnswers}
import viewmodels.AnswerRow

case object CompanyAddressId extends TypedIdentifier[TolerantAddress] {
  override def toString = "companyAddressId"

  implicit def checkYourAnswers[I <: TypedIdentifier[TolerantAddress]](implicit rds: Reads[TolerantAddress], fn: Address => Seq[String]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map { x =>
          x.toAddress match {
            case Some(address) =>
              Seq(AnswerRow(
                "cya.label.address",
                fn(address),
                false,
                controllers.register.company.routes.CompanyAddressController.onPageLoad().url
              ))
            case _ => Seq.empty[AnswerRow]
          }
        }.getOrElse(Seq.empty)
    }
}
