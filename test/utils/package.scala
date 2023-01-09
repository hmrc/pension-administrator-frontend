/*
 * Copyright 2023 HM Revenue & Customs
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

import controllers.actions.{DataRetrievalAction, FakeDataRetrievalAction}
import identifiers.register._
import identifiers.register.adviser._
import identifiers.register.company._
import identifiers.register.company.directors._
import identifiers.register.individual._
import identifiers.register.partnership._
import identifiers.register.partnership.partners._
import models._
import models.register.{BusinessType, DeclarationWorkingKnowledge}
import org.scalatest.OptionValues

import java.time.LocalDate


package object utils {

  //scalastyle:off number.of.methods
  implicit class UserAnswerOps(answers: UserAnswers) extends OptionValues {

    def registrationInfo(answer: RegistrationInfo): UserAnswers = {
      answers.set(RegistrationInfoId)(answer).asOpt.value
    }

    def areYouInUk(answer: Boolean): UserAnswers = {
      answers.set(identifiers.register.AreYouInUKId)(answer).asOpt.value
    }

    def isRegisteredName(answer: Boolean): UserAnswers = {
      answers.set(identifiers.register.IsRegisteredNameId)(answer).asOpt.value
    }

    def hasVat(answer: Boolean): UserAnswers = {
      answers.set(identifiers.register.HasVATId)(answer).asOpt.value
    }

    // Individual PSA Contact
    def individualContactAddress(address: Address): UserAnswers = {
      answers.set(IndividualContactAddressId)(address).asOpt.value
    }

    def individualDetails(individualDetails: TolerantIndividual): UserAnswers = {
      answers.set(IndividualDetailsId)(individualDetails).asOpt.value
    }

    def individualDob(dob: LocalDate): UserAnswers = {
      answers.set(IndividualDateOfBirthId)(dob).asOpt.value
    }

    def individualContactAddressList(address: TolerantAddress): UserAnswers = {
      answers.set(IndividualContactAddressListId)(address).asOpt.value
    }

    // Individual PSA

    def individualSameContactAddress(areSame: Boolean): UserAnswers = {
      answers.set(IndividualSameContactAddressId)(areSame).asOpt.value
    }

    def individualPreviousAddress(address: Address): UserAnswers = {
      answers.set(IndividualPreviousAddressId)(address).asOpt.value
    }

    def individualPreviousAddressList(address: TolerantAddress): UserAnswers = {
      answers.set(IndividualPreviousAddressListId)(address).asOpt.value
    }

    def individualAddress(address: Address): UserAnswers = {
      answers.set(IndividualAddressId)(address.toTolerantAddress).asOpt.value
    }

    def individualEmail(email: String): UserAnswers = {
      answers.set(IndividualEmailId)(email).asOpt.value
    }

    def individualPhone(phone: String): UserAnswers = {
      answers.set(IndividualPhoneId)(phone).asOpt.value
    }

    // Company PSA

    def companyCrn(crn: String): UserAnswers = {
      answers.set(CompanyRegistrationNumberId)(crn).asOpt.value
    }

    def companyIsThisPreviousAddress(flag: Boolean): UserAnswers = {
      answers.set(CompanyConfirmPreviousAddressId)(flag).asOpt.value
    }

    def companyTradingOverAYear(flag: Boolean): UserAnswers = {
      answers.set(CompanyTradingOverAYearId)(flag).asOpt.value
    }

    def companyHasCrn(hasCrn: Boolean): UserAnswers = {
      answers.set(HasCompanyCRNId)(hasCrn).asOpt.value
    }

    def hasPaye(flag: Boolean): UserAnswers = {
      answers.set(HasPAYEId)(flag).asOpt.value
    }

    def enterPaye(paye: String): UserAnswers = {
      answers.set(EnterPAYEId)(paye).asOpt.value
    }

    def hasVatRegistrationNumber(flag: Boolean): UserAnswers = {
      answers.set(HasVATId)(flag).asOpt.value
    }

    def enterVat(vat: String): UserAnswers = {
      answers.set(EnterVATId)(vat).asOpt.value
    }

    def companyEmail(email: String): UserAnswers = {
      answers.set(CompanyEmailId)(email).asOpt.value
    }

    def companyPhone(phone: String): UserAnswers = {
      answers.set(CompanyPhoneId)(phone).asOpt.value
    }

    def companyPreviousAddress(address: Address): UserAnswers = {
      answers.set(CompanyPreviousAddressId)(address).asOpt.value
    }

    def companyAddressList(address: TolerantAddress): UserAnswers = {
      answers.set(CompanyAddressListId)(address).asOpt.value
    }

    def companyContactAddress(address: Address): UserAnswers = {
      answers.set(CompanyContactAddressId)(address).asOpt.value
    }

    def companyContactAddressList(address: TolerantAddress): UserAnswers = {
      answers.set(CompanyContactAddressListId)(address).asOpt.value
    }

    def companySameContactAddress(same: Boolean): UserAnswers = {
      answers.set(CompanySameContactAddressId)(same).asOpt.value
    }

    //company non uk

    def nonUkCompanyAddress(address: Address): UserAnswers = {
      answers.set(CompanyAddressId)(address.toTolerantAddress).asOpt.value
    }

    //partnership non uk

    def nonUkPartnershipAddress(address: Address): UserAnswers = {
      answers.set(PartnershipRegisteredAddressId)(address.toTolerantAddress).asOpt.value
    }

    // Company director
    def directorName(index: Int = 0, name: PersonName = PersonName("first", "last")): UserAnswers = {
      answers.set(DirectorNameId(index))(name).asOpt.value
    }

    def directorAddress(index: Int, address: Address): UserAnswers = {
      answers.set(DirectorAddressId(index))(address).asOpt.value
    }

    def directorAddressYears(index: Int, addressYears: AddressYears): UserAnswers = {
      answers.set(DirectorAddressYearsId(index))(addressYears).asOpt.value
    }

    def directorPhone(index: Int, phone: String): UserAnswers = {
      answers.set(DirectorPhoneId(index))(phone).asOpt.value
    }

    def directorDob(index: Int, dateOfBirth: LocalDate): UserAnswers = {
      answers.set(DirectorDOBId(index))(dateOfBirth).asOpt.value
    }

    def directorEmail(index: Int, email: String): UserAnswers = {
      answers.set(DirectorEmailId(index))(email).asOpt.value
    }

    def directorHasNINO(index: Int, flag: Boolean): UserAnswers = {
      answers.set(HasDirectorNINOId(index))(flag).asOpt.value
    }

    def directorEnterNINO(index: Int, nino: ReferenceValue): UserAnswers = {
      answers.set(DirectorEnterNINOId(index))(nino).asOpt.value
    }

    def directorNoNINOReason(index: Int, reason: String): UserAnswers = {
      answers.set(DirectorNoNINOReasonId(index))(reason).asOpt.value
    }

    def directorHasUTR(index: Int, flag: Boolean): UserAnswers = {
      answers.set(HasDirectorUTRId(index))(flag).asOpt.value
    }

    def directorEnterUTR(index: Int, utr: ReferenceValue): UserAnswers = {
      answers.set(DirectorEnterUTRId(index))(utr).asOpt.value
    }

    def directorNoUTRReason(index: Int, reason: String): UserAnswers = {
      answers.set(DirectorNoUTRReasonId(index))(reason).asOpt.value
    }

    def companyDirectorAddressList(index: Int, address: TolerantAddress): UserAnswers = {
      answers.set(CompanyDirectorAddressListId(index))(address).asOpt.value
    }

    def directorPreviousAddress(index: Int, address: Address): UserAnswers = {
      answers.set(DirectorPreviousAddressId(index))(address).asOpt.value
    }

    def directorPreviousAddressList(index: Int, address: TolerantAddress): UserAnswers = {
      answers.set(DirectorPreviousAddressListId(index))(address).asOpt.value
    }

    // Adviser
    def adviserAddress(address: Address): UserAnswers = {
      answers.set(AdviserAddressId)(address).asOpt.value
    }

    def adviserEmail(email: String): UserAnswers = {
      answers.set(AdviserEmailId)(email).asOpt.value
    }

    def adviserPhone(phone: String): UserAnswers = {
      answers.set(AdviserPhoneId)(phone).asOpt.value
    }

    def adviserAddressList(address: TolerantAddress): UserAnswers = {
      answers.set(AdviserAddressListId)(address).asOpt.value
    }

    def adviserName(name: String): UserAnswers = {
      answers.set(AdviserNameId)(name).asOpt.value
    }

    def variationWorkingKnowledge(value: Boolean): UserAnswers = {
      answers.set(VariationWorkingKnowledgeId)(value).asOpt.value
    }

    def declarationWorkingKnowledge(value: DeclarationWorkingKnowledge): UserAnswers = {
      answers.set(DeclarationWorkingKnowledgeId)(value).asOpt.value
    }

    def businessName(name: String = "test company"): UserAnswers = {
      answers.set(BusinessNameId)(name).asOpt.value
    }

    def businessType(businessType: BusinessType): UserAnswers = {
      answers.set(BusinessTypeId)(businessType).asOpt.value
    }

    def businessUtr(utr: String = "1111111111"): UserAnswers = {
      answers.set(BusinessUTRId)(utr).asOpt.value
    }

    def companyContactAddressList(addresses: Seq[TolerantAddress]): UserAnswers = {
      answers.set(CompanyContactAddressPostCodeLookupId)(addresses).asOpt.value
    }

    // Partnership

    def partnershipTradingOverAYear(flag: Boolean): UserAnswers = {
      answers.set(PartnershipTradingOverAYearId)(flag).asOpt.value
    }

    def partnershipContactAddressList(address: TolerantAddress): UserAnswers = {
      answers.set(PartnershipContactAddressListId)(address).asOpt.value
    }

    def partnershipSameContactAddress(areSame: Boolean): UserAnswers = {
      answers.set(PartnershipSameContactAddressId)(areSame).asOpt.value
    }

    def partnershipEmail(email: String): UserAnswers = {
      answers.set(PartnershipEmailId)(email).asOpt.value
    }

    def partnershipPhone(phone: String): UserAnswers = {
      answers.set(PartnershipPhoneId)(phone).asOpt.value
    }

    def companyAddressYears(underOrOverAddressYear: AddressYears): UserAnswers = {
      answers.set(CompanyAddressYearsId)(underOrOverAddressYear).asOpt.value
    }

    def partnershipAddressYears(underOrOverAddressYear: AddressYears): UserAnswers = {
      answers.set(PartnershipAddressYearsId)(underOrOverAddressYear).asOpt.value
    }

    def individualAddressYears(underOrOverAddressYear: AddressYears): UserAnswers = {
      answers.set(IndividualAddressYearsId)(underOrOverAddressYear).asOpt.value
    }

    def partnershipContactAddress(address: Address): UserAnswers = {
      answers.set(PartnershipContactAddressId)(address).asOpt.value
    }

    def partnershipRegisteredAddress(address: TolerantAddress): UserAnswers = {
      answers.set(PartnershipRegisteredAddressId)(address).asOpt.value
    }

    def partnershipPreviousAddress(address: Address): UserAnswers = {
      answers.set(PartnershipPreviousAddressId)(address).asOpt.value
    }

    //Partners

    def partnerName(index: Int, personName: PersonName): UserAnswers = {
      answers.set(PartnerNameId(index))(personName).asOpt.value
    }

    def partnerDob(index: Int, dob: LocalDate): UserAnswers = {
      answers.set(PartnerDOBId(index))(dob).asOpt.value
    }

    def partnerPreviousAddress(index: Int, address: Address): UserAnswers = {
      answers.set(PartnerPreviousAddressId(index))(address).asOpt.value
    }

    def partnerPreviousAddressList(index: Int, address: TolerantAddress): UserAnswers = {
      answers.set(PartnerPreviousAddressListId(index))(address).asOpt.value
    }

    def partnerHasNINO(index: Int, flag: Boolean): UserAnswers = {
      answers.set(HasPartnerNINOId(index))(flag).asOpt.value
    }

    def partnerEnterNINO(index: Int, nino: ReferenceValue): UserAnswers = {
      answers.set(PartnerEnterNINOId(index))(nino).asOpt.value
    }

    def partnerNoNINOReason(index: Int, reason: String): UserAnswers = {
      answers.set(PartnerNoNINOReasonId(index))(reason).asOpt.value
    }

    def partnerHasUTR(index: Int, flag: Boolean): UserAnswers = {
      answers.set(HasPartnerUTRId(index))(flag).asOpt.value
    }

    def partnerEnterUTR(index: Int, nino: ReferenceValue): UserAnswers = {
      answers.set(PartnerEnterUTRId(index))(nino).asOpt.value
    }

    def partnerNoUTRReason(index: Int, reason: String): UserAnswers = {
      answers.set(PartnerNoUTRReasonId(index))(reason).asOpt.value
    }

    def partnerAddress(index: Int, address: Address): UserAnswers = {
      answers.set(PartnerAddressId(index))(address).asOpt.value
    }

    def partnerAddressYears(index: Int, addressYears: AddressYears): UserAnswers = {
      answers.set(PartnerAddressYearsId(index))(addressYears).asOpt.value
    }

    def partnerEmail(index: Int, email: String): UserAnswers = {
      answers.set(PartnerEmailId(index))(email).asOpt.value
    }

    def partnerPhone(index: Int, phone: String): UserAnswers = {
      answers.set(PartnerPhoneId(index))(phone).asOpt.value
    }

    // Non-UK

    def registerAsBusiness(isBusiness: Boolean): UserAnswers = {
      answers.set(RegisterAsBusinessId)(isBusiness).asOpt.value
    }

    // Converters
    def dataRetrievalAction: DataRetrievalAction = {
      new FakeDataRetrievalAction(Some(answers.json))
    }

  }

}
