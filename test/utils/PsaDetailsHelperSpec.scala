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
      val expectedLabels = expectedAnswerRows.map(_.label).toSet

      actualLabels mustBe expectedLabels
    }

    "display Individual details section with correct values" in {
      val actualValues = actualSeqAnswerRow(individualResult).map(_.answer).toSet
      val expectedValues = expectedAnswerRows.map(_.answer).toSet

      actualValues mustBe expectedValues
    }
  }
}

object PsaDetailsHelperSpec extends SpecBase {

  val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  val psaSubscriptionIndividual = PsaSubscription(false, customerId, None, Some(individual), address, contactDetails,
    true, Some(previousAddress), None, Some(pensionsAdvisor))

  val psaDetailsHelperIndividual = new PsaDetailsHelper(psaSubscriptionIndividual, countryOptions)

  val individualDateOfBirth = AnswerRow("cya.label.dob", Seq("29/03/1947"), false, None)
  val individualNino = AnswerRow("common.nino", Seq("AA999999A"), false, None)
  val individualAddress = AnswerRow("cya.label.address", addressAnswer(psaSubscriptionIndividual.address, countryOptions), false, None)
  val psaPreviousAddress = AnswerRow("common.previousAddress.checkyouranswers",
    addressAnswer(psaSubscriptionIndividual.previousAddress.get, countryOptions), false, None)
  val previousAddressExists = AnswerRow(
    Message("moreThan12Months.label", "abcdefghijkl abcdefghijkl abcdefjkl").resolve,
    Seq(messages(s"sameAddress.label.true")), false, None
  )

  val phoneNumber = AnswerRow("email.label", Seq("0044-09876542312"), false, None)

  val emailAddress = AnswerRow("phone.label", Seq("aaa@aa.com"), false, None)

  val expectedAnswerRows = Seq(
    individualDateOfBirth,
    individualNino,
    individualAddress,
    previousAddressExists,
    psaPreviousAddress,
    emailAddress,
    phoneNumber
  )

  val individualResult: Seq[SuperSection] = psaDetailsHelperIndividual.individualSections

  def actualSeqAnswerRow(result: Seq[SuperSection]): Seq[AnswerRow] = result.filter(_.headingKey.isEmpty).flatMap(_.sections).flatMap(_.rows)

}


