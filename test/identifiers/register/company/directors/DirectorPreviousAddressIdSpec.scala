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
import controllers.register.company.directors._
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Link, Message}

class DirectorPreviousAddressIdSpec extends SpecBase {
  private val address = Address("line1", "line2", None, None, None, "country")
  implicit val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val index = 0
  private val onwardUrl = routes.DirectorPreviousAddressController.onPageLoad(NormalMode, index).url

  "cya" when {
    "in normal mode" must {

      "return answers rows with change links when have value" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .directorPreviousAddress(index, address)
            .directorName(index, PersonName("first", "last"))
        )

        DirectorPreviousAddressId(index).row(Some(Link(onwardUrl)))(request, implicitly) must equal(
          Seq(AnswerRow(
            label = Message("previousAddress.checkYourAnswersLabel", "first last"),
            answer = address.lines(countryOptions),
            answerIsMessageKey = false,
            changeUrl = Some(Link(onwardUrl)),
            visuallyHiddenText = Some(Message("previousAddress.visuallyHidden.text", "first last"))
          ))
        )
      }

      "return answers rows with add links when address years is under a year, and no previous address" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .directorAddressYears(index, AddressYears.UnderAYear)
            .directorName(index, PersonName("first", "last"))
        )

        DirectorPreviousAddressId(index).row(Some(Link(onwardUrl)))(request, implicitly) must equal(
          Seq(AnswerRow(
            label = Message("previousAddress.checkYourAnswersLabel", "first last"),
            answer = Seq("site.not_entered"),
            answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl, "site.add")),
            visuallyHiddenText = Some(Message("previousAddress.visuallyHidden.text", "first last"))
          ))
        )
      }

      "return no answers rows when not mandatory and not have previous address" in {
        val request: DataRequest[AnyContent] = DataRequest(
          request = FakeRequest(),
          externalId = "id",
          user = PSAUser(UserType.Organisation, None, isExistingPSA = false, None, None, ""),
          userAnswers = UserAnswers()
            .directorAddressYears(index, AddressYears.OverAYear)
            .directorName(index, PersonName("first", "last"))
        )

        DirectorPreviousAddressId(index).row(Some(Link(onwardUrl)))(request, implicitly) must equal(Nil)
      }
    }
  }
}
