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

package identifiers.register

import base.SpecBase
import models.requests.DataRequest
import models.{PSAUser, UserType}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Link, Message}

class EnterVatIdSpec extends SpecBase {
  private val onwardUrl = "onwardUrl"

  "cya" when {
    "in normal mode" must {

      "return answers rows with change links when enter paye has a value" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          UserAnswers().businessName().enterPaye("test-paye"))

        EnterPAYEId.row(Some(Link(onwardUrl)))(request, implicitly) must equal(Seq(
          AnswerRow(label = Message("enterPAYE.heading"), answer = Seq("test-paye"), answerIsMessageKey = false,
            changeUrl = Some(Link(onwardUrl)), Some(Message("enterPAYE.visuallyHidden.text", "test company")))))
      }

      "return answers rows with add links when has paye is true but enter paye has no value" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), UserAnswers().businessName().hasPaye(flag = true))

        EnterPAYEId.row(Some(Link(onwardUrl)))(request, implicitly) must equal(Seq(
          AnswerRow(label = Message("enterPAYE.heading"), answer = Seq("site.not_entered"), answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl, "site.add")), Some(Message("enterPAYE.visuallyHidden.text", "test company")))))
      }

      "return no answers rows has paye is false but enter paye has no value" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), UserAnswers().businessName().hasPaye(flag = false))

        EnterPAYEId.row(Some(Link(onwardUrl)))(request, implicitly) must equal(Nil)
      }
    }
  }
}


