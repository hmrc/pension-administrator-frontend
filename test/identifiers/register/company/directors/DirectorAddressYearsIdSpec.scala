/*
 * Copyright 2018 HM Revenue & Customs
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

import models.{Address, AddressYears, TolerantAddress}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class DirectorAddressYearsIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

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

}
