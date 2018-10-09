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

package utils

import base.SpecBase
import models.PsaSubscription.PsaSubscription
import org.scalatest.{MustMatchers, WordSpec}
import utils.PsaDetailsHelper._
import utils.PsaDetailsHelperSpec.actualSeqAnswerRow
import utils.countryOptions.CountryOptions
import utils.testhelpers.PsaSubscriptionBuilder
import utils.testhelpers.PsaSubscriptionBuilder._
import viewmodels.{AnswerRow, Message, SuperSection}

class PsaDetailsHelperSpec extends WordSpec with MustMatchers {

  def validSection(testName: String, headingKey: Option[String], result: Seq[SuperSection], expectedAnswerRows: Seq[AnswerRow]) = {
    s"display Individual details section with correct labels for $testName" in {
      val actualLabels = actualSeqAnswerRow(result, headingKey).map(_.label).toSet
      val expectedLabels = expectedAnswerRows.map(_.label).toSet

      actualLabels mustBe expectedLabels
    }

    s"display Individual details section with correct values for $testName" in {
      val actualValues = actualSeqAnswerRow(result, headingKey).map(_.answer).toSet
      val expectedValues = expectedAnswerRows.map(_.answer).toSet

      actualValues mustBe expectedValues
    }
  }

  import PsaDetailsHelperSpec._

  "PsaDetailsHelper" must {

    behave like validSection(testName = "individual details", headingKey = None, result = individualResult, expectedAnswerRows = individualExpectedAnswerRows)

    behave like validSection(testName = "company details", headingKey = None, result = companyResult, expectedAnswerRows = companyExpectedAnswerRows)

    "have a supersection heading for directors" in {
      companyResult.exists(_.headingKey == directorDetailsSuperSectionKey) mustBe true
    }

    behave like validSection(testName = "director details", headingKey = directorDetailsSuperSectionKey,
      result = companyResult, expectedAnswerRows = directorOrPartnerExpectedAnswerRows)

    behave like validSection(testName = "partnership details", headingKey = None,
      result = partnershipResult, expectedAnswerRows = partnershipExpectedAnswerRows)

    "have a supersection heading for partners" in {
      partnershipResult.exists(_.headingKey == partnerDetailsSuperSectionKey) mustBe true
    }

    behave like validSection(testName = "partner details", headingKey = partnerDetailsSuperSectionKey,
      result = partnershipResult, expectedAnswerRows = directorOrPartnerExpectedAnswerRows)

    "have a supersection heading for pension advisor" in {
      partnershipResult.exists(_.headingKey == pensionAdvisorSuperSectionKey) mustBe true
    }

    behave like validSection(testName = "pension advisor details", headingKey = pensionAdvisorSuperSectionKey,
      result = partnershipResult, expectedAnswerRows = pensionAdvisorExpectedAnswerRows)
  }
}

object PsaDetailsHelperSpec extends SpecBase {

  val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  def psaDetailsHelper(psaSubscription: PsaSubscription) = new PsaDetailsHelper(psaSubscription, countryOptions)

  val individualDateOfBirth = AnswerRow("cya.label.dob", Seq("29/03/1947"), false, None)
  val individualNino = AnswerRow("common.nino", Seq("AA999999A"), false, None)

  def psaAddress(addressLabelKey: String) = AnswerRow(addressLabelKey, addressAnswer(psaSubscriptionIndividual.address, countryOptions), false, None)

  val psaPreviousAddress = AnswerRow("common.previousAddress.checkyouranswers",
    addressAnswer(psaSubscriptionIndividual.previousAddress.get, countryOptions), false, None)

  def previousAddressExists(name: String) = AnswerRow(
    Message("moreThan12Months.label", name).resolve,
    Seq(messages(s"sameAddress.label.true")), false, None
  )

  def phoneNumber(label: String) = AnswerRow(label, Seq("0044-09876542312"), false, None)

  def emailAddress(label: String) = AnswerRow(label, Seq("aaa@aa.com"), false, None)

  val vatNumber = AnswerRow("vat.label", Seq("12345678"), false, None)

  val payeNumber = AnswerRow("paye.label", Seq("9876543210"), false, None)

  val crn = AnswerRow("crn.label", Seq("1234567890"), false, None)


  private def directorOrPartnerDob =
    AnswerRow("cya.label.dob", Seq("1950-03-29"), false, None)

  private def directorOrPartnerNino = AnswerRow("common.nino", Seq("AA999999A"), false, None)

  private def directorOrPartnerUtr = AnswerRow("utr.label", Seq("1234567892"), false, None)


  private def directorOrPartnerAddress =
    AnswerRow("cya.label.address", addressAnswer(director1Address, countryOptions), false, None)

  private def directorOrPartnerPrevAddress = AnswerRow("common.previousAddress.checkyouranswers", addressAnswer(director1PrevAddress, countryOptions), false, None)

  private def directorOrPartnerPhone = AnswerRow("phone.label", Seq("0044-09876542312"), false, None)

  private def directorOrPartnerEmail = AnswerRow("email.label", Seq("abc@hmrc.gsi.gov.uk"), false, None)

  private def pensionAdvisor = AnswerRow("pensions.advisor.label", Seq(pensionsAdvisor.name), false, None)

  private def pensionAdvisorEmail = AnswerRow("contactDetails.email.checkYourAnswersLabel", Seq("aaa@yahoo.com"), false, None)

  private def pensionAdvisorAddress = AnswerRow("cya.label.address", addressAnswer(PsaSubscriptionBuilder.psaAddress, countryOptions), false, None)


  val individualExpectedAnswerRows = Seq(
    individualDateOfBirth,
    individualNino,
    psaAddress("cya.label.address"),
    previousAddressExists("abcdefghijkl abcdefghijkl abcdefjkl"),
    psaPreviousAddress,
    emailAddress("email.label"),
    phoneNumber("phone.label")
  )

  val companyExpectedAnswerRows = Seq(
    vatNumber,
    payeNumber,
    crn,
    psaAddress("company.address.label"),
    previousAddressExists("Test company name"),
    psaPreviousAddress,
    emailAddress("company.email.label"),
    phoneNumber("company.phone.label")
  )

  val partnershipExpectedAnswerRows = Seq(
    vatNumber,
    payeNumber,
    psaAddress("partnership.address.label"),
    previousAddressExists("Test partnership name"),
    psaPreviousAddress,
    emailAddress("partnership.email.label"),
    phoneNumber("partnership.phone.label")
  )

  val directorOrPartnerExpectedAnswerRows = Seq(
    directorOrPartnerDob,
    directorOrPartnerNino,
    directorOrPartnerUtr,
    directorOrPartnerAddress,
    directorOrPartnerPrevAddress,
    directorOrPartnerEmail,
    directorOrPartnerPhone
  )

  val pensionAdvisorExpectedAnswerRows = Seq(
    pensionAdvisor,
    pensionAdvisorEmail,
    pensionAdvisorAddress
  )

  val individualResult: Seq[SuperSection] = psaDetailsHelper(psaSubscriptionIndividual).individualSections
  val companyResult: Seq[SuperSection] = psaDetailsHelper(psaSubscriptionCompany).organisationSections
  val partnershipResult: Seq[SuperSection] = psaDetailsHelper(psaSubscriptionPartnership).organisationSections

  val partnerDetailsSuperSectionKey = Some("partner.supersection.header")
  val directorDetailsSuperSectionKey = Some("director.supersection.header")
  val pensionAdvisorSuperSectionKey = Some("pensionAdvisor.section.header")

  def actualSeqAnswerRow(result: Seq[SuperSection], headingKey: Option[String]): Seq[AnswerRow] =
    result.filter(_.headingKey == headingKey).flatMap(_.sections).take(1).flatMap(_.rows)
}
