/*
 * Copyright 2022 HM Revenue & Customs
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

package identifiers.register.partnership.partners

import base.SpecBase
import identifiers.register.DirectorsOrPartnersChangedId
import models._
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Link, Message}

class PartnerAddressYearsIdSpec extends SpecBase {

  "Cleanup" when {

    val answersWithPreviousAddress = UserAnswers(Json.obj())
      .set(PartnerAddressYearsId(0))(AddressYears.UnderAYear)
      .flatMap(_.set(PartnerPreviousAddressPostCodeLookupId(0))(Seq.empty))
      .flatMap(_.set(PartnerPreviousAddressId(0))(Address("foo", "bar", None, None, None, "GB")))
      .asOpt.value

    "`AddressYears` is set to `OverAYear`" must {

      val result: UserAnswers = answersWithPreviousAddress.set(PartnerAddressYearsId(0))(AddressYears.OverAYear).asOpt.value

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(PartnerPreviousAddressPostCodeLookupId(0)) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(PartnerPreviousAddressId(0)) mustNot be(defined)
      }

      "set the change flag for `DirectorsOrPartnersChangedId`" in {
        result.get(DirectorsOrPartnersChangedId).value mustBe true
      }
    }

    "`AddressYears` is set to `UnderAYear`" must {

      val result: UserAnswers = answersWithPreviousAddress.set(PartnerAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(PartnerPreviousAddressPostCodeLookupId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(PartnerPreviousAddressId(0)) mustBe defined
      }

      "don't set the change flag for `DirectorsOrPartnersChangedId`" in {
        result.get(DirectorsOrPartnersChangedId) mustNot be(defined)
      }
    }

    "`AddressYears` is removed" must {

      val result: UserAnswers = answersWithPreviousAddress.remove(PartnerAddressYearsId(0)).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(PartnerPreviousAddressPostCodeLookupId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(PartnerPreviousAddressId(0)) mustBe defined
      }
    }
  }

  "cya" when {
    val partnerDetails = PersonName("test first", "test last")
    val addressYears = AddressYears.OverAYear
    val onwardUrl = "onwardUrl"
    def answers: UserAnswers =
      UserAnswers()
        .set(PartnerNameId(0))(partnerDetails).asOpt.value
        .set(PartnerAddressYearsId(0))(value = AddressYears.OverAYear).asOpt.value

    "in normal mode" must {
      "return answers rows with change links" in {
        val answerRows =
          Seq(AnswerRow(
            Message("addressYears.heading").withArgs(partnerDetails.fullName),
            Seq(s"common.addressYears.${addressYears.toString}"),
            answerIsMessageKey = true,
            Some(Link(onwardUrl)),
            Some(Message("addressYears.visuallyHidden.text").withArgs(partnerDetails.fullName))
          ))
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), answers)

        PartnerAddressYearsId(0).row(Some(Link(onwardUrl)))(request, implicitly) must equal(answerRows)
      }
    }
  }

}
