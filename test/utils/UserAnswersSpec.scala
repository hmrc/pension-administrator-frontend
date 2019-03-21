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

package utils

import java.time.LocalDate

import controllers.register.company.directors.routes
import identifiers.register.company.directors.{DirectorAddressId, DirectorDetailsId, IsDirectorCompleteId, ExistingCurrentAddressId => DirectorsExistingCurrentAddressId}
import identifiers.register.company.{CompanyContactAddressId, ExistingCurrentAddressId => CompanyExistingCurrentAddressId}
import identifiers.register.partnership.partners.{IsPartnerCompleteId, PartnerDetailsId}
import models._
import models.register.adviser.AdviserDetails
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.{JsPath, JsResultException, Json}
import viewmodels.Person

class UserAnswersSpec extends WordSpec with MustMatchers with OptionValues {

  private val establishers = Json.obj(
    "establishers" -> Json.arr(
      Json.obj(
        "name" -> "foo"
      ),
      Json.obj(
        "name" -> "bar"
      )
    )
  )

  "getAll" should {
    "get all matching recursive results" in {
      val userAnswers = UserAnswers(establishers)
      userAnswers.getAll[String](JsPath \ "establishers" \\ "name").value must contain allOf("foo", "bar")
    }

    "throw JsResultException if Js Value is not of correct type" in {
      val userAnswers = UserAnswers(establishers)
      intercept[JsResultException] {
        userAnswers.getAll[Boolean](JsPath \ "establishers" \\ "name")
      }
    }

    "return an empty list when no matches" in {
      val userAnswers = UserAnswers(establishers)
      userAnswers.getAll[String](JsPath \ "establishers" \\ "address").value.size mustBe 0
    }
  }

  ".allDirectorsAfterDelete" must {

    "return a map of director names, edit links, delete links and isComplete flag" in {
      val userAnswers = UserAnswers()
        .set(DirectorDetailsId(0))(PersonDetails("First", None, "Last", LocalDate.now()))
        .flatMap(_.set(IsDirectorCompleteId(0))(true))
        .flatMap(_.set(IsDirectorCompleteId(1))(false))
        .flatMap(_.set(DirectorDetailsId(1))(PersonDetails("First1", None, "Last1", LocalDate.now))).get

      val directorEntities = Seq(
        Person(0, "First Last", routes.ConfirmDeleteDirectorController.onPageLoad(NormalMode, 0).url,
          routes.CheckYourAnswersController.onPageLoad(NormalMode, Index(0)).url,
          isDeleted = false, isComplete = true),
        Person(1, "First1 Last1", routes.ConfirmDeleteDirectorController.onPageLoad(NormalMode, 1).url,
          routes.DirectorDetailsController.onPageLoad(NormalMode, Index(1)).url,
          isDeleted = false, isComplete = false))

      val result = userAnswers.allDirectorsAfterDelete(NormalMode)

      result.size mustEqual 2
      result mustBe directorEntities
    }
  }

  "isUserAnswerUpdated" must {

    "return false if isChanged is not present" in {
      val userAnswers = UserAnswers(establishers)
      userAnswers.isUserAnswerUpdated() mustBe false
    }

    "return true if isChanged with true is present for a change" in {
      val userAnswersData = Json.obj("areYouInUK" -> true, "isChanged" -> true)
      val userAnswers = UserAnswers(userAnswersData)
      userAnswers.isUserAnswerUpdated() mustBe true
    }

    "return true if isChanged with true is present for multiple changes" in {
      val userAnswersData = Json.obj("areYouInUK" -> true,
        "areDirectorsOrPartnersChanged" -> true,
        "isMoreThanTenDirectorsOrPartnersChanged" -> true)
      val userAnswers = UserAnswers(userAnswersData)
      userAnswers.isUserAnswerUpdated() mustBe true
    }

    "return true if isChanged with true is present for one of multiple changes" in {
      val userAnswersData = Json.obj("areYouInUK" -> true, "isChanged" -> false,
        "areDirectorsOrPartnersChanged" -> true,
        "isMoreThanTenDirectorsOrPartnersChanged" -> false)
      val userAnswers = UserAnswers(userAnswersData)
      userAnswers.isUserAnswerUpdated() mustBe true
    }

    "return false if isChanged with false is present for MoreThanTenDirectorsOrPartnersChangedId" in {
      val userAnswersData = Json.obj("areYouInUK" -> true, "isMoreThanTenDirectorsOrPartnersChanged" -> false)
      val userAnswers = UserAnswers(userAnswersData)
      userAnswers.isUserAnswerUpdated() mustBe false
    }


    "return false if isChanged with false is present for multiple changes" in {
      val userAnswersData = Json.obj("areYouInUK" -> true, "isChanged" -> false,
        "areDirectorsOrPartnersChanged" -> false,
        "isMoreThanTenDirectorsOrPartnersChanged" -> false)
      val userAnswers = UserAnswers(userAnswersData)
      userAnswers.isUserAnswerUpdated() mustBe false
    }
  }

  "setAllExistingAddress" must {
    def address(typeOfUser: String) = Address(s"${typeOfUser}line1", s"line2", None, None, Some("test postcode"), "GB")

    val userAnswers = UserAnswers()
      .set(DirectorAddressId(0))(address("director1"))
      .flatMap(_.set(DirectorAddressId(1))(address("director2")))
      .flatMap(_.set(CompanyContactAddressId)(address("company"))).get

    "return the updated user answers with existing address" in {
      val result = userAnswers.setAllExistingAddress(
        Map(CompanyContactAddressId -> CompanyExistingCurrentAddressId,
          DirectorAddressId(0) -> DirectorsExistingCurrentAddressId(0),
          DirectorAddressId(1) -> DirectorsExistingCurrentAddressId(1)
        )
      ).get

      result mustBe userAnswers.set(DirectorsExistingCurrentAddressId(0))(address("director1").toTolerantAddress)
        .flatMap(_.set(DirectorsExistingCurrentAddressId(1))(address("director2").toTolerantAddress))
        .flatMap(_.set(CompanyExistingCurrentAddressId)(address("company").toTolerantAddress)).get
    }

    "return the same user answers if map is empty" in {
      val result = userAnswers.setAllExistingAddress(Map.empty).get
      result mustBe userAnswers
    }
  }

  "isPsaUpdateDetailsInComplete" must {

    "set the flag as true(incomplete)" when {

      "any one of the directors is incomplete" in {
        val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
          RegistrationLegalStatus.LimitedCompany, "", false, RegistrationCustomerType.UK, None, None))
          .companyAddressYears(AddressYears.UnderAYear)
          .companyPreviousAddress(Address("line1", "line2", None, None, None, "GB"))
          .variationWorkingKnowledge(true)
          .set(DirectorDetailsId(0))(PersonDetails("First", None, "Last", LocalDate.now()))
          .flatMap(_.set(IsDirectorCompleteId(0))(true))
          .flatMap(_.set(IsDirectorCompleteId(1))(false))
          .flatMap(_.set(DirectorDetailsId(1))(PersonDetails("First1", None, "Last1", LocalDate.now))).get


        userAnswers.isPsaUpdateDetailsInComplete mustBe true
      }

      "company previous address is incomplete" in {
        val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
          RegistrationLegalStatus.LimitedCompany, "", false, RegistrationCustomerType.UK, None, None))
          .companyAddressYears(AddressYears.UnderAYear)
          .variationWorkingKnowledge(true)
          .set(DirectorDetailsId(0))(PersonDetails("First", None, "Last", LocalDate.now()))
          .flatMap(_.set(IsDirectorCompleteId(0))(true))
          .flatMap(_.set(IsDirectorCompleteId(1))(true))
          .flatMap(_.set(DirectorDetailsId(1))(PersonDetails("First1", None, "Last1", LocalDate.now))).get


        userAnswers.isPsaUpdateDetailsInComplete mustBe true
      }

      "any one of the partners is incomplete" in {
        val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
          RegistrationLegalStatus.Partnership, "", false, RegistrationCustomerType.UK, None, None))
          .partnershipAddressYears(AddressYears.UnderAYear)
          .partnershipPreviousAddress(Address("line1", "line2", None, None, None, "GB"))
          .variationWorkingKnowledge(true)
          .set(PartnerDetailsId(0))(PersonDetails("First", None, "Last", LocalDate.now()))
          .flatMap(_.set(IsPartnerCompleteId(0))(true))
          .flatMap(_.set(IsPartnerCompleteId(1))(false))
          .flatMap(_.set(PartnerDetailsId(1))(PersonDetails("First1", None, "Last1", LocalDate.now))).get


        userAnswers.isPsaUpdateDetailsInComplete mustBe true
      }

      "partnership previous address is incomplete" in {
        val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
          RegistrationLegalStatus.Partnership, "", false, RegistrationCustomerType.UK, None, None))
          .partnershipAddressYears(AddressYears.UnderAYear)
          .variationWorkingKnowledge(true)
          .set(PartnerDetailsId(0))(PersonDetails("First", None, "Last", LocalDate.now()))
          .flatMap(_.set(IsPartnerCompleteId(0))(true))
          .flatMap(_.set(IsPartnerCompleteId(1))(true))
          .flatMap(_.set(PartnerDetailsId(1))(PersonDetails("First1", None, "Last1", LocalDate.now))).get

        userAnswers.isPsaUpdateDetailsInComplete mustBe true
      }

      "individual previous address is incomplete" in {
        val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
          RegistrationLegalStatus.Individual, "", false, RegistrationCustomerType.UK, None, None))
          .individualAddressYears(AddressYears.UnderAYear)
          .variationWorkingKnowledge(true)

        userAnswers.isPsaUpdateDetailsInComplete mustBe true
      }

      " adviser is incomplete i.e the flag value is false and only some details is entered" in {
        val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
          RegistrationLegalStatus.Individual, "", false, RegistrationCustomerType.UK, None, None))
          .individualAddressYears(AddressYears.UnderAYear)
          .individualPreviousAddress(Address("line1", "line2", None, None, None, "GB"))
          .variationWorkingKnowledge(false)
          .adviserName("test adviser")
          .adviserAddress(Address("line1", "line2", None, None, None, "GB"))

        userAnswers.isPsaUpdateDetailsInComplete mustBe true
      }

      "adviser is incomplete i.e flag value is false and no details is entered" in {
        val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
          RegistrationLegalStatus.Individual, "", false, RegistrationCustomerType.UK, None, None))
          .individualAddressYears(AddressYears.UnderAYear)
          .individualPreviousAddress(Address("line1", "line2", None, None, None, "GB"))
          .variationWorkingKnowledge(false)

        userAnswers.isPsaUpdateDetailsInComplete mustBe true
      }
    }

    "set the flag as false(complete)" when {

      "all the details of company is complete" in {
        val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
          RegistrationLegalStatus.LimitedCompany, "", false, RegistrationCustomerType.UK, None, None))
          .companyAddressYears(AddressYears.UnderAYear)
          .companyPreviousAddress(Address("line1", "line2", None, None, None, "GB"))
          .variationWorkingKnowledge(true)
          .set(DirectorDetailsId(0))(PersonDetails("First", None, "Last", LocalDate.now()))
          .flatMap(_.set(IsDirectorCompleteId(0))(true))
          .flatMap(_.set(IsDirectorCompleteId(1))(true))
          .flatMap(_.set(DirectorDetailsId(1))(PersonDetails("First1", None, "Last1", LocalDate.now))).get

        userAnswers.isPsaUpdateDetailsInComplete mustBe false
      }

      "all the details of individual including previous address with less than 12 months address years is complete" in {
        val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
          RegistrationLegalStatus.Individual, "", false, RegistrationCustomerType.UK, None, None))
          .individualAddressYears(AddressYears.UnderAYear)
          .individualPreviousAddress(Address("line1", "line2", None, None, None, "GB"))
          .variationWorkingKnowledge(true)

        userAnswers.isPsaUpdateDetailsInComplete mustBe false
      }

      "all the details of individual with more than 12 months address years is complete" in {
        val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
          RegistrationLegalStatus.Individual, "", false, RegistrationCustomerType.UK, None, None))
          .individualAddressYears(AddressYears.OverAYear)
          .variationWorkingKnowledge(true)

        userAnswers.isPsaUpdateDetailsInComplete mustBe false
      }

      "all the details of partnership is complete" in {
        val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
          RegistrationLegalStatus.Partnership, "", false, RegistrationCustomerType.UK, None, None))
          .partnershipAddressYears(AddressYears.UnderAYear)
          .partnershipPreviousAddress(Address("line1", "line2", None, None, None, "GB"))
          .variationWorkingKnowledge(false)
          .adviserName("test adviser")
          .adviserAddress(Address("line1", "line2", None, None, None, "GB"))
          .adviserDetails(AdviserDetails("email", "234"))
          .set(PartnerDetailsId(0))(PersonDetails("First", None, "Last", LocalDate.now()))
          .flatMap(_.set(IsPartnerCompleteId(0))(true))
          .flatMap(_.set(IsPartnerCompleteId(1))(true))
          .flatMap(_.set(PartnerDetailsId(1))(PersonDetails("First1", None, "Last1", LocalDate.now))).get

        userAnswers.isPsaUpdateDetailsInComplete mustBe false
      }
    }
  }
}
