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

package identifiers.register.company.directors

import base.SpecBase
import identifiers.register.DirectorsOrPartnersChangedId
import models._
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.checkyouranswers.Ops._
import utils.countryOptions.CountryOptions
import utils.{FakeCountryOptions, UserAnswers}
import viewmodels.{AnswerRow, Link, Message}

class DirectorAddressYearsIdSpec extends SpecBase {
  private val personDetails = models.PersonName("test first", "test last")
  private val onwardUrl = "onwardUrl"
  implicit val countryOptions: CountryOptions = new FakeCountryOptions(environment, frontendAppConfig)
  private val addressYears = AddressYears.OverAYear
  "Cleanup" when {

    val answersWithPreviousAddress = UserAnswers(Json.obj())
      .set(DirectorAddressYearsId(0))(AddressYears.UnderAYear)
      .flatMap(_.set(DirectorPreviousAddressPostCodeLookupId(0))(Seq.empty))
      .flatMap(_.set(DirectorPreviousAddressId(0))(Address("foo", "bar", None, None, None, "GB")))
      .flatMap(_.set(DirectorPreviousAddressListId(0))(TolerantAddress(Some("100"),
        Some("SuttonStreet"),
        Some("Wokingham"),
        Some("Surrey"),
        Some("NE39 1HX"),
        Some("GB"))))
      .asOpt.value

    "`AddressYears` is set to `OverAYear`" must {

      val result: UserAnswers = answersWithPreviousAddress.set(DirectorAddressYearsId(0))(AddressYears.OverAYear).asOpt.value

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(DirectorPreviousAddressPostCodeLookupId(0)) mustNot be(defined)
      }

      "remove the data for `PreviousAddressList`" in {
        result.get(DirectorPreviousAddressListId(0)) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(DirectorPreviousAddressId(0)) mustNot be(defined)
      }

      "set the change flag for `DirectorsOrPartnersChangedId`" in {
        result.get(DirectorsOrPartnersChangedId).value mustBe true
      }
    }

    "`AddressYears` is set to `UnderAYear`" must {

      val result: UserAnswers = answersWithPreviousAddress.set(DirectorAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(DirectorPreviousAddressPostCodeLookupId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddressList`" in {
        result.get(DirectorPreviousAddressListId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(DirectorPreviousAddressId(0)) mustBe defined
      }

      "don't set the change flag for `DirectorsOrPartnersChangedId`" in {
        result.get(DirectorsOrPartnersChangedId) mustNot be(defined)
      }
    }

    "`AddressYears` is removed" must {

      val result: UserAnswers = answersWithPreviousAddress.remove(DirectorAddressYearsId(0)).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(DirectorPreviousAddressPostCodeLookupId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddressList`" in {
        result.get(DirectorPreviousAddressListId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(DirectorPreviousAddressId(0)) mustBe defined
      }
    }
  }

  "cya" when {
    val address = Address("foo", "bar", None, None, None, "GB")
    "in normal mode" must {

      "return answers rows with change links when have value" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None), UserAnswers().directorAddressYears(0, AddressYears.OverAYear).
            directorName(0, PersonName("first", "last")))

        DirectorAddressYearsId(0).row(Some(Link(onwardUrl)))(request, implicitly) must equal(Seq(
          AnswerRow(label = Message("addressYears.heading"),
            answer = Seq("common.addressYears.over_a_year"), answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl)), Some(Message("addressYears.visuallyHidden.text", "first last")))))
      }

      "return answers rows with add links when there is address but no address years" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None),
          UserAnswers().directorAddress(0, address).
            directorName(0, PersonName("first", "last")))

        DirectorAddressYearsId(0).row(Some(Link(onwardUrl)))(request, implicitly) must equal(Seq(
          AnswerRow(label = Message("addressYears.heading"), answer = Seq("site.not_entered"), answerIsMessageKey = true,
            changeUrl = Some(Link(onwardUrl, "site.add")), Some(Message("addressYears.visuallyHidden.text", "first last")))))
      }

      "return no answers rows when there is no address and address years" in {
        val request: DataRequest[AnyContent] = DataRequest(FakeRequest(), "id",
          PSAUser(UserType.Organisation, None, isExistingPSA = false, None), UserAnswers().
            directorName(0, PersonName("first", "last")))

        DirectorAddressYearsId(0).row(Some(Link(onwardUrl)))(request, implicitly) must equal(Nil)
      }
    }
  }

}
