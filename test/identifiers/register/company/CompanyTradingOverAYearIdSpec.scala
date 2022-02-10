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

package identifiers.register.company

import models.{Address, TolerantAddress}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class CompanyTradingOverAYearIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "Cleanup" when {
    "CompanyTradingOverAYearId is set to 'true'" must {
      "remove 'CompanyPreviousAddressPostCodeLookupId' and 'CompanyPreviousAddressId' when true" in {
        val ua = UserAnswers(Json.obj(
          CompanyPreviousAddressPostCodeLookupId.toString -> Seq(TolerantAddress(None, None, None, None, None, None)),
          CompanyPreviousAddressId.toString -> Address("", "", None, None, None, ""))
        )

        val result = CompanyTradingOverAYearId.cleanup(Some(true), ua).asOpt.value
        result.get(CompanyPreviousAddressPostCodeLookupId) mustBe None
        result.get(CompanyPreviousAddressId) mustBe None
      }

      "not remove 'CompanyPreviousAddressPostCodeLookupId' and 'CompanyPreviousAddressId' when false" in {
        val ua = UserAnswers(Json.obj(
          CompanyPreviousAddressPostCodeLookupId.toString -> Seq(TolerantAddress(None, None, None, None, None, None)),
          CompanyPreviousAddressId.toString -> Address("", "", None, None, None, ""))
        )

        val result = CompanyTradingOverAYearId.cleanup(Some(false), ua).asOpt.value
        result.get(CompanyPreviousAddressPostCodeLookupId) mustBe Some(Seq(TolerantAddress(None, None, None, None, None, None)))
        result.get(CompanyPreviousAddressId) mustBe Some(Address("", "", None, None, None, ""))
      }
    }
  }

}
