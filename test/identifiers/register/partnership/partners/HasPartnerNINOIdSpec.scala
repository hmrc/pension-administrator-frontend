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

package identifiers.register.partnership.partners

import java.time.LocalDate

import base.SpecBase
import models._
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Link, Message}

class HasPartnerNINOIdSpec extends SpecBase {

  private val personDetails = PersonDetails("test first", None, "test last", LocalDate.now())
  private val onwardUrl = "onwardUrl"

  "Cleanup" when {

    def answers(hasNino: Boolean = true): UserAnswers = UserAnswers(Json.obj())
      .set(HasPartnerNINOId(0))(hasNino)
      .flatMap(_.set(PartnerEnterNINOId(0))(value = ReferenceValue("test-nino")))
      .flatMap(_.set(PartnerNoNINOReasonId(0))(value = "reason"))
      .asOpt.value

    "`HasPartnerNINO` is set to `false`" must {

      val result: UserAnswers = answers().set(HasPartnerNINOId(0))(value = false).asOpt.value

      "remove the data for `PartnerNino`" in {
        result.get(PartnerEnterNINOId(0)) mustNot be(defined)
      }
    }

    "`HasPartnerNINO` is set to `true`" must {

      val result: UserAnswers = answers(hasNino = false).set(HasPartnerNINOId(0))(value = true).asOpt.value

      "remove the data for `PartnerNoNinoReason`" in {
        result.get(PartnerNoNINOReasonId(0)) mustNot be(defined)
      }
    }

    "`HasPartnerNINO` is not present" must {

      val result: UserAnswers = answers().remove(HasPartnerNINOId(0)).asOpt.value

      "not remove the data for `PartnerNoNinoReason`" in {
        result.get(PartnerNoNINOReasonId(0)) mustBe defined
      }
    }
  }

  "cya" when {
    def answers: UserAnswers =
      UserAnswers()
        .set(PartnerDetailsId(0))(personDetails).asOpt.value
        .set(HasPartnerNINOId(0))(value = false).asOpt.value

    "in normal mode" must {
      "return answers rows with change links" in {
        val answerRows =
        Seq(
          AnswerRow(Message("hasNINO.heading", personDetails.fullName).resolve, Seq("site.no"), answerIsMessageKey = true,
            Some(Link("site.change", onwardUrl)), Some(Message("hasNINO.visuallyHidden.text").withArgs(personDetails.fullName))
          )
        )
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None), answers)

        HasPartnerNINOId(0).row(Some(Link("site.change", onwardUrl)))(request, implicitly) must equal(answerRows)
      }
    }
  }
}
