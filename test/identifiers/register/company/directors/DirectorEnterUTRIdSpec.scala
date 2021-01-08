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
import models._
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Link, Message}

class DirectorEnterUTRIdSpec extends SpecBase {

  private val onwardUrl = "onwardUrl"

  "Cleanup" when {
    def answers: UserAnswers =
      UserAnswers(Json.obj())
        .set(DirectorNoUTRReasonId(0))("reason")
        .asOpt
        .value

    "remove the data for `DirectorNoUTRReason`" in {
      val result: UserAnswers = answers.set(DirectorEnterUTRId(0))(ReferenceValue("UTR", isEditable = true)).asOpt.value
      result.get(DirectorNoUTRReasonId(0)) mustNot be(defined)
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
            .directorEnterUTR(0, ReferenceValue("test-utr"))
            .directorName(0, PersonName("first", "last"))
        )

        DirectorEnterUTRId(0).row(Some(Link(onwardUrl)))(request, implicitly) must equal(
          Seq(AnswerRow(
            label = Message("enterUTR.heading", "first last"),
            answer = Seq("test-utr"), answerIsMessageKey = false,
            changeUrl = Some(Link(onwardUrl)),
            visuallyHiddenText = Some(Message("enterUTR.visuallyHidden.text", "first last"))
          ))
        )
      }

      "return answers rows with add links when has utr is true but no utr" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .directorHasUTR(0, flag = true)
            .directorName(0, PersonName("first", "last"))
        )

        DirectorEnterUTRId(0).row(Some(Link(onwardUrl)))(request, implicitly) must equal(
          Seq(AnswerRow(
            label = Message("enterUTR.heading", "first last"),
            answer = Seq("site.not_entered"),
            answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl, "site.add")),
            visuallyHiddenText = Some(Message("enterUTR.visuallyHidden.text", "first last"))
          ))
        )
      }

      "return no answers rows when has utr is false" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .directorHasUTR(0, flag = false)
            .directorName(0, PersonName("first", "last"))
        )

        DirectorEnterUTRId(0).row(Some(Link(onwardUrl)))(request, implicitly) must equal(Nil)
      }
    }
  }
}