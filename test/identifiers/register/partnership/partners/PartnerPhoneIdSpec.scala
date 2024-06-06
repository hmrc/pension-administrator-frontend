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
import play.api.mvc.AnyContent
import utils.FakeRequest
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Link, Message}

class PartnerPhoneIdSpec extends SpecBase {

  private val personDetails = PersonName("test first", "test last")
  private val onwardUrl = "onwardUrl"

  "cya" when {
    def answers: UserAnswers =
      UserAnswers()
        .set(PartnerNameId(0))(personDetails).asOpt.value
        .set(PartnerPhoneId(0))(value = "1234").asOpt.value

    "in normal mode" must {
      "return answers rows with change links" in {
        val answerRows =
          Seq(
            AnswerRow(Message("phone.title").withArgs(personDetails.fullName), Seq("1234"), answerIsMessageKey = false,
              Some(Link("site.change", onwardUrl)), Some(Message("phone.visuallyHidden.text").withArgs(personDetails.fullName))
            )
          )
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), answers)

        PartnerPhoneId(0).row(Some(Link("site.change", onwardUrl)))(request, implicitly) must equal(answerRows)
      }
    }
  }
}
