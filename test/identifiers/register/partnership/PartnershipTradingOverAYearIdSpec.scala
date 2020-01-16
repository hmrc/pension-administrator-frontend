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

package identifiers.register.partnership

import models.{Address, TolerantAddress}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class PartnershipTradingOverAYearIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "Cleanup" when {
    "`PartnershipTradingOverAYearId` has been set to `true`" must {
      "remove `PartnershipPreviousAddressPostCodeLookupId`, `PartnershipPreviousAddressId`, and `PartnershipPreviousAddressListId`" in {
        val ua = UserAnswers(Json.obj(
          PartnershipPreviousAddressPostCodeLookupId.toString -> Seq(TolerantAddress(None, None, None, None, None, None)),
          PartnershipPreviousAddressId.toString -> Address("", "", None, None, None, ""),
          PartnershipPreviousAddressListId.toString -> TolerantAddress(None, None, None, None, None, None)
        ))

        val result = PartnershipTradingOverAYearId.cleanup(Some(true), ua).asOpt.value

        result.get(PartnershipPreviousAddressPostCodeLookupId) mustBe None
        result.get(PartnershipPreviousAddressId) mustBe None
        result.get(PartnershipPreviousAddressListId) mustBe None
      }

    }

    "`PartnershipTradingOverAYearId` has been set to `false`" must {
      "not remove `PartnershipPreviousAddressPostCodeLookupId`, `PartnershipPreviousAddressId`, and `PartnershipPreviousAddressListId`" in {
        val ua = UserAnswers(Json.obj(
          PartnershipPreviousAddressPostCodeLookupId.toString -> Seq(TolerantAddress(None, None, None, None, None, None)),
          PartnershipPreviousAddressId.toString -> Address("", "", None, None, None, ""),
          PartnershipPreviousAddressListId.toString -> TolerantAddress(None, None, None, None, None, None)
        ))

        val result = PartnershipTradingOverAYearId.cleanup(Some(false), ua).asOpt.value

        result.get(PartnershipPreviousAddressPostCodeLookupId) mustBe Some(Seq(TolerantAddress(None, None, None, None, None, None)))
        result.get(PartnershipPreviousAddressId) mustBe Some(Address("", "", None, None, None, ""))
        result.get(PartnershipPreviousAddressListId) mustBe Some(TolerantAddress(None, None, None, None, None, None))
      }
    }
  }

}
