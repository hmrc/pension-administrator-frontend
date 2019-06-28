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
import identifiers.register.company.directors.DirectorDetailsId
import models.register.company.CompanyDetails
import models.{NormalMode, PersonDetails}
import play.api.libs.json.{JsObject, Json}
import viewmodels.{AnswerRow, Link}

class CheckYourAnswersHelperSpec extends SpecBase {

  "directorContactDetails" should {
    "respond correctly when user answers data exists" in {
      val localDate = LocalDate.parse("28/06/2019", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
      val json: JsObject = Json.obj(
        CompanyDetailsId.toString -> CompanyDetails(None, None),
        "directors" -> Json.arr(
          Json.obj(
            DirectorDetailsId.toString ->
              PersonDetails("test first name", Some("test middle name"), "test last name", localDate)
          )
        )
      )

      val ua = UserAnswers(json)
      val countryOptions = new FakeCountryOptions(environment, frontendAppConfig)
      val cyah = new CheckYourAnswersHelper(ua, countryOptions)
      val result = cyah.directorDetails(0, NormalMode)
      val expectedResult = Seq(
        AnswerRow("cya.label.name",
          Seq("test first name test last name"),
          answerIsMessageKey = false,
          Some(Link("/register-as-pension-scheme-administrator/register/company/directors/1/change/director-details"))),
        AnswerRow("cya.label.dob",
          Seq("28 June 2019"),
          answerIsMessageKey = false,
          Some(Link("/register-as-pension-scheme-administrator/register/company/directors/1/change/director-details"))))
      result mustBe expectedResult
    }

    "respond correctly when user answers data does not exist" in {
      val ua = UserAnswers()
      val countryOptions = new FakeCountryOptions(environment, frontendAppConfig)
      val cyah = new CheckYourAnswersHelper(ua, countryOptions)
      val result = cyah.directorDetails(0, NormalMode)
      result mustBe Seq()
    }
  }

}


