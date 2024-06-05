/*
 * Copyright 2023 HM Revenue & Customs
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

package identifiers.register.company

import base.SpecBase
import models.requests.DataRequest
import models.{Address, AddressYears, PSAUser, UserType}
import play.api.mvc.AnyContent
import utils.FakeRequest
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Link, Message}

class CompanyPreviousAddressIdSpec extends SpecBase {

  private val address = Address("foo", "bar", None, None, None, "GB")
  private val onwardUrl = "onwardUrl"
  implicit val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)

  "cya" when {
    "in normal mode" must {

      "return answers rows with change links when have value" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .companyPreviousAddress(address)
            .businessName()
        )

        CompanyPreviousAddressId.row(Some(Link(onwardUrl)))(request, implicitly) must equal(
          Seq(AnswerRow(
            label = Message("previousAddress.checkYourAnswersLabel", "test company"),
            answer = address.lines(countryOptions),
            answerIsMessageKey = false,
            changeUrl = Some(Link(onwardUrl)),
            visuallyHiddenText = Some(Message("previousAddress.visuallyHidden.text", "test company"))
          ))
        )
      }

      "return answers rows with add links when address years is under a year, trading over a year is true and no previous address" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .companyAddressYears(AddressYears.UnderAYear)
            .companyTradingOverAYear(flag = true)
            .businessName()
        )

        CompanyPreviousAddressId.row(Some(Link(onwardUrl)))(request, implicitly) must equal(
          Seq(AnswerRow(
            label = Message("previousAddress.checkYourAnswersLabel", "test company"),
            answer = Seq("site.not_entered"),
            answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl, "site.add")),
            visuallyHiddenText = Some(Message("previousAddress.visuallyHidden.text", "test company"))
          ))
        )
      }

      "return no answers rows when not mandatory and not have contact address" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .companyAddressYears(AddressYears.OverAYear).businessName())

        CompanyPreviousAddressId.row(Some(Link(onwardUrl)))(request, implicitly) must equal(Nil)
      }
    }
  }

}
