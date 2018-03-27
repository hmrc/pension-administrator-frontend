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
import identifiers.register.company._
import models.CheckMode
import models.register.company.CompanyDetails
import play.api.libs.json.Reads
import viewmodels.AnswerRow

import scala.language.implicitConversions

class CheckYourAnswersHelper(userAnswers: UserAnswers, countryOptions: CountryOptions) {

  def cya[A](id: TypedIdentifier[A])(fn: A => Seq[AnswerRow])(implicit reads: Reads[A]): Seq[AnswerRow] = {
    userAnswers.get(id) map fn getOrElse Seq.empty[AnswerRow]
  }

  def email: Seq[AnswerRow] =
    cya(ContactDetailsId) { x =>
      Seq(AnswerRow(
        "contactDetails.email.checkYourAnswersLabel",
        Seq(x.email),
        false,
        controllers.register.company.routes.ContactDetailsController.onPageLoad(CheckMode).url
      ))
    }

  def phone: Seq[AnswerRow] =
    cya(ContactDetailsId) { x =>
      Seq(AnswerRow(
        "contactDetails.phone.checkYourAnswersLabel",
        Seq(x.phone),
        false,
        controllers.register.company.routes.ContactDetailsController.onPageLoad(CheckMode).url
      ))
    }

  def vatRegistrationNumber: Seq[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDetailsId) match {
    case Some(CompanyDetails(_, Some(vatRegNo), _)) =>
      Seq(AnswerRow(
        "companyDetails.vatRegistrationNumber.checkYourAnswersLabel",
        Seq(vatRegNo),
        false,
        controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url
      ))
    case _ => Seq.empty[AnswerRow]
  }

  def payeEmployerReferenceNumber: Seq[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDetailsId) match {
    case Some(CompanyDetails(_, _, Some(payeRefNo))) =>
      Seq(AnswerRow(
        "companyDetails.payeEmployerReferenceNumber.checkYourAnswersLabel",
        Seq(payeRefNo),
        false,
        controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url
      ))
    case _ => Seq.empty[AnswerRow]
  }

}
