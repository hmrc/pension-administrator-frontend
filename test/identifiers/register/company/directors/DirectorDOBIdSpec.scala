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

package identifiers.register.company.directors

import java.time.LocalDate

import base.SpecBase
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.checkyouranswers.Ops._
import utils.{DateHelper, UserAnswers}
import viewmodels.{AnswerRow, Link, Message}

class DirectorDOBIdSpec extends SpecBase {

  private val personDetails = PersonName("test first", "test last")
  private val onwardUrl = "onwardUrl"

  "cya" when {
    def answers: UserAnswers =
      UserAnswers()
        .set(DirectorNameId(0))(personDetails).asOpt.value
        .set(DirectorDOBId(0))(value = LocalDate.now()).asOpt.value

    "in normal mode" must {
      "return answers rows with change links" in {
        val answerRows =
        Seq(
          AnswerRow(Message("dob.heading").withArgs(personDetails.fullName), Seq(DateHelper.formatDate(LocalDate.now())),
            answerIsMessageKey = false, Some(Link("site.change", onwardUrl)),
            Some(Message("dob.visuallyHidden.text").withArgs(personDetails.fullName))
          )
        )
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), answers)

        DirectorDOBId(0).row(Some(Link("site.change", onwardUrl)))(request, implicitly) must equal(answerRows)
      }
    }
  }
}
