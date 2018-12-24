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

package identifiers.register

import java.time.LocalDate

import identifiers.register.NonUKBusinessTypeIdSpec.{contactDetails, personDetails}
import identifiers.register.adviser.{AdviserAddressId, AdviserAddressListId, AdviserAddressPostCodeLookupId, AdviserDetailsId}
import identifiers.register.company._
import identifiers.register.company.directors.DirectorDetailsId
import identifiers.register.individual._
import identifiers.register.partnership._
import identifiers.register.partnership.partners.PartnerDetailsId
import models._
import models.register.adviser.AdviserDetails
import models.register.company.CompanyDetails
import models.register.{BusinessType, DeclarationWorkingKnowledge, NonUKBusinessType}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class AreYouInUKIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  import AreYouInUKIdSpec._

  "Cleanup for a company" when {

    "are you in uk has already answered as Yes and we change to No" must {
      val result: UserAnswers =
        companyAnswersForYes.set(AreYouInUKId)(false)
          .asOpt.value

      "remove all the uk company data " in {
        result.get(BusinessDetailsId) mustNot be(defined)
        result.get(BusinessTypeId) mustNot be(defined)
        result.get(ConfirmCompanyAddressId) mustNot be(defined)
        result.get(CompanySameContactAddressId) mustNot be(defined)
        result.get(CompanyContactAddressPostCodeLookupId) mustNot be(defined)
        result.get(CompanyAddressListId) mustNot be(defined)
        result.get(CompanyContactAddressId) mustNot be(defined)
        result.get(CompanyContactAddressListId) mustNot be(defined)
        result.get(CompanyAddressYearsId) mustNot be(defined)
        result.get(CompanyPreviousAddressId) mustNot be(defined)
        result.get(CompanyPreviousAddressPostCodeLookupId) mustNot be(defined)
        result.get(ContactDetailsId) mustNot be(defined)
        result.get(CompanyDetailsId) mustNot be(defined)
        result.get(CompanyRegistrationNumberId) mustNot be(defined)
        result.get(DirectorDetailsId(0)) mustNot be(defined)
        result.get(DirectorDetailsId(1)) mustNot be(defined)
        result.get(MoreThanTenDirectorsId) mustNot be(defined)
      }

      "remove the data for working knowledge and pension adviser " in {
        result.get(DeclarationWorkingKnowledgeId) mustNot be(defined)
        result.get(AdviserDetailsId) mustNot be(defined)
        result.get(AdviserAddressPostCodeLookupId) mustNot be(defined)
        result.get(AdviserAddressListId) mustNot be(defined)
        result.get(AdviserAddressId) mustNot be(defined)
      }

      "not remove the data for no uk business type" in {
        result.get(RegisterAsBusinessId) must be(defined)
      }
    }

    "where are you in uk has already answered as No and we change to Yes " must {
      val result: UserAnswers =
        companyAnswersForNo.set(AreYouInUKId)(true)
          .asOpt.value

      "remove all the non uk company data" in {
        result.get(NonUKBusinessTypeId) mustNot be(defined)
        result.get(CompanyAddressId) mustNot be(defined)
        result.get(CompanySameContactAddressId) mustNot be(defined)
        result.get(CompanyContactAddressPostCodeLookupId) mustNot be(defined)
        result.get(CompanyAddressListId) mustNot be(defined)
        result.get(CompanyContactAddressId) mustNot be(defined)
        result.get(CompanyContactAddressListId) mustNot be(defined)
        result.get(CompanyAddressYearsId) mustNot be(defined)
        result.get(CompanyPreviousAddressId) mustNot be(defined)
        result.get(CompanyPreviousAddressPostCodeLookupId) mustNot be(defined)
        result.get(ContactDetailsId) mustNot be(defined)
        result.get(DirectorDetailsId(0)) mustNot be(defined)
        result.get(DirectorDetailsId(1)) mustNot be(defined)
        result.get(MoreThanTenDirectorsId) mustNot be(defined)
      }

      "remove the data for working knowledge and pension adviser " in {
        result.get(DeclarationWorkingKnowledgeId) mustNot be(defined)
        result.get(AdviserDetailsId) mustNot be(defined)
        result.get(AdviserAddressPostCodeLookupId) mustNot be(defined)
        result.get(AdviserAddressListId) mustNot be(defined)
        result.get(AdviserAddressId) mustNot be(defined)
      }

      "not remove the data for uk company details" in {
        result.get(CompanyDetailsId) must be(defined)
      }
    }

    "where are you in uk has already answered as No and we change to No (i.e. don't change at all!)" must {
      val result: UserAnswers =
        companyAnswersForNo.set(AreYouInUKId)(false)
          .asOpt.value

      "not remove the data for Business details " in {
        result.get(BusinessDetailsId) must be(defined)
      }

      "not remove the data for non uk Business type " in {
        result.get(NonUKBusinessTypeId) must be(defined)
      }

      "not remove the data for non uk company address " in {
        result.get(CompanyAddressId) must be(defined)
      }
    }
  }

  "Cleanup for an individual" when {

    "where are you in uk has already answered as Yes and we change to No" must {
      val result: UserAnswers =
        individualAnswersForYes.set(AreYouInUKId)(false)
          .asOpt.value

      "remove all the uk individual data " in {
        result.get(IndividualDetailsCorrectId) mustNot be(defined)
        result.get(IndividualDetailsId) mustNot be(defined)
        result.get(IndividualDateOfBirthId) mustNot be(defined)
        result.get(RegistrationInfoId) mustNot be(defined)
        result.get(IndividualSameContactAddressId) mustNot be(defined)
        result.get(IndividualContactAddressListId) mustNot be(defined)
        result.get(IndividualAddressId) mustNot be(defined)
        result.get(IndividualAddressYearsId) mustNot be(defined)
        result.get(IndividualPreviousAddressListId) mustNot be(defined)
        result.get(IndividualPreviousAddressPostCodeLookupId) mustNot be(defined)
        result.get(IndividualPreviousAddressId) mustNot be(defined)
        result.get(IndividualContactDetailsId) mustNot be(defined)
      }
    }

    "where are you in uk has already answered as No and we change to Yes " must {
      val result: UserAnswers =
        individualAnswersForNo.set(AreYouInUKId)(true)
          .asOpt.value

      "remove all the non uk individual data " in {
        result.get(IndividualDetailsId) mustNot be(defined)
        result.get(IndividualDateOfBirthId) mustNot be(defined)
        result.get(RegistrationInfoId) mustNot be(defined)
        result.get(IndividualSameContactAddressId) mustNot be(defined)
        result.get(IndividualAddressId) mustNot be(defined)
        result.get(IndividualAddressYearsId) mustNot be(defined)
        result.get(IndividualPreviousAddressListId) mustNot be(defined)
        result.get(IndividualPreviousAddressId) mustNot be(defined)
        result.get(IndividualContactDetailsId) mustNot be(defined)
      }

      "not remove the data for Individual Details Correct " in {
        result.get(IndividualDetailsCorrectId) must be(defined)
      }
    }

    "where are you in uk has already answered as No and we change to No (i.e. don't change at all!)" must {
      val result: UserAnswers =
        individualAnswersForNo.set(AreYouInUKId)(false)
          .asOpt.value

      "not remove the data for non uk Individual Details" in {
        result.get(IndividualDetailsId) must be(defined)
      }

      "not remove the data for Individual date of birth" in {
        result.get(IndividualDateOfBirthId) must be(defined)
      }

      "not remove the data for Individual Address" in {
        result.get(IndividualAddressId) must be(defined)
      }
    }
  }

  "Cleanup for a partnership" when {

    "where are you in uk has already answered as Yes and we change to No" must {
      val result: UserAnswers =
        partnershipAnswersForYes.set(AreYouInUKId)(false)
          .asOpt.value

      "remove all the uk partnership data " in {
        result.get(PartnershipDetailsId) mustNot be(defined)
        result.get(BusinessTypeId) mustNot be(defined)
        result.get(ConfirmPartnershipDetailsId) mustNot be(defined)
        result.get(PartnershipSameContactAddressId) mustNot be(defined)
        result.get(PartnershipContactAddressPostCodeLookupId) mustNot be(defined)
        result.get(PartnershipContactAddressListId) mustNot be(defined)
        result.get(PartnershipContactAddressId) mustNot be(defined)
        result.get(PartnershipAddressYearsId) mustNot be(defined)
        result.get(PartnershipPreviousAddressId) mustNot be(defined)
        result.get(PartnershipPreviousAddressPostCodeLookupId) mustNot be(defined)
        result.get(PartnershipPreviousAddressListId) mustNot be(defined)
        result.get(PartnershipContactDetailsId) mustNot be(defined)
        result.get(CompanyDetailsId) mustNot be(defined)
        result.get(PartnershipVatId) mustNot be(defined)
        result.get(PartnershipPayeId) mustNot be(defined)
        result.get(PartnerDetailsId(0)) mustNot be(defined)
        result.get(PartnerDetailsId(1)) mustNot be(defined)
        result.get(MoreThanTenPartnersId) mustNot be(defined)
      }

      "not remove the data for non uk register as business" in {
        result.get(RegisterAsBusinessId) must be(defined)
      }
    }

    "where are you in uk has already answered as No and we change to Yes " must {
      val result: UserAnswers =
        partnershipAnswersForNo.set(AreYouInUKId)(true)
          .asOpt.value

      "remove all the data for non uk partnership" in {
        result.get(PartnershipDetailsId) mustNot be(defined)
        result.get(BusinessTypeId) mustNot be(defined)
        result.get(PartnershipSameContactAddressId) mustNot be(defined)
        result.get(PartnershipRegisteredAddressId) mustNot be(defined)
        result.get(PartnershipContactAddressPostCodeLookupId) mustNot be(defined)
        result.get(PartnershipContactAddressListId) mustNot be(defined)
        result.get(PartnershipContactAddressId) mustNot be(defined)
        result.get(PartnershipAddressYearsId) mustNot be(defined)
        result.get(PartnershipPreviousAddressId) mustNot be(defined)
        result.get(PartnershipPreviousAddressPostCodeLookupId) mustNot be(defined)
        result.get(PartnershipPreviousAddressListId) mustNot be(defined)
        result.get(PartnershipContactDetailsId) mustNot be(defined)
        result.get(CompanyDetailsId) mustNot be(defined)
        result.get(PartnershipVatId) mustNot be(defined)
        result.get(PartnershipPayeId) mustNot be(defined)
        result.get(PartnerDetailsId(0)) mustNot be(defined)
        result.get(PartnerDetailsId(1)) mustNot be(defined)
        result.get(MoreThanTenPartnersId) mustNot be(defined)
      }

    }

    "where are you in uk has already answered as No and we change to No (i.e. don't change at all!)" must {
      val result: UserAnswers =
        partnershipAnswersForNo.set(AreYouInUKId)(false)
          .asOpt.value

      "not remove the data for Business details " in {
        result.get(PartnershipDetailsId) must be(defined)
      }

      "not remove the data for non uk Business type " in {
        result.get(NonUKBusinessTypeId) must be(defined)
      }

      "not remove the data for non uk company address " in {
        result.get(PartnershipRegisteredAddressId) must be(defined)
      }
    }

  }
}

object AreYouInUKIdSpec extends OptionValues {

  val tolerantAddress = TolerantAddress(Some("line 1"), Some("line 2"), Some("line 3"), Some("line 4"), None, Some("DE"))
  val address = Address("line 1", "line 2", Some("line 3"), Some("line 4"), None, "UK")
  val tolerantIndividual = TolerantIndividual(Some("firstName"), Some("middleName"), Some("lastName"))
  val date = LocalDate.now()
  val registrationUK = RegistrationInfo(RegistrationLegalStatus.Individual, "", false, RegistrationCustomerType.UK, None, None)

  private def setCommonCompanyData(userAnswers: UserAnswers) = {
    userAnswers.set(CompanyContactAddressPostCodeLookupId)(Seq(tolerantAddress))
      .flatMap(_.set(BusinessDetailsId)(BusinessDetails("test company", Some("utr"))))
        .flatMap(_.set(CompanyAddressListId)(tolerantAddress))
        .flatMap(_.set(CompanyContactAddressId)(address))
        .flatMap(_.set(CompanyContactAddressListId)(tolerantAddress))
        .flatMap(_.set(CompanyAddressYearsId)(AddressYears.OverAYear))
        .flatMap(_.set(CompanyPreviousAddressId)(address))
        .flatMap(_.set(CompanyPreviousAddressPostCodeLookupId)(Seq(tolerantAddress)))
        .flatMap(_.set(ContactDetailsId)(contactDetails))
        .flatMap(_.set(DirectorDetailsId(0))(personDetails))
        .flatMap(_.set(DirectorDetailsId(1))(personDetails))
        .flatMap(_.set(MoreThanTenDirectorsId)(true))
        .flatMap(_.set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.Adviser))
        .flatMap(_.set(AdviserDetailsId)(AdviserDetails("name", "email@test.com", "678")))
        .flatMap(_.set(AdviserAddressPostCodeLookupId)(Seq(tolerantAddress)))
        .flatMap(_.set(AdviserAddressListId)(tolerantAddress))
        .flatMap(_.set(CompanySameContactAddressId)(false))
        .flatMap(_.set(AdviserAddressId)(address))
  }

  private def setCommonIndividualData(userAnswers: UserAnswers) = {
    userAnswers.set(IndividualDetailsCorrectId)(true)
      .flatMap(_.set(IndividualAddressYearsId)(AddressYears.OverAYear))
      .flatMap(_.set(IndividualPreviousAddressListId)(tolerantAddress))
      .flatMap(_.set(IndividualPreviousAddressId)(address))
      .flatMap(_.set(IndividualContactDetailsId)(contactDetails))
      .flatMap(_.set(IndividualDateOfBirthId)(date))
      .flatMap(_.set(IndividualSameContactAddressId)(true))
      .flatMap(_.set(IndividualDetailsId)(tolerantIndividual))
      .flatMap(_.set(IndividualAddressId)(tolerantAddress))
      .flatMap(_.set(RegistrationInfoId)(registrationUK))
  }

  private def setCommonPartnershipData(userAnswers: UserAnswers) = {
    userAnswers.set(PartnershipSameContactAddressId)(false)
      .flatMap(_.set(PartnershipDetailsId)(BusinessDetails("test company", Some("utr"))))
      .flatMap(_.set(BusinessTypeId)(BusinessType.BusinessPartnership))
      .flatMap(_.set(PartnershipContactAddressPostCodeLookupId)(Seq(tolerantAddress)))
      .flatMap(_.set(PartnershipContactAddressListId)(tolerantAddress))
      .flatMap(_.set(PartnershipContactAddressId)(address))
      .flatMap(_.set(PartnershipAddressYearsId)(AddressYears.OverAYear))
      .flatMap(_.set(PartnershipPreviousAddressId)(address))
      .flatMap(_.set(PartnershipPreviousAddressPostCodeLookupId)(Seq(tolerantAddress)))
      .flatMap(_.set(PartnershipPreviousAddressListId)(tolerantAddress))
      .flatMap(_.set(PartnershipContactDetailsId)(contactDetails))
      .flatMap(_.set(PartnerDetailsId(0))(personDetails))
      .flatMap(_.set(PartnerDetailsId(1))(personDetails))
      .flatMap(_.set(MoreThanTenPartnersId)(true))
      .flatMap(_.set(DeclarationWorkingKnowledgeId)(DeclarationWorkingKnowledge.Adviser))
      .flatMap(_.set(AdviserDetailsId)(AdviserDetails("name", "email@test.com", "678")))
      .flatMap(_.set(AdviserAddressPostCodeLookupId)(Seq(tolerantAddress)))
      .flatMap(_.set(AdviserAddressListId)(tolerantAddress))
      .flatMap(_.set(AdviserAddressId)(address))
  }

  val companyAnswersForYes: UserAnswers = UserAnswers(Json.obj())
    .set(AreYouInUKId)(true)
    .flatMap(setCommonCompanyData)
    .flatMap(_.set(BusinessTypeId)(BusinessType.LimitedCompany))
    .flatMap(_.set(ConfirmCompanyAddressId)(tolerantAddress))
    .flatMap(_.set(CompanyDetailsId)(CompanyDetails(None, None)))
    .flatMap(_.set(CompanyRegistrationNumberId)("test reg no"))
    .flatMap(_.set(RegisterAsBusinessId)(true))
    .asOpt.value

  val companyAnswersForNo: UserAnswers = UserAnswers(Json.obj())
    .set(AreYouInUKId)(false)
    .flatMap(setCommonCompanyData)
      .flatMap(_.set(RegisterAsBusinessId)(true))
      .flatMap(_.set(CompanyAddressId)(tolerantAddress))
      .flatMap(_.set(NonUKBusinessTypeId)(NonUKBusinessType.Company))
    .flatMap(_.set(CompanyDetailsId)(CompanyDetails(None, None)))
    .asOpt.value

  val individualAnswersForYes: UserAnswers = UserAnswers(Json.obj())
    .set(AreYouInUKId)(true)
    .flatMap(setCommonIndividualData)
      .flatMap(_.set(IndividualContactAddressListId)(tolerantAddress))
      .flatMap(_.set(IndividualPreviousAddressPostCodeLookupId)(Seq(tolerantAddress)))
    .asOpt.value

  val individualAnswersForNo: UserAnswers = UserAnswers(Json.obj())
    .set(AreYouInUKId)(false)
    .flatMap(setCommonIndividualData)
    .asOpt.value

  val partnershipAnswersForYes = UserAnswers(Json.obj())
    .set(AreYouInUKId)(true)
    .flatMap(setCommonPartnershipData)
    .flatMap(_.set(ConfirmPartnershipDetailsId)(true))
    .flatMap(_.set(PartnershipVatId)(Vat.No))
    .flatMap(_.set(PartnershipPayeId)(Paye.No))
    .flatMap(_.set(RegisterAsBusinessId)(true))
    .asOpt.value

  val partnershipAnswersForNo = UserAnswers(Json.obj())
    .set(AreYouInUKId)(false)
    .flatMap(setCommonPartnershipData)
    .flatMap(_.set(PartnershipSameContactAddressId)(false))
    .flatMap(_.set(PartnershipRegisteredAddressId)(tolerantAddress))
    .flatMap(_.set(RegisterAsBusinessId)(true))
    .asOpt.value
}
