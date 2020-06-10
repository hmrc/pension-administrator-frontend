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

package identifiers.register.adviser

import base.SpecBase
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Link, Message}

class AdviserEmailIdSpec extends SpecBase {

  private val personDetails = "test first test last"
  private val onwardUrl = "onwardUrl"

  "cya" when {
    def answers: UserAnswers =
      UserAnswers()
        .set(AdviserNameId)(personDetails).asOpt.value
        .set(AdviserEmailId)(value = "test@test.com").asOpt.value

    "in normal mode" must {
      "return answers rows with change links" in {
        val answerRows =
          Seq(
            AnswerRow(Message("email.title").withArgs(personDetails), Seq("test@test.com"), answerIsMessageKey = false,
              Some(Link("site.change", onwardUrl)), Some(Message("email.visuallyHidden.text").withArgs(personDetails))
            )
          )
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers)

        AdviserEmailId.row(Some(Link("site.change", onwardUrl)))(request, implicitly) must equal(answerRows)
      }
    }
  }
}
