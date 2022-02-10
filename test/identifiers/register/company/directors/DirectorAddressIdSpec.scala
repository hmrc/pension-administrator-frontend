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

import base.SpecBase
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Link, Message}

class DirectorAddressIdSpec extends SpecBase {
  private val personDetails = models.PersonName("test first", "test last")
  private val address = Address("line1", "line2", None, None, None, "country")
  implicit val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val index = 0
  private val onwardUrl = controllers.register.company.directors.routes.DirectorAddressController.onPageLoad(NormalMode, index)

  "cya" when {
    def answers: UserAnswers =
      UserAnswers()
        .set(DirectorNameId(0))(personDetails).asOpt.value
        .set(DirectorAddressId(0))(value = address).asOpt.value

    "in normal mode" must {
      "return answers rows with change links" in {
        val answerRows =
          Seq(AnswerRow(
            Message("address.checkYourAnswersLabel").withArgs(personDetails.fullName),
            address.lines(countryOptions),
            answerIsMessageKey = false,
            Some(Link(onwardUrl.url)),
            visuallyHiddenText = Some(Message("address.visuallyHidden.text").withArgs(personDetails.fullName))
          ))
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""), answers)

        DirectorAddressId(index).row(Some(Link(onwardUrl.url)))(request, implicitly) must equal(answerRows)
      }
    }
  }
}
