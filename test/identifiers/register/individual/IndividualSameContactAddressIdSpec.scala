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

package identifiers.register.individual

import models.{Address, AddressYears}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class IndividualSameContactAddressIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "Cleanup" when {

    val answersWithContactAddress = UserAnswers(Json.obj())
      .set(IndividualSameContactAddressId)(false)
      .flatMap(_.set(IndividualContactAddressPostCodeLookupId)(Seq.empty))
      .flatMap(_.set(IndividualContactAddressId)(Address("foo", "bar", None, None, None, "GB")))
      .flatMap(_.set(IndividualPreviousAddressPostCodeLookupId)(Seq.empty))
      .flatMap(_.set(IndividualPreviousAddressId)(Address("previous-foo", "previous-bar", None, None, None, "GB")))
      .asOpt.value

    "`IndividualSameContactAddress` is set to `true`" must {

      val result: UserAnswers = answersWithContactAddress.set(IndividualSameContactAddressId)(true).asOpt.value

      "remove the data for `IndividualContactAddress`" in {
        result.get(IndividualContactAddressId) mustNot be(defined)
      }

      "remove the data for `IndividualContactAddressPostCodeLookup`" in {
        result.get(IndividualContactAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(IndividualPreviousAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(IndividualPreviousAddressId) mustNot be(defined)
      }

      "remove the data for `AddressYears`" in {
        result.get(IndividualAddressYearsId) mustNot be(defined)
      }
    }

    "`IndividualSameContactAddress` is set to `false`" must {

      val result: UserAnswers = answersWithContactAddress.set(IndividualSameContactAddressId)(false).asOpt.value

      "remove the data for `IndividualContactAddress`" in {
        result.get(IndividualContactAddressId) mustNot be(defined)
      }

      "remove the data for `IndividualContactAddressPostCodeLookup`" in {
        result.get(IndividualContactAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(IndividualPreviousAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(IndividualPreviousAddressId) mustNot be(defined)
      }

      "remove the data for `AddressYears`" in {
        result.get(IndividualAddressYearsId) mustNot be(defined)
      }
    }

    "`IndividualSameContactAddress` is removed" must {

      val result: UserAnswers = answersWithContactAddress.remove(IndividualSameContactAddressId).asOpt.value

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(IndividualPreviousAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(IndividualPreviousAddressId) mustNot be(defined)
      }

      "remove the data for `AddressYears`" in {
        result.get(IndividualAddressYearsId) mustNot be(defined)
      }
    }
  }
}
