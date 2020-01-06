/*
 * Copyright 2020 HM Revenue & Customs
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

import controllers.register.company.directors.routes
import identifiers.TypedIdentifier
import identifiers.register.company.directors.{DirectorAddressId, DirectorNameId, IsDirectorCompleteId, ExistingCurrentAddressId => DirectorsExistingCurrentAddressId}
import identifiers.register.company.{CompanyContactAddressId, ExistingCurrentAddressId => CompanyExistingCurrentAddressId}
import identifiers.register.partnership.partners.{IsPartnerCompleteId, PartnerNameId}
import models._
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
        .set(DirectorNameId(0))(PersonName("First", "Last"))
        .flatMap(_.set(IsDirectorCompleteId(0))(true))
        .flatMap(_.set(IsDirectorCompleteId(1))(false))
        .flatMap(_.set(DirectorNameId(1))(PersonName("First1", "Last1"))).get

      val directorEntities = Seq(
        Person(0, "First Last", routes.ConfirmDeleteDirectorController.onPageLoad(NormalMode, 0).url,
          routes.CheckYourAnswersController.onPageLoad(NormalMode, Index(0)).url,
          isDeleted = false, isComplete = true),
        Person(1, "First1 Last1", routes.ConfirmDeleteDirectorController.onPageLoad(NormalMode, 1).url,
          routes.DirectorNameController.onPageLoad(NormalMode, Index(1)).url,
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
          .set(DirectorNameId(0))(PersonName("First", "Last"))
          .flatMap(_.set(IsDirectorCompleteId(0))(true))
          .flatMap(_.set(IsDirectorCompleteId(1))(false))
          .flatMap(_.set(DirectorNameId(1))(PersonName("First1", "Last1"))).get


        userAnswers.isPsaUpdateDetailsInComplete mustBe true
      }

      "company previous address is incomplete" in {
        val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
          RegistrationLegalStatus.LimitedCompany, "", false, RegistrationCustomerType.UK, None, None))
          .companyAddressYears(AddressYears.UnderAYear)
          .variationWorkingKnowledge(true)
          .set(DirectorNameId(0))(PersonName("First", "Last"))
          .flatMap(_.set(IsDirectorCompleteId(0))(true))
          .flatMap(_.set(IsDirectorCompleteId(1))(true))
          .flatMap(_.set(DirectorNameId(1))(PersonName("First1", "Last1"))).get


        userAnswers.isPsaUpdateDetailsInComplete mustBe true
      }

      "any one of the partners is incomplete" in {
        val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
          RegistrationLegalStatus.Partnership, "", false, RegistrationCustomerType.UK, None, None))
          .partnershipAddressYears(AddressYears.UnderAYear)
          .partnershipPreviousAddress(Address("line1", "line2", None, None, None, "GB"))
          .variationWorkingKnowledge(true)
          .set(PartnerNameId(0))(PersonName("First", "Last"))
          .flatMap(_.set(IsPartnerCompleteId(0))(true))
          .flatMap(_.set(IsPartnerCompleteId(1))(false))
          .flatMap(_.set(PartnerNameId(1))(PersonName("First", "Last"))).get


        userAnswers.isPsaUpdateDetailsInComplete mustBe true
      }

      "partnership previous address is incomplete" in {
        val userAnswers = UserAnswers().registrationInfo(RegistrationInfo(
          RegistrationLegalStatus.Partnership, "", false, RegistrationCustomerType.UK, None, None))
          .partnershipAddressYears(AddressYears.UnderAYear)
          .variationWorkingKnowledge(true)
          .set(PartnerNameId(0))(PersonName("First", "Last"))
          .flatMap(_.set(IsPartnerCompleteId(0))(true))
          .flatMap(_.set(IsPartnerCompleteId(1))(true))
          .flatMap(_.set(PartnerNameId(1))(PersonName("First", "Last"))).get

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
          .set(DirectorNameId(0))(PersonName("First", "Last"))
          .flatMap(_.set(IsDirectorCompleteId(0))(true))
          .flatMap(_.set(IsDirectorCompleteId(1))(true))
          .flatMap(_.set(DirectorNameId(1))(PersonName("First1", "Last1"))).get

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
          .adviserEmail("email")
          .adviserPhone("234")
          .set(PartnerNameId(0))(PersonName("First", "Last"))
          .flatMap(_.set(IsPartnerCompleteId(0))(true))
          .flatMap(_.set(IsPartnerCompleteId(1))(true))
          .flatMap(_.set(PartnerNameId(1))(PersonName("First", "Last"))).get

        userAnswers.isPsaUpdateDetailsInComplete mustBe false
      }
    }
  }

  "remove" must {

    "remove an element that exists" in {
      val ua = UserAnswers(Json.obj(
        "name" -> "jim bloggs",
        "email" -> "a@a.c"
      ))

      val expectedResult = UserAnswers(Json.obj(
        "name" -> "jim bloggs"
      ))

      val testIdentifier = new TypedIdentifier[String] {
        override def toString: String = "email"
      }

      val result = ua.remove(testIdentifier).asOpt.value
      result mustBe expectedResult
    }

    "remove an element inside an element that does exist with no other elements" in {
      val ua = UserAnswers(Json.obj(
        "name" -> "jim bloggs",
        "contactDetails" -> Json.obj(
          "email" -> "a@a.com"
        )
      ))

      val expectedResult = UserAnswers(Json.obj(
        "name" -> "jim bloggs",
        "contactDetails" -> Json.obj(
        )
      ))

      val testIdentifier = new TypedIdentifier[String] {
        override def toString: String = "email"
        override def path: JsPath = JsPath \ "contactDetails" \ "email"
      }

      val result = ua.remove(testIdentifier).asOpt.value
      result mustBe expectedResult
    }

    "remove an element inside an element that does exist with one other element" in {
      val ua = UserAnswers(Json.obj(
        "name" -> "jim bloggs",
        "contactDetails" -> Json.obj(
          "email" -> "a@a.com",
          "phone" -> "999"
        )
      ))

      val expectedResult = UserAnswers(Json.obj(
        "name" -> "jim bloggs",
        "contactDetails" -> Json.obj(
          "phone" -> "999"
        )
      ))

      val testIdentifier = new TypedIdentifier[String] {
        override def toString: String = "email"
        override def path: JsPath = JsPath \ "contactDetails" \ "email"
      }

      val result = ua.remove(testIdentifier).asOpt.value
      result mustBe expectedResult
    }

    "NOT attempt to remove an element inside an element that does NOT exist" in {
      val ua = UserAnswers(Json.obj(
        "name" -> "jim bloggs"
      ))

      val testIdentifier = new TypedIdentifier[String] {
        override def toString: String = "email"
        override def path: JsPath = JsPath \ "contactDetails" \ "email"
      }

      val result = ua.remove(testIdentifier).asOpt.value
      result mustBe ua
    }

    "NOT attempt to remove a non-existent element inside an element that DOES exist" in {
      val ua = UserAnswers(Json.obj(
        "name" -> "jim bloggs",
        "contactDetails" -> Json.obj()
      ))

      val testIdentifier = new TypedIdentifier[String] {
        override def toString: String = "email"
        override def path: JsPath = JsPath \ "contactDetails" \ "email"
      }

      val result = ua.remove(testIdentifier).asOpt.value
      result mustBe ua
    }
  }
}
