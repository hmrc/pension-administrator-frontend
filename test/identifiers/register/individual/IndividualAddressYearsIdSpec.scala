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

package identifiers.register.individual

import models.{Address, AddressYears, TolerantAddress}
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class IndividualAddressYearsIdSpec extends AnyWordSpecLike with Matchers with OptionValues with Enumerable.Implicits {

  "Cleanup" when {

    val answersWithPreviousAddress = UserAnswers(Json.obj())
      .set(IndividualAddressYearsId)(AddressYears.UnderAYear)
      .flatMap(_.set(IndividualPreviousAddressPostCodeLookupId)(Seq.empty))
      .flatMap(_.set(IndividualPreviousAddressListId)(TolerantAddress(Some("foo"), Some("bar"), None, None, None, Some("GB"))))
      .flatMap(_.set(IndividualPreviousAddressId)(Address("foo", "bar", None, None, None, "GB")))
      .asOpt.value

    "`AddressYears` is set to `OverAYear`" must {

      val result: UserAnswers = answersWithPreviousAddress.set(IndividualAddressYearsId)(AddressYears.OverAYear).asOpt.value

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(IndividualPreviousAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for `PreviousAddressList`" in {
        result.get(IndividualPreviousAddressListId) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(IndividualPreviousAddressId) mustNot be(defined)
      }
    }

    "`AddressYears` is set to `UnderAYear`" must {

      val result: UserAnswers = answersWithPreviousAddress.set(IndividualAddressYearsId)(AddressYears.UnderAYear).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(IndividualPreviousAddressPostCodeLookupId) mustBe defined
      }

      "not remove the data for `PreviousAddressList`" in {
        result.get(IndividualPreviousAddressListId) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(IndividualPreviousAddressId) mustBe defined
      }
    }

    "`AddressYears` is removed" must {

      val result: UserAnswers = answersWithPreviousAddress.remove(IndividualAddressYearsId).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(IndividualPreviousAddressPostCodeLookupId) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(IndividualPreviousAddressId) mustBe defined
      }
    }
  }
}
