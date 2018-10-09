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
import utils.countryOptions.CountryOptions
import utils.testhelpers.PsaSubscriptionBuilder._
import viewmodels.{AnswerRow, Message, SuperSection}

class PsaDetailsHelperSpec extends WordSpec with MustMatchers {

  import PsaDetailsHelperSpec._

  "PsaDetailsHelper" must {
    "display Individual details section with correct labels" in {
      val actualLabels = actualSeqAnswerRow(individualResult).map(_.label).toSet
      val expectedLabels = individualExpectedAnswerRows.map(_.label).toSet

      actualLabels mustBe expectedLabels
    }

    "display Individual details section with correct values" in {
      val actualValues = actualSeqAnswerRow(individualResult).map(_.answer).toSet
      val expectedValues = individualExpectedAnswerRows.map(_.answer).toSet

      actualValues mustBe expectedValues
    }

    "display Company details section with correct labels" in {
      val actualLabels = actualSeqAnswerRow(companyResult).map(_.label).toSet
      val expectedLabels = companyExpectedAnswerRows.map(_.label).toSet

      actualLabels mustBe expectedLabels
    }

    "display Company details section with correct values" in {
      val actualValues = actualSeqAnswerRow(companyResult).map(_.answer).toSet
      val expectedValues = companyExpectedAnswerRows.map(_.answer).toSet

      actualValues mustBe expectedValues
    }

    "display Partnership details section with correct labels" in {
      val actualLabels = actualSeqAnswerRow(partnershipResult).map(_.label).toSet
      val expectedLabels = partnershipExpectedAnswerRows.map(_.label).toSet

      actualLabels mustBe expectedLabels
    }

    "display Partnership details section with correct values" in {
      val actualValues = actualSeqAnswerRow(partnershipResult).map(_.answer).toSet
      val expectedValues = partnershipExpectedAnswerRows.map(_.answer).toSet

      actualValues mustBe expectedValues
    }
  }
}

object PsaDetailsHelperSpec extends SpecBase {

  val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  def psaDetailsHelper(psaSubscription: PsaSubscription) = new PsaDetailsHelper(psaSubscription, countryOptions)

  val individualDateOfBirth = AnswerRow("cya.label.dob", Seq("29/03/1947"), false, None)
  val individualNino = AnswerRow("common.nino", Seq("AA999999A"), false, None)
  val psaAddress = AnswerRow("cya.label.address", addressAnswer(psaSubscriptionIndividual.address, countryOptions), false, None)
  val psaPreviousAddress = AnswerRow("common.previousAddress.checkyouranswers",
    addressAnswer(psaSubscriptionIndividual.previousAddress.get, countryOptions), false, None)
  def previousAddressExists(name: String) = AnswerRow(
    Message("moreThan12Months.label", name).resolve,
    Seq(messages(s"sameAddress.label.true")), false, None
  )

  val phoneNumber = AnswerRow("email.label", Seq("0044-09876542312"), false, None)

  val emailAddress = AnswerRow("phone.label", Seq("aaa@aa.com"), false, None)

  val vatNumber = AnswerRow("vat.label", Seq("12345678"), false, None)

  val payeNumber = AnswerRow("paye.label", Seq("9876543210"), false, None)

  val crn = AnswerRow("crn.label", Seq("1234567890"), false, None)

  val individualExpectedAnswerRows = Seq(
    individualDateOfBirth,
    individualNino,
    psaAddress,
    previousAddressExists("abcdefghijkl abcdefghijkl abcdefjkl"),
    psaPreviousAddress,
    emailAddress,
    phoneNumber
  )

  val companyExpectedAnswerRows = Seq(
    vatNumber,
    payeNumber,
    crn,
    psaAddress,
    previousAddressExists("Test company name"),
    psaPreviousAddress,
    emailAddress,
    phoneNumber
  )

  val partnershipExpectedAnswerRows = Seq(
    vatNumber,
    payeNumber,
    psaAddress,
    previousAddressExists("Test partnership name"),
    psaPreviousAddress,
    emailAddress,
    phoneNumber
  )

  val individualResult: Seq[SuperSection] = psaDetailsHelper(psaSubscriptionIndividual).individualSections
  val companyResult: Seq[SuperSection] = psaDetailsHelper(psaSubscriptionCompany).organisationSections
  val partnershipResult: Seq[SuperSection] = psaDetailsHelper(psaSubscriptionPartnership).organisationSections

  def actualSeqAnswerRow(result: Seq[SuperSection]): Seq[AnswerRow] = result.filter(_.headingKey.isEmpty).flatMap(_.sections).flatMap(_.rows)

}


