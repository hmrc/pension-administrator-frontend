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

package identifiers.register.partnership.partners

import models.Address
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class PartnerConfirmPreviousAddressIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "Cleanup" when {

    val answersWithContactAddress = UserAnswers(Json.obj())
      .set(PartnerConfirmPreviousAddressId(0))(true)
      .flatMap(_.set(PartnerPreviousAddressId(0))(Address("foo", "bar", None, None, None, "GB")))
      .asOpt.value

    "`PartnerConfirmPreviousAddress` is set to `false`" must {

      val result: UserAnswers = answersWithContactAddress.set(PartnerConfirmPreviousAddressId(0))(false).asOpt.value

      "remove the data for `PartnerPreviousAddress`" in {
        result.get(PartnerPreviousAddressId(0)) mustNot be(defined)
      }
    }

    "`PartnerConfirmPreviousAddress` is set to `true` (when it is already false)" must {

      val result: UserAnswers = answersWithContactAddress.set(PartnerConfirmPreviousAddressId(0))(true).asOpt.value

      "not remove the data for `PartnerPreviousAddress`" in {
        result.get(PartnerPreviousAddressId(0)) must be(defined)
      }
    }
  }
}
