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

import identifiers.register.company.{CompanyUniqueTaxReferenceId, ContactDetailsId}
import models.CheckMode
import viewmodels.AnswerRow

class CheckYourAnswersHelper(userAnswers: UserAnswers) {

  def companyPreviousAddress: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyPreviousAddressId) map {
    x => AnswerRow("companyPreviousAddress.checkYourAnswersLabel", s"${x.field1} ${x.field2}", false, controllers.register.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode).url)
  }

  def contactDetails: Option[AnswerRow] = userAnswers.get(ContactDetailsId) map {
    x => AnswerRow("contactDetails.checkYourAnswersLabel", s"${x.email} ${x.phone}", false, controllers.register.company.routes.ContactDetailsController.onPageLoad(CheckMode).url)
  }
  
  def companyDetails: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDetailsId) map {
    x => AnswerRow("companyDetails.checkYourAnswersLabel", s"${x.companyName} ${x.vatRegistrationNumber} ${x.payeEmployerReferenceNumber}", false, controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url)
  }

  def companyUniqueTaxReference: Option[AnswerRow] = userAnswers.get(CompanyUniqueTaxReferenceId) map {
    x => AnswerRow("companyUniqueTaxReference.checkYourAnswersLabel", s"$x", false, controllers.register.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode).url)
  }

  def companyRegistrationNumber: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyRegistrationNumberId) map {
    x => AnswerRow("companyRegistrationNumber.checkYourAnswersLabel", s"$x", false, controllers.register.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url)
  }
}
