/*
 * Copyright 2021 HM Revenue & Customs
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

import identifiers._
import identifiers.register.adviser._
import identifiers.register.company._
import identifiers.register.company.directors.DirectorId
import identifiers.register.individual._
import identifiers.register.partnership._
import identifiers.register.partnership.partners.PartnerId
import play.api.libs.json.JsResult
import utils.UserAnswers

case object AreYouInUKId extends TypedIdentifier[Boolean] {
  override def toString: String = "areYouInUK"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        removeIndividualData(userAnswers).flatMap(
          removeCompanyData).flatMap(
          removePartnershipData).flatMap(
          removeDeclarationData).flatMap(
          _.removeAllOf(List(IndividualDetailsCorrectId, IndividualContactAddressListId, IndividualPreviousAddressPostCodeLookupId,
            BusinessTypeId, CompanyRegistrationNumberId, ConfirmCompanyAddressId, CompanyContactAddressPostCodeLookupId,
            HasPAYEId, EnterPAYEId, HasVATId, EnterVATId,
            ConfirmPartnershipDetailsId
          ))
        )
      case Some(true) =>
        removeIndividualData(userAnswers).flatMap(
          removeCompanyData).flatMap(
          removePartnershipData).flatMap(
          removeDeclarationData).flatMap(
          _.removeAllOf(List(NonUKBusinessTypeId, CompanyAddressId, PartnershipRegisteredAddressId))
        )
      case _ =>
        super.cleanup(value, userAnswers)
    }
  }

  private def removeDeclarationData(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.removeAllOf(List(
      DeclarationWorkingKnowledgeId, AdviserNameId, AdviserEmailId, AdviserPhoneId, AdviserAddressPostCodeLookupId, AdviserAddressListId, AdviserAddressId
    ))
  }

  private def removeIndividualData(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.removeAllOf(List(
      IndividualAddressYearsId,
      IndividualPreviousAddressListId, IndividualPreviousAddressId,
      IndividualEmailId,
      IndividualPhoneId,
      IndividualDateOfBirthId,
      IndividualSameContactAddressId,
      IndividualDetailsId, IndividualAddressId, RegistrationInfoId
    ))
  }

  private def removePartnershipData(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.removeAllOf(List(BusinessNameId, BusinessUTRId, PartnershipSameContactAddressId,
      PartnershipContactAddressPostCodeLookupId, PartnershipContactAddressListId, PartnershipContactAddressId,
      PartnershipAddressYearsId, PartnershipPreviousAddressId, PartnershipPreviousAddressPostCodeLookupId,
      PartnershipPreviousAddressListId, PartnershipEmailId, PartnershipPhoneId, MoreThanTenPartnersId))
      .flatMap(_.remove(PartnerId))
  }

  private def removeCompanyData(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.removeAllOf(List(BusinessNameId, BusinessUTRId, CompanySameContactAddressId,
      CompanyAddressListId, CompanyContactAddressId, CompanyContactAddressListId, CompanyAddressYearsId, CompanyPreviousAddressId,
      CompanyPreviousAddressPostCodeLookupId, CompanyEmailId, CompanyPhoneId, MoreThanTenDirectorsId))
      .flatMap(_.remove(DirectorId))
  }
}
