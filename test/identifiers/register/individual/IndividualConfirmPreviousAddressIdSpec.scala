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

package identifiers.register.individual

import models.Address
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class IndividualConfirmPreviousAddressIdSpec extends AnyWordSpecLike with Matchers with OptionValues with Enumerable.Implicits {

  "Cleanup" when {

    val answersWithContactAddress = UserAnswers(Json.obj())
      .set(IndividualConfirmPreviousAddressId)(true)
      .flatMap(_.set(IndividualPreviousAddressId)(Address("foo", "bar", None, None, None, "GB")))
      .asOpt.value

    "`IndividualConfirmPreviousAddress` is set to `false`" must {

      val result: UserAnswers = answersWithContactAddress.set(IndividualConfirmPreviousAddressId)(false).asOpt.value

      "remove the data for `IndividualPreviousAddress`" in {
        result.get(IndividualPreviousAddressId) mustNot be(defined)
      }
    }

    "`IndividualConfirmPreviousAddress` is set to `true` (when it is already false)" must {

      val result: UserAnswers = answersWithContactAddress.set(IndividualConfirmPreviousAddressId)(true).asOpt.value

      "not remove the data for `IndividualPreviousAddress`" in {
        result.get(IndividualPreviousAddressId) must be(defined)
      }
    }
  }
}
