/*
 * Copyright 2019 HM Revenue & Customs
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
import models.requests.DataRequest
import models.{PSAUser, PersonDetails, ReferenceValue, UserType}
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Link, Message}

class HasDirectorUTRIdSpec extends SpecBase {

  private val personDetails = PersonDetails("test first", None, "test last", LocalDate.now)
  private val onwardUrl = "onwardUrl"
  private def ua(hasUTR: Boolean = true): UserAnswers = UserAnswers(Json.obj())
    .set(HasDirectorUTRId(0))(hasUTR)
    .flatMap(_.set(DirectorEnterUTRId(0))(value = ReferenceValue("test-UTR")))
    .flatMap(_.set(DirectorNoUTRReasonId(0))(value = "reason"))
    .asOpt.value

  "cya" when {
    def answers: UserAnswers =
      UserAnswers()
        .set(DirectorDetailsId(0))(personDetails).asOpt.value
        .set(HasDirectorUTRId(0))(value = false).asOpt.value

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

        HasDirectorUTRId(0).row(Some(Link("site.change", onwardUrl)))(request, implicitly) must equal(answerRows)
      }
    }
  }
}