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

package utils.dataCompletion

import identifiers.register.company._
import identifiers.register.company.directors.{DirectorEnterNINOId, DirectorNoNINOReasonId, HasDirectorNINOId}
import identifiers.register.{EnterVATId, HasVATId}
import models._
import models.register.DeclarationWorkingKnowledge
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import utils.UserAnswers
import utils.testhelpers.DataCompletionBuilder.DataCompletionUserAnswerOps

class DataCompletionSpec extends WordSpec with MustMatchers with OptionValues {

  val address = Address("Telford1", "Telford2", Some("Telford3"), Some("Telford4"), Some("TF3 4ER"), "GB")

  "DataCompletion" when {
    "calling isComplete" must {
      "return true only when all the values in list are true" in {
        DataCompletion.isComplete(Seq(Some(true), Some(true), Some(true))).value mustBe true
        DataCompletion.isComplete(Seq(Some(true), Some(false), Some(true))).value mustBe false
      }

      "return None only when all the values in list are None" in {
        DataCompletion.isComplete(Seq(None, None, None, None)) mustBe None
        DataCompletion.isComplete(Seq(None, Some(false), Some(true))) mustBe Some(false)
      }

      "return false in every other case" in {
        DataCompletion.isComplete(Seq(Some(true), None, Some(false), None)) mustBe Some(false)
        DataCompletion.isComplete(Seq(None, Some(true), Some(true))) mustBe Some(false)
      }
    }

    "calling isListComplete" must {
      "return true only when all values in list are true" in {
        DataCompletion.isListComplete(Seq(true, true, true)) mustBe true
      }

      "return false in every other case" in {
        DataCompletion.isListComplete(Seq(true, false, true)) mustBe false
      }
    }

    "calling isAddressComplete" must {

      "return false" when {
        val userAnswers = UserAnswers().companyContactAddress(address)

        "when current Address is missing" in {
          DataCompletion.isAddressComplete(UserAnswers(), CompanyContactAddressId, CompanyPreviousAddressId,
            CompanyAddressYearsId, Some(CompanyTradingOverAYearId), CompanyConfirmPreviousAddressId) mustBe None
        }

        "when current address is present but address years is missing" in {
          DataCompletion.isAddressComplete(userAnswers, CompanyContactAddressId, CompanyPreviousAddressId,
            CompanyAddressYearsId, Some(CompanyTradingOverAYearId), CompanyConfirmPreviousAddressId).value mustBe false
        }

        "when current address is present, address years is under a year but no previous address present" in {
          DataCompletion.isAddressComplete(userAnswers.companyAddressYears(AddressYears.UnderAYear),
            CompanyContactAddressId, CompanyPreviousAddressId, CompanyAddressYearsId, Some(CompanyTradingOverAYearId),
            CompanyConfirmPreviousAddressId).value mustBe false
        }

        "when current address is present, address years is under a year, trading over a years is yes but no previous address present" in {
          DataCompletion.isAddressComplete(userAnswers.companyAddressYears(AddressYears.UnderAYear).companyTradingOverAYear(flag = true),
            CompanyContactAddressId, CompanyPreviousAddressId, CompanyAddressYearsId, Some(CompanyTradingOverAYearId),
            CompanyConfirmPreviousAddressId).value mustBe false
        }

        "when current address is present, is this previous address true and previous address is not present" in {
          val ua = userAnswers.companyAddressYears(AddressYears.UnderAYear).
            companyIsThisPreviousAddress(flag = true)

          DataCompletion.isAddressComplete(ua, CompanyContactAddressId, CompanyPreviousAddressId, CompanyAddressYearsId,
            Some(CompanyTradingOverAYearId), CompanyConfirmPreviousAddressId).value mustBe false
        }

        "when current address is present, is this previous address false and previous address is not present" in {
          val ua = userAnswers.companyAddressYears(AddressYears.UnderAYear).
            companyIsThisPreviousAddress(flag = false)

          DataCompletion.isAddressComplete(ua, CompanyContactAddressId, CompanyPreviousAddressId, CompanyAddressYearsId,
            Some(CompanyTradingOverAYearId), CompanyConfirmPreviousAddressId).value mustBe false
        }
      }

      "return true" when {
        val userAnswers = UserAnswers().companyContactAddress(address)

        "when current address is present and address years is over a year" in {
          DataCompletion.isAddressComplete(userAnswers.companyAddressYears(AddressYears.OverAYear), CompanyContactAddressId, CompanyPreviousAddressId,
            CompanyAddressYearsId, Some(CompanyTradingOverAYearId), CompanyConfirmPreviousAddressId).value mustBe true
        }

        "when current address is present, address years is under a year and trading over a year is false" in {
          DataCompletion.isAddressComplete(userAnswers.companyAddressYears(AddressYears.UnderAYear).
            companyTradingOverAYear(flag = false), CompanyContactAddressId, CompanyPreviousAddressId,
            CompanyAddressYearsId, Some(CompanyTradingOverAYearId), CompanyConfirmPreviousAddressId).value mustBe true
        }

        "when current address is present, address years is under a year, trading over a year is true and previous address is present" in {
          DataCompletion.isAddressComplete(userAnswers.companyAddressYears(AddressYears.UnderAYear).
            companyTradingOverAYear(flag = true).companyPreviousAddress(address),
            CompanyContactAddressId, CompanyPreviousAddressId, CompanyAddressYearsId, Some(CompanyTradingOverAYearId),
            CompanyConfirmPreviousAddressId).value mustBe true
        }

        "when current address is present, is this previous address true and previous address is present" in {
          val ua = userAnswers.companyAddressYears(AddressYears.UnderAYear).
            companyIsThisPreviousAddress(flag = true).companyPreviousAddress(address)

          DataCompletion.isAddressComplete(ua, CompanyContactAddressId, CompanyPreviousAddressId, CompanyAddressYearsId,
            Some(CompanyTradingOverAYearId), CompanyConfirmPreviousAddressId).value mustBe true
        }

        "when current address is present, is this previous address false and previous address is present" in {
          val ua = userAnswers.companyAddressYears(AddressYears.UnderAYear).
            companyIsThisPreviousAddress(flag = false).companyPreviousAddress(address)

          DataCompletion.isAddressComplete(ua, CompanyContactAddressId, CompanyPreviousAddressId, CompanyAddressYearsId,
            Some(CompanyTradingOverAYearId), CompanyConfirmPreviousAddressId).value mustBe true
        }
      }
    }


    "isAnswerComplete" must {
      "return None when answer is missing" in {
        DataCompletion.isAnswerComplete(UserAnswers(), CompanyRegistrationNumberId) mustBe None
      }

      "return true when answer is present" in {
        val ua = UserAnswers().companyCrn(crn = "test-srn")
        DataCompletion.isAnswerComplete(ua, CompanyRegistrationNumberId).value mustBe true
      }
    }

    "isAnswerComplete for yes no answers" must {

      "return None when answer is missing" in {
        DataCompletion.isAnswerComplete(UserAnswers(), HasVATId, EnterVATId, None) mustBe None
      }

      "return true when answer for yes value is present" in {
        val ua = UserAnswers().hasVat(true).enterVat("test-vat")
        DataCompletion.isAnswerComplete(ua, HasVATId, EnterVATId, None).value mustBe true
      }

      "return true when answer for no - reason is present" in {
        val ua = UserAnswers().directorHasNINO(index = 0, flag = false).directorNoNINOReason(index = 0, reason = "no nino")
        DataCompletion.isAnswerComplete(ua, HasDirectorNINOId(0), DirectorEnterNINOId(0), Some(DirectorNoNINOReasonId(0))).value mustBe true
      }

      "return true when has value is false and reason is not needed" in {
        val ua = UserAnswers().hasVat(false)
        DataCompletion.isAnswerComplete(ua, HasVATId, EnterVATId, None).value mustBe true
      }

      "return false when answer for yes value is missing" in {
        val ua = UserAnswers().hasVat(true)
        DataCompletion.isAnswerComplete(ua, HasVATId, EnterVATId, None).value mustBe false
      }

      "return false when answer for no - reason is missing" in {
        val ua = UserAnswers().directorHasNINO(index = 0, flag = false)
        DataCompletion.isAnswerComplete(ua, HasDirectorNINOId(0), DirectorEnterNINOId(0), Some(DirectorNoNINOReasonId(0))).value mustBe false
      }
    }

    "isCompanyDetailsComplete" must {

      "return false" when {

        "no answers are present" in {
          DataCompletion.isCompanyDetailsComplete(UserAnswers()) mustBe false
        }

        "some answers are missing when the company is in UK" in {
          val ua = UserAnswers().areYouInUk(answer = true).businessName().businessUtr().
            companyEmail(email = "s@s.com")
          DataCompletion.isCompanyDetailsComplete(ua) mustBe false
        }

        "all answers are complete when the company is in UK" in {
          val ua = UserAnswers().completeCompanyDetailsUK
          DataCompletion.isCompanyDetailsComplete(ua) mustBe true
        }

        "some answers are missing when the company is in NON UK" in {
          val ua = UserAnswers().areYouInUk(answer = false).businessName().nonUkCompanyAddress(address).
            companyEmail(email = "s@s.com")
          DataCompletion.isCompanyDetailsComplete(ua) mustBe false
        }

        "all answers are complete when the company is in NON UK" in {
          val ua = UserAnswers().completeCompanyDetailsNonUK
          DataCompletion.isCompanyDetailsComplete(ua) mustBe true
        }
      }
    }

    "isCompanyComplete" must {
      "return false" when {

        "company details is complete but no directors are present" in {
          val ua = UserAnswers().completeCompanyDetailsUK
          DataCompletion.isCompanyComplete(ua, NormalMode) mustBe false
        }

        "company details is complete but one director is incomplete" in {
          val ua = UserAnswers().completeCompanyDetailsUK.completeDirector(index = 0).
            directorName(index = 1, name = PersonName("first", "last"))
          DataCompletion.isCompanyComplete(ua, NormalMode) mustBe false
        }

        "all directors are complete but company details is not complete" in {
          val ua = UserAnswers().businessName().businessUtr().completeDirector(index = 0)
          DataCompletion.isCompanyComplete(ua, NormalMode) mustBe false
        }
      }

      "return true" when {
        "the company details and all the directors are complete" in {
          val ua = UserAnswers().completeCompanyDetailsUK.completeDirector(index = 0).completeDirector(index = 1)
          DataCompletion.isCompanyComplete(ua, NormalMode) mustBe true
        }
      }
    }

    "isPartnershipDetailsComplete" must {

      "return false" when {

        "no answers are present" in {
          DataCompletion.isPartnershipDetailsComplete(UserAnswers()) mustBe false
        }

        "some answers are missing when the partnership is in UK" in {
          val ua = UserAnswers().areYouInUk(answer = true).businessName().businessUtr().
            partnershipEmail("s@s.com")
          DataCompletion.isPartnershipDetailsComplete(ua) mustBe false
        }

        "all answers are complete when the partnership is in UK" in {
          val ua = UserAnswers().completePartnershipDetailsUK
          DataCompletion.isPartnershipDetailsComplete(ua) mustBe true
        }

        "some answers are missing when the partnership is in NON UK" in {
          val ua = UserAnswers().areYouInUk(false).businessName().nonUkPartnershipAddress(address).
            companyEmail("s@s.com")
          DataCompletion.isPartnershipDetailsComplete(ua) mustBe false
        }

        "all answers are complete when the partnership is in NON UK" in {
          val ua = UserAnswers().completePartnershipDetailsNonUK
          DataCompletion.isPartnershipDetailsComplete(ua) mustBe true
        }
      }
    }

    "isPartnershipComplete" must {
      "return false" when {

        "partnership details is complete but no partners are present" in {
          val ua = UserAnswers().completePartnershipDetailsUK
          DataCompletion.isPartnershipComplete(ua, NormalMode) mustBe false
        }

        "partnership details is complete but one partner is incomplete" in {
          val ua = UserAnswers().completePartnershipDetailsUK.completePartner(index = 0).
            partnerName(index = 1, personName = PersonName("first", "last"))
          DataCompletion.isPartnershipComplete(ua, NormalMode) mustBe false
        }

        "all partnership are complete but partnership details is not complete" in {
          val ua = UserAnswers().businessName().businessUtr().completePartner(index = 0)
          DataCompletion.isPartnershipComplete(ua, NormalMode) mustBe false
        }
      }

      "return true" when {
        "the partnership details and all the partners are complete" in {
          val ua = UserAnswers().completePartnershipDetailsUK.completePartner(index = 0).completePartner(index = 1)
          DataCompletion.isPartnershipComplete(ua, NormalMode) mustBe true
        }
      }
    }

    "isIndividualComplete" must {

      "return false" when {

        "no answers are present" in {
          DataCompletion.isIndividualComplete(UserAnswers(), NormalMode) mustBe false
        }

        "some answers are missing in Normal Mode" in {
          val ua = UserAnswers().individualDetails(TolerantIndividual(Some("first"), None, Some("last"))).individualEmail(email = "s@s.com")
          DataCompletion.isIndividualComplete(ua, NormalMode) mustBe false
        }

        "all answers are complete in Normal Mode" in {
          val ua = UserAnswers().completeIndividual
          DataCompletion.isIndividualComplete(ua, NormalMode) mustBe true
        }

        "some answers are missing in UpdateMode" in {
          val ua = UserAnswers().individualDetails(TolerantIndividual(Some("first"), None, Some("last"))).individualEmail(email = "s@s.com")
          DataCompletion.isIndividualComplete(ua, UpdateMode) mustBe false
        }

        "all answers are complete in UpdateMode" in {
          val ua = UserAnswers().completeIndividualVariations
          DataCompletion.isIndividualComplete(ua, UpdateMode) mustBe true
        }
      }
    }

    "isAdviserComplete" must {

      "return false" when {

        "no answers are present" in {
          DataCompletion.isAdviserComplete(UserAnswers(), NormalMode) mustBe false
        }

        "declaration working knowledge is adviser without adviser details in Normal Mode" in {
          val ua = UserAnswers().declarationWorkingKnowledge(DeclarationWorkingKnowledge.Adviser)
          DataCompletion.isAdviserComplete(ua, NormalMode) mustBe false
        }

        "declaration working knowledge is adviser with some adviser details is missing in Normal Mode" in {
          val ua = UserAnswers().declarationWorkingKnowledge(DeclarationWorkingKnowledge.Adviser).adviserName("test-name").
            adviserEmail("s@s.com")
          DataCompletion.isAdviserComplete(ua, NormalMode) mustBe false
        }

        "variation working knowledge is false without adviser details in Update Mode" in {
          val ua = UserAnswers().variationWorkingKnowledge(false)
          DataCompletion.isAdviserComplete(ua, UpdateMode) mustBe false
        }

        "variation working knowledge is false with some adviser details missing in Update Mode" in {
          val ua = UserAnswers().variationWorkingKnowledge(false).adviserName("test-name").
            adviserEmail("s@s.com")
          DataCompletion.isAdviserComplete(ua, UpdateMode) mustBe false
        }
      }

      "return true" when {

        "declaration working knowledge is working knowledge in Normal Mode" in {
          val ua = UserAnswers().declarationWorkingKnowledge(DeclarationWorkingKnowledge.WorkingKnowledge)
          DataCompletion.isAdviserComplete(ua, NormalMode) mustBe true
        }

        "declaration working knowledge is adviser with all the adviser details in Normal Mode" in {
          val ua = UserAnswers().declarationWorkingKnowledge(DeclarationWorkingKnowledge.Adviser).adviserName(name = "test-name").
            adviserAddress(address).adviserPhone("123").adviserEmail("s@s.com")
          DataCompletion.isAdviserComplete(ua, NormalMode) mustBe true
        }

        "variation working knowledge is true in Update Mode" in {
          val ua = UserAnswers().variationWorkingKnowledge(true)
          DataCompletion.isAdviserComplete(ua, UpdateMode) mustBe true
        }

        "variation working knowledge is false with all the adviser details in Update Mode" in {
          val ua = UserAnswers().variationWorkingKnowledge(false).adviserName(name = "test-name").
            adviserAddress(address).adviserPhone("123").adviserEmail("s@s.com")
          DataCompletion.isAdviserComplete(ua, UpdateMode) mustBe true
        }
      }
    }

    "psaUpdateDetailsInCompleteAlert" must {

      "return alert message " when {

        "no answers are present" in {
          DataCompletion.psaUpdateDetailsInCompleteAlert(UserAnswers()) mustBe Some("incomplete.alert.message")
        }

        "individual details are not complete" in {
          val ua = UserAnswers().regInfo(RegistrationLegalStatus.Individual).
            individualDetails(TolerantIndividual(Some("first"), None, Some("last"))).individualEmail(email = "s@s.com")
          DataCompletion.psaUpdateDetailsInCompleteAlert(ua) mustBe Some("incomplete.alert.message")
        }

        "individual details are complete but adviser is not complete" in {
          val ua = UserAnswers().regInfo(RegistrationLegalStatus.Individual).completeIndividualVariations.variationWorkingKnowledge(value = false)
          DataCompletion.psaUpdateDetailsInCompleteAlert(ua) mustBe Some("incomplete.alert.message")
        }

        "company is not complete" in {
          val ua = UserAnswers().regInfo(RegistrationLegalStatus.LimitedCompany).completeCompanyDetailsUK
          DataCompletion.psaUpdateDetailsInCompleteAlert(ua) mustBe Some("incomplete.alert.message")
        }

        "partnership partner is not complete" in {
          val ua = UserAnswers().regInfo(RegistrationLegalStatus.Partnership).completePartnershipDetailsUK.completePartner(index = 0)
            .partnerName(1, PersonName("first", "last"))
          DataCompletion.psaUpdateDetailsInCompleteAlert(ua) mustBe Some("incomplete.alert.message")
        }

        "partnership is complete but no of partners is less than two" in {
          val ua = UserAnswers().areYouInUk(true).regInfo(RegistrationLegalStatus.Partnership).completePartnershipDetailsUK.completePartner(index = 0).
            variationWorkingKnowledge(value = true)
          DataCompletion.psaUpdateDetailsInCompleteAlert(ua) mustBe Some("incomplete.alert.message.less.partners")
        }
      }

      "return None " when {

        "individual details are complete" in {
          val ua = UserAnswers().regInfo(RegistrationLegalStatus.Individual).completeIndividualVariations.variationWorkingKnowledge(value = true)
          DataCompletion.psaUpdateDetailsInCompleteAlert(ua) mustBe None
        }

        "company is complete" in {
          val ua = UserAnswers().areYouInUk(true).regInfo(RegistrationLegalStatus.LimitedCompany).
            completeCompanyDetailsUK.completeDirector(index = 0).
            variationWorkingKnowledge(value = true)
          DataCompletion.psaUpdateDetailsInCompleteAlert(ua) mustBe None
        }

        "partnership is complete" in {
          val ua = UserAnswers().areYouInUk(true).regInfo(RegistrationLegalStatus.Partnership).completePartnershipDetailsUK.completePartner(index = 0).
            completePartner(index = 1).variationWorkingKnowledge(value = true)
          DataCompletion.psaUpdateDetailsInCompleteAlert(ua) mustBe None
        }
      }
    }
  }
}
