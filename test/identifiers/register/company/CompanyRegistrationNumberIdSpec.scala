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

package identifiers.register.company

import base.SpecBase
import models.requests.DataRequest
import models.{AddressYears, PSAUser, UserType}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers
import utils.checkyouranswers.Ops._
import viewmodels.{AnswerRow, Link, Message}

class CompanyRegistrationNumberIdSpec extends SpecBase {

  private val onwardUrl = "onwardUrl"

  "cya" when {
    "in normal mode" must {

      "return no answers rows when not in uk" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None), UserAnswers().areYouInUk(false))

        CompanyRegistrationNumberId.row(Some(Link(onwardUrl)))(request, implicitly) must equal(Nil)
      }

      "return answers rows with add link when in uk, has company crn is true and no crn" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None), UserAnswers().businessName()
        .companyHasCrn(true).areYouInUk(true))

        CompanyRegistrationNumberId.row(Some(Link(onwardUrl)))(request, implicitly) must equal(Seq(
          AnswerRow(label = Message("companyRegistrationNumber.heading"), answer = Seq("site.not_entered"), answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl, "site.add")), Some("companyRegistrationNumber.visuallyHidden.text"))))
      }

      "return answers rows with change links when crn has value" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None), UserAnswers().companyCrn("test-crn").businessName())

        CompanyRegistrationNumberId.row(Some(Link(onwardUrl)))(request, implicitly) must equal(Seq(
          AnswerRow(label = Message("companyRegistrationNumber.heading"),
            answer = Seq("test-crn"), answerIsMessageKey = false,
            changeUrl = Some(Link(onwardUrl)), Some("companyRegistrationNumber.visuallyHidden.text"))))
      }

      "return no answers rows when has Crn is false and no value" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None), UserAnswers().companyHasCrn(false).businessName())

        CompanyRegistrationNumberId.row(Some(Link(onwardUrl)))(request, implicitly) must equal(Nil)
      }
    }
  }

}