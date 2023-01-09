/*
 * Copyright 2023 HM Revenue & Customs
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

import models.Address
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class CompanySameContactAddressIdSpec extends AnyWordSpecLike with Matchers with OptionValues with Enumerable.Implicits {

  "Cleanup" when {

    val answersWithContactAddress = UserAnswers(Json.obj())
      .set(CompanySameContactAddressId)(false)
      .flatMap(_.set(CompanyContactAddressPostCodeLookupId)(Seq.empty))
      .flatMap(_.set(CompanyContactAddressId)(Address("foo", "bar", None, None, None, "GB")))
      .flatMap(_.set(CompanyPreviousAddressPostCodeLookupId)(Seq.empty))
      .flatMap(_.set(CompanyPreviousAddressId)(Address("previous-foo", "previous-bar", None, None, None, "GB")))
      .asOpt.value

    "`CompanySameContactAddress` is set to `true`" must {

      val result: UserAnswers = answersWithContactAddress.set(CompanySameContactAddressId)(true).asOpt.value

      "remove the data for `CompanyContactAddress`" in {
        result.get(CompanyContactAddressId) mustNot be(defined)
      }

      "remove the data for `CompanyContactAddressPostCodeLookup`" in {
        result.get(CompanyContactAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(CompanyPreviousAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(CompanyPreviousAddressId) mustNot be(defined)
      }

      "remove the data for `AddressYears`" in {
        result.get(CompanyAddressYearsId) mustNot be(defined)
      }
    }

    "`CompanySameContactAddress` is set to `false` (when already set to false)" must {

      val result: UserAnswers = answersWithContactAddress.set(CompanySameContactAddressId)(false).asOpt.value

      "not remove the data for `CompanyContactAddress`" in {
        result.get(CompanyContactAddressId) must be(defined)
      }

      "not remove the data for `CompanyContactAddressPostCodeLookup`" in {
        result.get(CompanyContactAddressPostCodeLookupId) must be(defined)
      }

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(CompanyPreviousAddressPostCodeLookupId) must be(defined)
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(CompanyPreviousAddressId) must be(defined)
      }
    }

    "`CompanySameContactAddress` is removed" must {

      val result: UserAnswers = answersWithContactAddress.remove(CompanySameContactAddressId).asOpt.value

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(CompanyPreviousAddressPostCodeLookupId) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(CompanyPreviousAddressId) mustNot be(defined)
      }

      "remove the data for `AddressYears`" in {
        result.get(CompanyAddressYearsId) mustNot be(defined)
      }
    }
  }
}
