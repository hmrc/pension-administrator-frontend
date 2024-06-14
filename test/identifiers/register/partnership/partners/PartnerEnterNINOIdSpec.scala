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

package identifiers.register.partnership.partners

import base.SpecBase
import models._
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Link, Message}

class PartnerEnterNINOIdSpec extends SpecBase {

  private val personDetails = PersonName("test first", "test last")
  private val onwardUrl = "onwardUrl"

  "Cleanup" when {
    def answers: UserAnswers =
      UserAnswers(Json.obj())
        .set(PartnerNoNINOReasonId(0))("reason")
        .asOpt
        .value

    "remove the data for `PartnerNoNINOReason`" in {
      val result: UserAnswers = answers.set(PartnerEnterNINOId(0))(ReferenceValue("nino", isEditable = true)).asOpt.value
      result.get(PartnerNoNINOReasonId(0)) mustNot be(defined)
    }
  }

  "cya" when {
    def answers: UserAnswers =
      UserAnswers()
        .set(PartnerNameId(0))(personDetails).asOpt.value
        .set(PartnerEnterNINOId(0))(value = ReferenceValue("test-nino")).asOpt.value

    "in normal mode" must {
      "return answers rows with change links" in {
        val answerRows =
          Seq(
            AnswerRow(Message("enterNINO.heading").withArgs(personDetails.fullName), Seq("test-nino"), answerIsMessageKey = false,
              Some(Link("site.change", onwardUrl)), Some(Message("enterNINO.visuallyHidden.text").withArgs(personDetails.fullName))
            )
          )
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), answers)

        PartnerEnterNINOId(0).row(Some(Link("site.change", onwardUrl)))(request, implicitly) must equal(answerRows)
      }
    }
  }
}
