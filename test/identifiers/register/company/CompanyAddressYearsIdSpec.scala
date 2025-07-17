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
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.{UserAnswerOps, UserAnswers}
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Link, Message}

class CompanyAddressYearsIdSpec extends SpecBase {

  private val address = Address("foo", "bar", None, None, None, "GB")
  private val onwardUrl = "onwardUrl"
  "Cleanup" when {

    val answersWithPreviousAddress = UserAnswers(Json.obj())
      .set(CompanyAddressYearsId)(AddressYears.UnderAYear)
      .flatMap(_.set(CompanyPreviousAddressPostCodeLookupId)(Seq.empty))
      .flatMap(_.set(CompanyPreviousAddressId)(Address("foo", "bar", None, None, None, "GB")))
      .asOpt.value

    "`AddressYears` is set to `OverAYear`" must {

      val result: UserAnswers = answersWithPreviousAddress.set(CompanyAddressYearsId)(AddressYears.OverAYear).asOpt.value

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(CompanyPreviousAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(CompanyPreviousAddressId) mustNot be(defined)
      }
    }

    "`AddressYears` is set to `UnderAYear`" must {

      val result: UserAnswers = answersWithPreviousAddress.set(CompanyAddressYearsId)(AddressYears.UnderAYear).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(CompanyPreviousAddressPostCodeLookupId) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(CompanyPreviousAddressId) mustBe defined
      }
    }

    "`AddressYears` is removed" must {

      val result: UserAnswers = answersWithPreviousAddress.remove(CompanyAddressYearsId).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(CompanyPreviousAddressPostCodeLookupId) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(CompanyPreviousAddressId) mustBe defined
      }
    }
  }

  "cya" when {
    "in normal mode" must {

      "return answers rows with change links when have value" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .companyAddressYears(AddressYears.OverAYear)
            .companyContactAddress(address).businessName()
        )

        CompanyAddressYearsId.row(Some(Link(onwardUrl)))(request, implicitly) must equal(
          Seq(AnswerRow(
            label = Message("addressYears.heading", "test company"),
            answer = Seq("common.addressYears.over_a_year"),
            answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl)),
            visuallyHiddenText = Some(Message("addressYears.visuallyHidden.text", "test company"))
          ))
        )
      }

      "return answers rows with add links when mandatory and have contact address" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .companyContactAddress(address).businessName()
        )

        CompanyAddressYearsId.row(Some(Link(onwardUrl)))(request, implicitly) must equal(
          Seq(AnswerRow(
            label = Message("addressYears.heading", "test company"),
            answer = Seq("site.not_entered"),
            answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl, "site.add")),
            visuallyHiddenText = Some(Message("addressYears.visuallyHidden.text", "test company"))
          ))
        )
      }

      "return no answers rows when not mandatory and not have contact address" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
        )

        CompanyAddressYearsId.row(Some(Link(onwardUrl)))(request, implicitly) must equal(Nil)
      }
    }
  }

}
