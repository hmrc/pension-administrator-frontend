/*
 * Copyright 2021 HM Revenue & Customs
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

import models.Address
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class PartnershipConfirmPreviousAddressIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "Cleanup" when {

    val answersWithContactAddress = UserAnswers(Json.obj())
      .set(PartnershipConfirmPreviousAddressId)(true)
      .flatMap(_.set(PartnershipPreviousAddressId)(Address("foo", "bar", None, None, None, "GB")))
      .asOpt.value

    "`PartnershipConfirmPreviousAddress` is set to `false`" must {

      val result: UserAnswers = answersWithContactAddress.set(PartnershipConfirmPreviousAddressId)(false).asOpt.value

      "remove the data for `PartnershipPreviousAddress`" in {
        result.get(PartnershipPreviousAddressId) mustNot be(defined)
      }
    }

    "`PartnershipConfirmPreviousAddress` is set to `true` (when it is already false)" must {

      val result: UserAnswers = answersWithContactAddress.set(PartnershipConfirmPreviousAddressId)(true).asOpt.value

      "not remove the data for `PartnershipPreviousAddress`" in {
        result.get(PartnershipPreviousAddressId) must be(defined)
      }
    }
  }
}
