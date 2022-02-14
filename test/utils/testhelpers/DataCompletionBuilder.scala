/*
 * Copyright 2022 HM Revenue & Customs
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

package utils.testhelpers

import java.time.LocalDate
import models._
import models.register.BusinessType
import org.scalatest.OptionValues
import utils.UserAnswers

object DataCompletionBuilder {

  implicit class DataCompletionUserAnswerOps(answers: UserAnswers) extends OptionValues {
    private val address = Address("Telford1", "Telford2", None, None, Some("TF3 4ER"), "GB")
    private val email = "test@test.com"
    private val phone = "111"
    private val reason = "not available"
    private val dob = LocalDate.now().minusYears(20)

    def completeDirector(index: Int, isDeleted: Boolean = false): UserAnswers =
      answers
        .directorName(index, PersonName(s"first$index", s"last$index", isDeleted))
        .directorDob(index, dob)
        .directorHasNINO(index, flag = false)
        .directorNoNINOReason(index, reason)
        .directorHasUTR(index, flag = false)
        .directorNoUTRReason(index, reason)
        .directorAddress(index, address)
        .directorAddressYears(index, AddressYears.OverAYear)
        .directorEmail(index, email)
        .directorPhone(index, phone)

    def completePartner(index: Int, isDeleted: Boolean = false): UserAnswers =
      answers
        .partnerName(index, PersonName(s"first$index", s"last$index", isDeleted))
        .partnerDob(index, dob)
        .partnerHasNINO(index, flag = false)
        .partnerNoNINOReason(index, reason)
        .partnerHasUTR(index, flag = false)
        .partnerNoUTRReason(index, reason)
        .partnerAddress(index, address)
        .partnerAddressYears(index, AddressYears.OverAYear)
        .partnerEmail(index, email)
        .partnerPhone(index, phone)

    def completeIndividual: UserAnswers =
      answers
        .individualAddress(address)
        .individualSameContactAddress(areSame = true)
        .individualDetails(TolerantIndividual(Some("first"), None, Some("last")))
        .individualDob(dob)
        .individualContactAddress(address)
        .individualAddressYears(AddressYears.UnderAYear)
        .individualPreviousAddress(address)
        .individualEmail(email)
        .individualPhone(phone)
        .registrationInfo(
          RegistrationInfo(
            legalStatus = RegistrationLegalStatus.Individual,
            sapNumber = "test-sap",
            noIdentifier = false,
            customerType = RegistrationCustomerType.UK,
            idType = None,
            idNumber = None
          )
        )

    def completeIndividualNotSameAddress: UserAnswers =
      answers
        .individualAddress(address)
        .individualSameContactAddress(areSame = false)
        .individualDetails(TolerantIndividual(Some("first"), None, Some("last")))
        .individualDob(dob)
        .individualContactAddress(address)
        .individualAddressYears(AddressYears.UnderAYear)
        .individualPreviousAddress(address)
        .individualEmail(email)
        .individualPhone(phone)
        .registrationInfo(
          RegistrationInfo(
            legalStatus = RegistrationLegalStatus.Individual,
            sapNumber = "test-sap",
            noIdentifier = false,
            customerType = RegistrationCustomerType.UK,
            idType = None,
            idNumber = None
          )
        )

    def completeIndividualVariations: UserAnswers =
      answers
        .individualDetails(TolerantIndividual(Some("first"), None, Some("last")))
        .individualDob(dob)
        .individualContactAddress(address)
        .individualAddressYears(AddressYears.OverAYear)
        .individualEmail(email)
        .individualPhone(phone)
        .registrationInfo(
          RegistrationInfo(
            legalStatus = RegistrationLegalStatus.Individual,
            sapNumber = "test-sap",
            noIdentifier = false,
            customerType = RegistrationCustomerType.UK,
            idType = None,
            idNumber = None
          )
        )

    def completeCompanyDetailsUK: UserAnswers =
      answers
        .areYouInUk(answer = true)
        .businessType(BusinessType.LimitedCompany)
        .regInfo(RegistrationLegalStatus.LimitedCompany)
        .hasVat(true)
        .enterVat("test-vat")
        .companyHasCrn(true)
        .businessUtr()
        .companyCrn(crn = "test-crn")
        .hasPaye(flag = true)
        .enterPaye(paye = "test-paye")
        .companyContactAddress(address)
        .companyAddressYears(AddressYears.UnderAYear)
        .companyTradingOverAYear(flag = true)
        .companyPreviousAddress(address)
        .companyEmail(email)
        .companyPhone(phone)
        .businessName()

    def completePartnershipDetailsUK: UserAnswers =
      answers
        .areYouInUk(answer = true)
        .businessType(BusinessType.LimitedPartnership)
        .regInfo(RegistrationLegalStatus.Partnership)
        .businessUtr()
        .hasVat(answer = true)
        .hasPaye(flag = true)
        .enterPaye(paye = "test-paye")
        .enterVat(vat = "test-vat")
        .partnershipContactAddress(address)
        .partnershipAddressYears(AddressYears.UnderAYear)
        .partnershipTradingOverAYear(flag = true)
        .partnershipPreviousAddress(address)
        .partnershipEmail(email)
        .partnershipPhone(phone)
        .businessName("limited partnership")

    def completeCompanyDetailsNonUK: UserAnswers =
      answers
        .areYouInUk(answer = false)
        .businessType(BusinessType.LimitedCompany)
        .regInfo(RegistrationLegalStatus.LimitedCompany)
        .businessName()
        .nonUkCompanyAddress(address)
        .companyContactAddress(address)
        .companyAddressYears(AddressYears.UnderAYear)
        .companyTradingOverAYear(flag = true)
        .companyPreviousAddress(address)
        .companyEmail(email)
        .companyPhone(phone)

    def completePartnershipDetailsNonUK: UserAnswers =
      answers
        .areYouInUk(answer = false)
        .businessType(BusinessType.LimitedPartnership)
        .businessName("limited partnership")
        .regInfo(RegistrationLegalStatus.Partnership)
        .nonUkPartnershipAddress(address)
        .partnershipContactAddress(address)
        .partnershipAddressYears(AddressYears.UnderAYear)
        .partnershipTradingOverAYear(flag = true)
        .partnershipPreviousAddress(address)
        .partnershipEmail(email)
        .partnershipPhone(phone)

    def regInfo(legalStatus: RegistrationLegalStatus): UserAnswers =
      answers
        .registrationInfo(
          RegistrationInfo(
            legalStatus = legalStatus,
            sapNumber = "test-sap",
            noIdentifier = false,
            customerType = RegistrationCustomerType.UK,
            idType = None,
            idNumber = None
          )
        )
  }

}
