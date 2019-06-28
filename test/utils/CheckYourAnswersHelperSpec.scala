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
import identifiers.register.company.CompanyDetailsId
import identifiers.register.company.directors.{CompanyDirectorAddressPostCodeLookupId, DirectorDetailsId, DirectorUniqueTaxReferenceId}
import models.register.company.CompanyDetails
import models.{NormalMode, PersonDetails, TolerantAddress, UniqueTaxReference}
import play.api.libs.json.{JsObject, Json}
import viewmodels.{AnswerRow, Link}

class CheckYourAnswersHelperSpec extends SpecBase {

  private val localDate = LocalDate.parse("28/06/2019", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
  private val countryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  def cyaHelperMethod(codeToTest: CheckYourAnswersHelper => Seq[AnswerRow], json: JsObject, expectedResult: Seq[AnswerRow]): Unit = {
    val countryOptions = new FakeCountryOptions(environment, frontendAppConfig)
    val genCYAHelper = new CheckYourAnswersHelper(_: UserAnswers, countryOptions)

    "respond correctly when user answers data exists" in {
      codeToTest(genCYAHelper(UserAnswers(json))) mustBe expectedResult
    }

    "respond correctly when user answers data does not exist" in {
      codeToTest(genCYAHelper(UserAnswers())) mustBe Seq()
    }
  }

  "directorContactDetails" should {
    behave like cyaHelperMethod(_.directorDetails(0, NormalMode),
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
          Seq("28 June 2019"),
          answerIsMessageKey = false,
          Some(Link("/register-as-pension-scheme-administrator/register/company/directors/1/change/director-details"))))
    )
  }

  "directorUniqueTaxReference" should {
    behave like cyaHelperMethod(_.directorUniqueTaxReference(0, NormalMode),
      Json.obj(
        CompanyDetailsId.toString -> CompanyDetails(None, None),
        "directors" -> Json.arr(
          Json.obj(
            DirectorUniqueTaxReferenceId.toString ->
              UniqueTaxReference.Yes("utr")
          )
        )
      ),
      Seq(
        AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel",
          Seq("Yes"),
          answerIsMessageKey = true,
          Some(Link("/register-as-pension-scheme-administrator/register/company/directors/1/change/unique-taxpayer-reference"))),
        AnswerRow("directorUniqueTaxReference.checkYourAnswersLabel.utr",
          Seq("utr"),
          answerIsMessageKey = true,
          Some(Link("/register-as-pension-scheme-administrator/register/company/directors/1/change/unique-taxpayer-reference"))))
    )
  }

}
