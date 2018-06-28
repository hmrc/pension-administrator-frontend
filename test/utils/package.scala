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

import controllers.actions.{DataRetrievalAction, FakeDataRetrievalAction}
import identifiers.LastPageId
import identifiers.register.adviser.{AdviserAddressId, AdviserAddressListId}
import identifiers.register.company._
import identifiers.register.company.directors.{CompanyDirectorAddressListId, DirectorAddressId, DirectorPreviousAddressId, DirectorPreviousAddressListId}
import identifiers.register.individual._
import identifiers.register.partnership.{PartnershipContactAddressListId, PartnershipDetailsId}
import models.{Address, BusinessDetails, LastPage, TolerantAddress}
import org.scalatest.OptionValues

package object utils {

  implicit class UserAnswerOps(answers: UserAnswers) extends OptionValues {

    def lastPage(page: LastPage): UserAnswers = {
      answers.set(LastPageId)(page).asOpt.value
    }

    // Individual PSA Contact
    def individualContactAddress(address: Address): UserAnswers = {
      answers.set(IndividualContactAddressId)(address).asOpt.value
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

    // Company director
    def directorAddress(index: Int, address: Address): UserAnswers = {
      answers.set(DirectorAddressId(index))(address).asOpt.value
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

    def adviserAddressList(address: TolerantAddress): UserAnswers = {
      answers.set(AdviserAddressListId)(address).asOpt.value
    }

    def businessDetails: UserAnswers = {
      answers.set(BusinessDetailsId)(BusinessDetails("test company", "1111111111")).asOpt.value
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


    // Converters
    def dataRetrievalAction: DataRetrievalAction = {
      new FakeDataRetrievalAction(Some(answers.json))
    }

  }

}
