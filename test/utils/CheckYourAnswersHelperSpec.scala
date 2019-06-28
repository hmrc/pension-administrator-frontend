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
import java.time.format.DateTimeFormatter

import base.SpecBase
import identifiers.register.company.{CompanyDetailsId, CompanyRegistrationNumberId, MoreThanTenDirectorsId}
import identifiers.register.company.directors._
import identifiers.register.individual.IndividualDateOfBirthId
import models.register.company.CompanyDetails
import models._
import play.api.libs.json.{JsObject, Json}
import viewmodels.{AnswerRow, Link}

class CheckYourAnswersHelperSpec extends SpecBase {

  private val reason = "don't have one"
  private val localDate = LocalDate.parse("28/06/2019", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
  private val displayDate = "28 June 2019"
  private val countryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  case class TestScenario(userAnswersJson: JsObject, expectedResult: Seq[AnswerRow], name: Option[String] = None) {
    def describe(descr: String): String = name.map(x => descr + s" ($x)").getOrElse(descr)
  }

  def cyaHelperMethod(codeToTest: CheckYourAnswersHelper => Seq[AnswerRow],
                      dataExistingTests: Seq[TestScenario]): Unit = {
    val countryOptions = new FakeCountryOptions(environment, frontendAppConfig)
    val genCYAHelper = new CheckYourAnswersHelper(_: UserAnswers, countryOptions)

    dataExistingTests.foreach { scenario =>
      scenario.describe("respond correctly when user answers data exists") in {
        codeToTest(genCYAHelper(UserAnswers(scenario.userAnswersJson))) mustBe scenario.expectedResult
      }
    }

    "respond correctly when user answers data does not exist" in {
      codeToTest(genCYAHelper(UserAnswers())) mustBe Seq()
    }
  }

  def yesNoExpectedResult(row1Label: String, row1Answer: String, row2Label: String, row2Answer: String, changeLink: String): Seq[AnswerRow] =
    Seq(
      AnswerRow(row1Label,
        Seq(row1Answer),
        answerIsMessageKey = true,
        Some(Link(s"/register-as-pension-scheme-administrator/register/company/directors/1/change/$changeLink"))),
      AnswerRow(row2Label,
        Seq(row2Answer),
        answerIsMessageKey = true,
        Some(Link(s"/register-as-pension-scheme-administrator/register/company/directors/1/change/$changeLink"))))

  "directorDetails" should {
    behave like cyaHelperMethod(_.directorDetails(0, NormalMode),
      Seq(
        TestScenario(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails(None, None),
            "directors" -> Json.arr(
              Json.obj(
                DirectorDetailsId.toString ->
                  PersonDetails("test first name", Some("test middle name"), "test last name", localDate)
              )
            )
          ),
          Seq(
            AnswerRow("cya.label.name",
              Seq("test first name test last name"),
              answerIsMessageKey = false,
              Some(Link("/register-as-pension-scheme-administrator/register/company/directors/1/change/director-details"))),
            AnswerRow("cya.label.dob",
              Seq(displayDate),
              answerIsMessageKey = false,
              Some(Link("/register-as-pension-scheme-administrator/register/company/directors/1/change/director-details"))))
        )
      )
    )
  }

  "directorUniqueTaxReference" should {
    behave like cyaHelperMethod(_.directorUniqueTaxReference(0, NormalMode),
      Seq(
        TestScenario(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails(None, None),
            "directors" -> Json.arr(
              Json.obj(
                DirectorUniqueTaxReferenceId.toString ->
                  UniqueTaxReference.Yes("utr")
              )
            )
          ),
          yesNoExpectedResult("directorUniqueTaxReference.checkYourAnswersLabel",
            "Yes",
            "directorUniqueTaxReference.checkYourAnswersLabel.utr",
            "utr",
            "unique-taxpayer-reference"),
          Some("user answered yes")
        ),
        TestScenario(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails(None, None),
            "directors" -> Json.arr(
              Json.obj(
                DirectorUniqueTaxReferenceId.toString ->
                  UniqueTaxReference.No(reason)
              )
            )
          ),
          yesNoExpectedResult(
            "directorUniqueTaxReference.checkYourAnswersLabel",
            "No",
            "directorUniqueTaxReference.checkYourAnswersLabel.reason",
            reason,
            "unique-taxpayer-reference"),
          Some("user answered no")
        )
      )
    )
  }

  "directorAddressYears" should {
    behave like cyaHelperMethod(_.directorAddressYears(0, NormalMode),
      Seq(
        TestScenario(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails(None, None),
            "directors" -> Json.arr(
              Json.obj(
                DirectorAddressYearsId.toString -> AddressYears.OverAYear.toString
              )
            )
          ),
          Seq(
            AnswerRow("directorAddressYears.checkYourAnswersLabel",
              Seq("common.addressYears.over_a_year"),
              answerIsMessageKey = true,
              Some(Link("/register-as-pension-scheme-administrator/register/company/directors/1/change/how-long-at-address"))
            )
          )
        )
      )
    )
  }

  "directorNino" should {
    behave like cyaHelperMethod(_.directorNino(0, NormalMode),
      Seq(
        TestScenario(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails(None, None),
            "directors" -> Json.arr(
              Json.obj(
                DirectorNinoId.toString ->
                  Nino.Yes("nino")
              )
            )
          ),
          yesNoExpectedResult("directorNino.checkYourAnswersLabel",
            "Yes",
            "directorNino.checkYourAnswersLabel.nino",
            "nino",
            "national-insurance-number"),
          Some("user answered yes")
        ),
        TestScenario(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails(None, None),
            "directors" -> Json.arr(
              Json.obj(
                DirectorNinoId.toString ->
                  Nino.No(reason)
              )
            )
          ),
          yesNoExpectedResult("directorNino.checkYourAnswersLabel",
            "No",
            "directorNino.checkYourAnswersLabel.reason",
            reason,
            "national-insurance-number"),
          Some("user answered no")
        )
      )
    )
  }

  "moreThanTenDirectors" should {
    behave like cyaHelperMethod(_.moreThanTenDirectors.toSeq,
      Seq(
        TestScenario(
          Json.obj(MoreThanTenDirectorsId.toString -> true),
          Seq(AnswerRow("moreThanTenDirectors.checkYourAnswersLabel", Seq("site.yes"), true, Some(Link("/register-as-pension-scheme-administrator/register/company/change/other-directors")))),
          Some("user answered yes")
        ),
        TestScenario(
          Json.obj(MoreThanTenDirectorsId.toString -> false),
          Seq(AnswerRow("moreThanTenDirectors.checkYourAnswersLabel", Seq("site.no"), true, Some(Link("/register-as-pension-scheme-administrator/register/company/change/other-directors")))),
          Some("user answered no")
        )
      )
    )
  }

  "vatRegistrationNumber" should {
    behave like cyaHelperMethod(_.vatRegistrationNumber.toSeq,
      Seq(
        TestScenario(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails(Some("vat"), None)
          ),
          Seq(
            AnswerRow("companyDetails.vatRegistrationNumber.checkYourAnswersLabel",
              Seq("vat"),
              answerIsMessageKey = false,
              Some(Link("/register-as-pension-scheme-administrator/register/company/change/company-details")))
          )
        )
      )
    )
  }

  "payeEmployerReferenceNumber" should {
    behave like cyaHelperMethod(_.payeEmployerReferenceNumber.toSeq,
      Seq(
        TestScenario(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails(None, Some("paye"))
          ),
          Seq(
            AnswerRow("companyDetails.payeEmployerReferenceNumber.checkYourAnswersLabel",
              Seq("paye"),
              answerIsMessageKey = false,
              Some(Link("/register-as-pension-scheme-administrator/register/company/change/company-details")))
          )
        )
      )
    )
  }

  "companyRegistrationNumber" should {
    behave like cyaHelperMethod(_.companyRegistrationNumber.toSeq,
      Seq(
        TestScenario(
          Json.obj(
            CompanyRegistrationNumberId.toString -> "crn"
          ),
          Seq(
            AnswerRow("companyRegistrationNumber.checkYourAnswersLabel",
              Seq("crn"),
              answerIsMessageKey = false,
              Some(Link("/register-as-pension-scheme-administrator/register/company/change/company-registration-number")))
          )
        )
      )
    )
  }

  "individualDateOfBirth" should {
    behave like cyaHelperMethod(_.individualDateOfBirth.toSeq,
      Seq(
        TestScenario(
          Json.obj(
            IndividualDateOfBirthId.toString -> localDate
          ),
          Seq(
            AnswerRow("cya.label.dob",
              Seq(displayDate),
              answerIsMessageKey = false,
              Some(Link("/register-as-pension-scheme-administrator/register/individual/change/your-date-of-birth")))
          )
        )
      )
    )
  }

  "directorContactDetails" should {
    behave like cyaHelperMethod(_.directorContactDetails(0, NormalMode),
      Seq(
        TestScenario(
          Json.obj(
            CompanyDetailsId.toString -> CompanyDetails(None, None),
            "directors" -> Json.arr(
              Json.obj(
                DirectorContactDetailsId.toString ->
                  ContactDetails("email", "phone")
              )
            )
          ),
          Seq(
            AnswerRow("contactDetails.email",
              Seq("email"),
              answerIsMessageKey = false,
              Some(Link("/register-as-pension-scheme-administrator/register/company/directors/1/change/directors-contact-details"))),
            AnswerRow("contactDetails.phone",
              Seq("phone"),
              answerIsMessageKey = false,
              Some(Link("/register-as-pension-scheme-administrator/register/company/directors/1/change/directors-contact-details"))))
        )
      )
    )
  }

}
