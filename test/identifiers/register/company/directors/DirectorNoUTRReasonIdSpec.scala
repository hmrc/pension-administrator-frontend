/*
 * Copyright 2021 HM Revenue & Customs
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

import base.SpecBase
import models.requests.DataRequest
import models.{PSAUser, PersonName, UserType}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Link, Message}

class DirectorNoUTRReasonIdSpec extends SpecBase {
  private val onwardUrl = "onwardUrl"

  "cya" when {
    "in normal mode" must {

      "return answers rows with change links when have value" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers().directorNoUTRReason(0, "no reason")
            .directorName(0, PersonName("first", "last"))
        )

        DirectorNoUTRReasonId(0).row(Some(Link(onwardUrl)))(request, implicitly) must equal(
          Seq(AnswerRow(
            label = Message("whyNoUTR.heading", "first last"),
            answer = Seq("no reason"),
            answerIsMessageKey = false,
            changeUrl = Some(Link(onwardUrl)),
            visuallyHiddenText = Some(Message("whyNoUTR.visuallyHidden.text", "first last"))
          ))
        )
      }

      "return answers rows with add links when has utr is false but no utr reason" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .directorHasUTR(0, flag = false)
            .directorName(0, PersonName("first", "last"))
        )

        DirectorNoUTRReasonId(0).row(Some(Link(onwardUrl)))(request, implicitly) must equal(
          Seq(AnswerRow(
            label = Message("whyNoUTR.heading", "first last"),
            answer = Seq("site.not_entered"),
            answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl, "site.add")),
            visuallyHiddenText = Some(Message("whyNoUTR.visuallyHidden.text", "first last"))
          ))
        )
      }

      "return no answers rows when has utr is true" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .directorHasUTR(0, flag = true)
            .directorName(0, PersonName("first", "last"))
        )

        DirectorNoUTRReasonId(0).row(Some(Link(onwardUrl)))(request, implicitly) must equal(Nil)
      }
    }
  }
}