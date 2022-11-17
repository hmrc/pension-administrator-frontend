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

package identifiers.register.company.directors

import models.Address
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class DirectorConfirmPreviousAddressIdSpec extends AnyWordSpecLike with Matchers with OptionValues with Enumerable.Implicits {

  "Cleanup" when {

    val answersWithContactAddress = UserAnswers(Json.obj())
      .set(DirectorConfirmPreviousAddressId(0))(true)
      .flatMap(_.set(DirectorPreviousAddressId(0))(Address("foo", "bar", None, None, None, "GB")))
      .asOpt.value

    "`DirectorConfirmPreviousAddress` is set to `false`" must {

      val result: UserAnswers = answersWithContactAddress.set(DirectorConfirmPreviousAddressId(0))(false).asOpt.value

      "remove the data for `DirectorPreviousAddress`" in {
        result.get(DirectorPreviousAddressId(0)) mustNot be(defined)
      }
    }

    "`DirectorConfirmPreviousAddress` is set to `true` (when it is already false)" must {

      val result: UserAnswers = answersWithContactAddress.set(DirectorConfirmPreviousAddressId(0))(true).asOpt.value

      "not remove the data for `DirectorPreviousAddress`" in {
        result.get(DirectorPreviousAddressId(0)) must be(defined)
      }
    }
  }
}
