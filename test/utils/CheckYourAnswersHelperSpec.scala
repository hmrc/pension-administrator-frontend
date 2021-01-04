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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import base.SpecBase
import identifiers.register.company.MoreThanTenDirectorsId
import identifiers.register.individual.IndividualDateOfBirthId
import play.api.libs.json.{JsObject, Json}
import viewmodels.{AnswerRow, Link, Message}

class CheckYourAnswersHelperSpec extends SpecBase {

  private val localDate = LocalDate.parse("28/06/2019", DateTimeFormatter.ofPattern("dd/MM/yyyy"))
  private val displayDate = "28 June 2019"

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

  "moreThanTenDirectors" should {
    behave like cyaHelperMethod(_.moreThanTenDirectors.toSeq,
      Seq(
        TestScenario(
          Json.obj(MoreThanTenDirectorsId.toString -> true),
          Seq(AnswerRow("moreThanTenDirectors.checkYourAnswersLabel", Seq("site.yes"), true,
            Some(Link("/register-as-pension-scheme-administrator/register/company/change/other-directors")))),
          Some("user answered yes")
        ),
        TestScenario(
          Json.obj(MoreThanTenDirectorsId.toString -> false),
          Seq(AnswerRow("moreThanTenDirectors.checkYourAnswersLabel", Seq("site.no"), true,
            Some(Link("/register-as-pension-scheme-administrator/register/company/change/other-directors")))),
          Some("user answered no")
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
            AnswerRow("individualDateOfBirth.heading",
              Seq(displayDate),
              answerIsMessageKey = false,
              Some(Link("/register-as-pension-scheme-administrator/register/individual/change/date-of-birth")),
              Some(Message("individualDateOfBirth.visuallyHidden.text")))
          )
        )
      )
    )
  }

}
