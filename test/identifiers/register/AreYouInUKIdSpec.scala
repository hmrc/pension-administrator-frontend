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

import identifiers.register.company.{BusinessDetailsId, CompanyAddressId}
import identifiers.register.individual.{IndividualAddressId, IndividualDateOfBirthId, IndividualDetailsCorrectId, IndividualDetailsId}
import identifiers.register.partnership.{PartnershipDetailsId, PartnershipRegisteredAddressId}
import models.{BusinessDetails, TolerantAddress, TolerantIndividual}
import models.register.{BusinessType, NonUKBusinessType}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class AreYouInUKIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  import AreYouInUKIdSpec._

  "Cleanup for a company" when {

    "where are you in uk has already answered as Yes and we change to No" must {
      val result: UserAnswers =
        answersForYes.set(AreYouInUKId)(false)
          .asOpt.value

      "remove the data for Business details " in {
        result.get(BusinessDetailsId) mustNot be(defined)
      }

      "remove the data for Business type " in {
        result.get(BusinessTypeId) mustNot be(defined)
      }
    }

    "where are you in uk has already answered as No and we change to Yes " must {
      val result: UserAnswers =
        answersForNo.set(AreYouInUKId)(true)
          .asOpt.value

      "remove the data for Business details " in {
        result.get(BusinessDetailsId) mustNot be(defined)
      }

      "remove the data for non uk Business type " in {
        result.get(NonUKBusinessTypeId) mustNot be(defined)
      }

      "remove the data for non uk company address " in {
        result.get(CompanyAddressId) mustNot be(defined)
      }
    }

    "where are you in uk has already answered as No and we change to No (i.e. don't change at all!)" must {
      val result: UserAnswers =
        answersForNo.set(AreYouInUKId)(false)
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

      "remove the data for Individual Details Correct " in {
        result.get(IndividualDetailsCorrectId) mustNot be(defined)
      }

      "remove the data for Individual Details " in {
        result.get(IndividualDetailsId) mustNot be(defined)
      }

      "remove the data for Individual Address" in {
        result.get(IndividualAddressId) mustNot be(defined)
      }
    }

    "where are you in uk has already answered as No and we change to Yes " must {
      val result: UserAnswers =
        individualAnswersForNo.set(AreYouInUKId)(true)
            .asOpt.value

      "remove the data for non uk Individual Details" in {
        result.get(IndividualDetailsId) mustNot be(defined)
      }

      "remove the data for Individual date of birth" in {
        result.get(IndividualDateOfBirthId) mustNot be(defined)
      }

      "remove the data for Individual Address" in {
        result.get(IndividualAddressId) mustNot be(defined)
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

      "remove the data for Business details " in {
        result.get(PartnershipDetailsId) mustNot be(defined)
      }

      "remove the data for Business type " in {
        result.get(BusinessTypeId) mustNot be(defined)
      }
    }

    "where are you in uk has already answered as No and we change to Yes " must {
      val result: UserAnswers =
        partnershipAnswersForNo.set(AreYouInUKId)(true)
            .asOpt.value

      "remove the data for Business details " in {
        result.get(PartnershipDetailsId) mustNot be(defined)
      }

      "remove the data for non uk Business type " in {
        result.get(NonUKBusinessTypeId) mustNot be(defined)
      }

      "remove the data for non uk company address " in {
        result.get(PartnershipRegisteredAddressId) mustNot be(defined)
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

  val tolerantAddress = TolerantAddress(Some("line 1"),Some("line 2"), Some("line 3"), Some("line 4"), None, Some("DE"))
  val tolerantIndividual = TolerantIndividual(Some("firstName"), Some("middleName"), Some("lastName"))

  val answersForYes: UserAnswers = UserAnswers(Json.obj())
      .set(AreYouInUKId)(true)
      .flatMap(_.set(BusinessDetailsId)(BusinessDetails("test company", Some("utr")))
          .flatMap(_.set(BusinessTypeId)(BusinessType.LimitedCompany)))
      .asOpt.value

  val answersForNo: UserAnswers = UserAnswers(Json.obj())
      .set(AreYouInUKId)(false)
      .flatMap(_.set(BusinessDetailsId)(BusinessDetails("test company", None))
          .flatMap(_.set(CompanyAddressId)(tolerantAddress))
          .flatMap(_.set(NonUKBusinessTypeId)(NonUKBusinessType.Company)))
      .asOpt.value

  val individualAnswersForYes: UserAnswers = UserAnswers(Json.obj())
      .set(AreYouInUKId)(true)
      .flatMap(_.set(IndividualDetailsCorrectId)(true)
          .flatMap(_.set(IndividualDetailsId)(tolerantIndividual))
          .flatMap(_.set(IndividualAddressId)(tolerantAddress)))
      .asOpt.value

  val individualAnswersForNo: UserAnswers = UserAnswers(Json.obj())
      .set(AreYouInUKId)(false)
      .flatMap(_.set(IndividualDetailsId)(tolerantIndividual)
          .flatMap(_.set(IndividualDateOfBirthId)(LocalDate.of(2000, 12, 12)))
          .flatMap(_.set(IndividualAddressId)(tolerantAddress)))
      .asOpt.value

  val partnershipAnswersForYes = UserAnswers(Json.obj())
      .set(AreYouInUKId)(true)
      .flatMap(_.set(PartnershipDetailsId)(BusinessDetails("test partnership", Some("utr")))
          .flatMap(_.set(BusinessTypeId)(BusinessType.LimitedPartnership)))
      .asOpt.value

  val partnershipAnswersForNo = UserAnswers(Json.obj())
      .set(AreYouInUKId)(false)
      .flatMap(_.set(PartnershipDetailsId)(BusinessDetails("test partnership", None))
          .flatMap(_.set(PartnershipRegisteredAddressId)(tolerantAddress))
          .flatMap(_.set(NonUKBusinessTypeId)(NonUKBusinessType.BusinessPartnership)))
      .asOpt.value
}
