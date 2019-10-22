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

import controllers.actions.{DataRetrievalAction, FakeDataRetrievalAction}
import identifiers.LastPageId
import identifiers.register.adviser._
import identifiers.register.company._
import identifiers.register.company.directors._
import identifiers.register.individual._
import identifiers.register.partnership.partners.{PartnerAddressId, PartnerPreviousAddressId, PartnerPreviousAddressListId}
import identifiers.register.partnership.{PartnershipContactAddressListId, PartnershipDetailsId, _}
import identifiers.register.{BusinessNameId, RegisterAsBusinessId, RegistrationInfoId, VariationWorkingKnowledgeId}
import models._
import models.register.adviser.AdviserDetails
import org.scalatest.OptionValues
import views.html.phone


package object utils {

  //scalastyle:off number.of.methods
  implicit class UserAnswerOps(answers: UserAnswers) extends OptionValues {

    def lastPage(page: LastPage): UserAnswers = {
      answers.set(LastPageId)(page).asOpt.value
    }

    def registrationInfo(answer: RegistrationInfo): UserAnswers = {
      answers.set(RegistrationInfoId)(answer).asOpt.value
    }

    def areYouInUk(answer: Boolean): UserAnswers = {
      answers.set(identifiers.register.AreYouInUKId)(answer).asOpt.value
    }

    def isRegisteredName(answer: Boolean): UserAnswers = {
      answers.set(identifiers.register.IsRegisteredNameId)(answer).asOpt.value
    }

    // Individual PSA Contact
    def individualContactAddress(address: Address): UserAnswers = {
      answers.set(IndividualContactAddressId)(address).asOpt.value
    }

    def individualDetails(individualDetails: TolerantIndividual): UserAnswers = {
      answers.set(IndividualDetailsId)(individualDetails).asOpt.value
    }

    def individualContactAddressList(address: TolerantAddress): UserAnswers = {
      answers.set(IndividualContactAddressListId)(address).asOpt.value
    }

    // Individual PSA
    def individualPreviousAddress(address: Address): UserAnswers = {
      answers.set(IndividualPreviousAddressId)(address).asOpt.value
    }

    def individualPreviousAddressList(address: TolerantAddress): UserAnswers = {
      answers.set(IndividualPreviousAddressListId)(address).asOpt.value
    }

    def nonUkIndividualAddress(address: Address): UserAnswers = {
      answers.set(IndividualAddressId)(address.toTolerantAddress).asOpt.value
    }

    // Company PSA
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
    def directorAddress(index: Int, address: Address): UserAnswers = {
      answers.set(DirectorAddressId(index))(address).asOpt.value
    }

    def directorPhone(index: Int, phone: String): UserAnswers = {
      answers.set(DirectorPhoneId(index))(phone).asOpt.value
    }

    def directorDetails(index: Int, details: PersonDetails): UserAnswers = {
      answers.set(DirectorDetailsId(index))(details).asOpt.value
    }

    def directorEmail(index: Int, email: String): UserAnswers = {
      answers.set(DirectorEmailId(index))(email).asOpt.value
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

    def adviserDetails(details: AdviserDetails): UserAnswers = {
      answers.set(AdviserDetailsId)(details).asOpt.value
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

    def businessDetails: UserAnswers = {
      answers.set(BusinessDetailsId)(BusinessDetails("test company", Some("1111111111"))).asOpt.value
    }

    def businessName: UserAnswers = {
      answers.set(BusinessNameId)("test company").asOpt.value
    }

    def companyContactAddressList(addresses: Seq[TolerantAddress]): UserAnswers = {
      answers.set(CompanyContactAddressPostCodeLookupId)(addresses).asOpt.value
    }

    // Partnership
    def partnershipDetails(details: models.BusinessDetails): UserAnswers = {
      answers.set(PartnershipDetailsId)(details).asOpt.value
    }

    def partnershipContactAddressList(address: TolerantAddress): UserAnswers = {
      answers.set(PartnershipContactAddressListId)(address).asOpt.value
    }

    def partnershipSameContactAddress(areSame: Boolean): UserAnswers = {
      answers.set(PartnershipSameContactAddressId)(areSame).asOpt.value
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

    def partnershipContactDetails(contactDetails: ContactDetails): UserAnswers = {
      answers.set(PartnershipContactDetailsId)(contactDetails).asOpt.value
    }

    //Partners

    def partnerPreviousAddress(index: Int, address: Address): UserAnswers = {
      answers.set(PartnerPreviousAddressId(index))(address).asOpt.value
    }

    def partnerPreviousAddressList(index: Int, address: TolerantAddress): UserAnswers = {
      answers.set(PartnerPreviousAddressListId(index))(address).asOpt.value
    }


    def partnerAddress(index: Int, address: Address): UserAnswers = {
      answers.set(PartnerAddressId(index))(address).asOpt.value
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
