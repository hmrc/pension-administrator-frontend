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

import base.{JsonFileReader, SpecBase}
import models.Address
import org.scalatest.{MustMatchers, WordSpec}
import utils.ViewPsaDetailsHelper.addressAnswer
import utils.countryOptions.CountryOptions
import viewmodels.{AnswerRow, Message, SuperSection}
import utils.testhelpers.ViewPsaDetailsBuilder._

class ViewPsaDetailsHelperSpec extends WordSpec with MustMatchers {

  import ViewPsaDetailsHelperSpec._

  def validSection(testName: String, headingKey: Option[String], result: Seq[SuperSection], expectedAnswerRows: Seq[AnswerRow]): Unit = {
    s"display details section with correct labels for $testName" in {
      val actualLabels = actualSeqAnswerRow(result, headingKey).map(_.label).toSet
      val expectedLabels = expectedAnswerRows.map(_.label).toSet

      actualLabels mustBe expectedLabels
    }

    s"display details section with correct values for $testName" in {
      val actualValues = actualSeqAnswerRow(result, headingKey).map(_.answer).toSet
      val expectedValues = expectedAnswerRows.map(_.answer).toSet

      actualValues mustBe expectedValues
    }
  }



  "ViewPsaDetailsHelper" must {



    behave like validSection(testName = "individual details", headingKey = None, result = individualResult, expectedAnswerRows = individualSeqAnswers)

    behave like validSection(testName = "company details", headingKey = None, result = companyResult, expectedAnswerRows = companySeqAnswers)


    behave like validSection(testName = "partnership details", headingKey = None,
      result = partnershipResult, expectedAnswerRows = partnershipSeqAnswers)

    "have a supersection heading for pension advisor" in {
      partnershipResult.exists(_.headingKey == pensionAdvisorSuperSectionKey) mustBe true
    }

    behave like validSection(testName = "pension advisor details", headingKey = pensionAdvisorSuperSectionKey,
      result = partnershipResult, expectedAnswerRows = pensionAdviserSeqAnswers)
  }
}

object ViewPsaDetailsHelperSpec extends SpecBase with JsonFileReader {

  private val individualUserAnswers = readJsonFromFile("/data/psaIndividualUserAnswers.json")
  private val companyUserAnswers = readJsonFromFile("/data/psaCompanyUserAnswers.json")
  private val partnershipUserAnswers = readJsonFromFile("/data/psaPartnershipUserAnswers.json")

  private val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  private def psaDetailsHelper(userAnswers: UserAnswers) = new ViewPsaDetailsHelper(userAnswers, countryOptions)
  val address = Address("Telford1", "Telford2",Some("Telford3"), Some("Telford4"), Some("TF3 4ER"), "GB")
  val psaAddress = Address("addline1", "addline2", Some("addline3"), Some("addline4"), Some("56765"), "AD")
  val previousAddress = Address("London1", "London2", Some("London3"), Some("London4"), Some("LN12 4DC"), "GB")

  private val individualResult: Seq[SuperSection] = psaDetailsHelper(UserAnswers(individualUserAnswers)).individualSections
  private val companyResult: Seq[SuperSection] = psaDetailsHelper(UserAnswers(companyUserAnswers)).companySections
  private val partnershipResult: Seq[SuperSection] = psaDetailsHelper(UserAnswers(partnershipUserAnswers)).partnershipSections

  private val pensionAdvisorSuperSectionKey = Some("pensionAdvisor.section.header")

  private def actualSeqAnswerRow(result: Seq[SuperSection], headingKey: Option[String]): Seq[AnswerRow] =
    result.filter(_.headingKey == headingKey).flatMap(_.sections).take(1).flatMap(_.rows)
}
