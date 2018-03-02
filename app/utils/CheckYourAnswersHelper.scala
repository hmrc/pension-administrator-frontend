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

  def directorAddress(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.company.directorAddressId) map {
    x => AnswerRow("directorAddress.checkYourAnswersLabel", s"${x.addressLine1} ${x.addressLine2}", false, controllers.register.company.routes.DirectorAddressController.onPageLoad(CheckMode, index).url)
  }

  def directorUniqueTaxReference(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorUniqueTaxReferenceId(index)) map {
    x => AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel", s"directorUniqueTaxReference.$x", true, controllers.register.company.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode, index).url)
  }

  def directorDetails(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorDetailsId(index)) map {
    x => AnswerRow("directorDetails.checkYourAnswersLabel", s"${x.firstName} ${x.lastName}", false, controllers.register.company.routes.DirectorDetailsController.onPageLoad(CheckMode, index).url)
  }

  def directorNino(index: Int): Option[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorNinoId(index)) map {
    x => AnswerRow("directorNino.checkYourAnswersLabel", s"directorNino.$x", true, controllers.register.company.routes.DirectorNinoController.onPageLoad(CheckMode, index).url)
  }

  def companyPreviousAddress: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyPreviousAddressId) map {
    x => AnswerRow("companyPreviousAddress.checkYourAnswersLabel", s"${x.addressLine1} ${x.addressLine2}", false, controllers.register.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode).url)
  }

  def companyAddressList: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyAddressListId) map {
    x => AnswerRow("companyAddressList.checkYourAnswersLabel", s"companyAddressList.$x", true, controllers.register.company.routes.CompanyAddressListController.onPageLoad(CheckMode).url)
  }

  def companyPreviousAddressPostCodeLookup: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyPreviousAddressPostCodeLookupId) map {
    x => AnswerRow("companyPreviousAddressPostCodeLookup.checkYourAnswersLabel", s"$x", false, controllers.register.company.routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url)
  }

  def addCompanyDirectors: Option[AnswerRow] = userAnswers.get(identifiers.register.company.AddCompanyDirectorsId) map {
    x => AnswerRow("addCompanyDirectors.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true, controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(CheckMode).url)
  }

  def moreThanTenDirectors: Option[AnswerRow] = userAnswers.get(identifiers.register.company.MoreThanTenDirectorsId) map {
    x => AnswerRow("moreThanTenDirectors.checkYourAnswersLabel", if(x) "site.yes" else "site.no", true, controllers.register.company.routes.MoreThanTenDirectorsController.onPageLoad(CheckMode).url)
  }

  def contactDetails: Option[AnswerRow] = userAnswers.get(ContactDetailsId) map {
    x => AnswerRow("contactDetails.checkYourAnswersLabel", s"${x.email} ${x.phone}", false, controllers.register.company.routes.ContactDetailsController.onPageLoad(CheckMode).url)
  }
  
  def companyDetails: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDetailsId) map {
    x => AnswerRow("companyDetails.checkYourAnswersLabel", s"${x.companyName} ${x.vatRegistrationNumber} ${x.payeEmployerReferenceNumber}", false, controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url)
  }

  def companyAddressYears: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyAddressYearsId) map {
    x => AnswerRow("companyAddressYears.checkYourAnswersLabel", s"companyAddressYears.$x", true, controllers.register.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode).url)
  }

  def companyUniqueTaxReference: Option[AnswerRow] = userAnswers.get(CompanyUniqueTaxReferenceId) map {
    x => AnswerRow("companyUniqueTaxReference.checkYourAnswersLabel", s"$x", false, controllers.register.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode).url)
  }

  def companyRegistrationNumber: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyRegistrationNumberId) map {
    x => AnswerRow("companyRegistrationNumber.checkYourAnswersLabel", s"$x", false, controllers.register.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url)
  }
}
