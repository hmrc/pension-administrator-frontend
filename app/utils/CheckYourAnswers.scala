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

package utils

import identifiers.TypedIdentifier
import models.{Address, AddressYears, CheckMode}
import models.requests.DataRequest
import play.api.libs.json.Reads
import play.api.mvc.AnyContent
import viewmodels.AnswerRow

import scala.language.implicitConversions

trait CheckYourAnswers[I <: TypedIdentifier.PathDependent] {
  def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow]
}

object CheckYourAnswers {

  implicit def string[I <: TypedIdentifier[String]](implicit rds: Reads[String]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          string =>
            Seq(AnswerRow(
              s"${id.toString}.checkYourAnswersLabel",
              Seq(string),
              answerIsMessageKey = false,
              changeUrl
            ))
        }.getOrElse(Seq.empty)
    }

  implicit def address[I <: TypedIdentifier[Address]](implicit rds: Reads[Address], fn: Address => Seq[String]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] =
        userAnswers.get(id).map {
          address =>
            Seq(AnswerRow(
              s"${id.toString}.checkYourAnswersLabel",
              fn(address),
              false,
              changeUrl
            ))
        }.getOrElse(Seq.empty)
    }

  implicit def addressYears[I <: TypedIdentifier[AddressYears]](implicit rds: Reads[AddressYears]): CheckYourAnswers[I] =
    new CheckYourAnswers[I] {
      override def row(id: I)(changeUrl: String, userAnswers: UserAnswers): Seq[AnswerRow] = {
        userAnswers.get(id).map {
          addressYears =>
            Seq(AnswerRow(
              s"$id.checkYourAnswersLabel",
              Seq(s"common.addressYears.$addressYears"),
              true,
              controllers.register.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode).url
            ))
        }.getOrElse(Seq.empty)
      }
    }

  trait Ops[A] {
    def row(changeUrl: String)(implicit request: DataRequest[AnyContent], reads: Reads[A]): Seq[AnswerRow]
  }

  object Ops {
    implicit def toOps[I <: TypedIdentifier.PathDependent](id: I)(implicit ev: CheckYourAnswers[I]): Ops[id.Data] =
      new Ops[id.Data] {
        override def row(changeUrl: String)(implicit request: DataRequest[AnyContent], reads: Reads[id.Data]): Seq[AnswerRow] =
          ev.row(id)(changeUrl, request.userAnswers)
      }
  }
}