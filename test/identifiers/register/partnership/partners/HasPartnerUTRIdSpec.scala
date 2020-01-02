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

package identifiers.register.partnership.partners

import base.SpecBase
import models._
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers
import viewmodels.{AnswerRow, Link, Message}
import utils.checkyouranswers.Ops._

class HasPartnerUTRIdSpec extends SpecBase {

  import HasPartnerUTRIdSpec._

  "cya" when {
    def answers: UserAnswers =
      UserAnswers()
        .partnerName(index = 0, personDetails).partnerHasUTR(index = 0, flag = false)

    "in normal mode" must {
      "return answers rows with change links" in {
        val answerRows =
          Seq(
            AnswerRow(Message("hasUTR.heading", personDetails.fullName).resolve, Seq("site.no"), answerIsMessageKey = true,
              Some(Link("site.change", onwardUrl)), Some(Message("hasUTR.visuallyHidden.text").withArgs(personDetails.fullName))
            )
          )
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers)

        HasPartnerUTRId(0).row(Some(Link("site.change", onwardUrl)))(request, implicitly) must equal(answerRows)
      }
    }
  }

  "Cleanup" must {

    def answers(hasUtr: Boolean = true): UserAnswers = UserAnswers(Json.obj())
      .partnerHasUTR(index = 0, hasUtr)
      .partnerEnterUTR(index = 0, ReferenceValue("test-utr"))
      .partnerNoUTRReason(index = 0, reason = "reason")

    "remove the data for `PartnerUTR` when PartnerHasUTR is set to false" in {
      val result: UserAnswers = answers().set(HasPartnerUTRId(0))(value = false).asOpt.value
      result.get(PartnerEnterUTRId(0)) mustNot be(defined)
    }


    "remove the data for `PartnerNoUTRReason` when PartnerHasUTR is set to true" in {
      val result: UserAnswers = answers(hasUtr = false).set(HasPartnerUTRId(0))(value = true).asOpt.value
      result.get(PartnerNoUTRReasonId(0)) mustNot be(defined)
    }
  }
}

object HasPartnerUTRIdSpec {
  private val personDetails = PersonName("test first", "test last")
  private val onwardUrl = "onwardUrl"
}

