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

package identifiers.register.partnership.partners

import models.{Address, AddressYears}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class PartnerAddressYearsIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "Cleanup" when {

    val answersWithPreviousAddress = UserAnswers(Json.obj())
      .set(PartnerAddressYearsId(0))(AddressYears.UnderAYear)
      .flatMap(_.set(PartnerPreviousAddressPostCodeLookupId(0))(Seq.empty))
      .flatMap(_.set(PartnerPreviousAddressId(0))(Address("foo", "bar", None, None, None, "GB")))
      .asOpt.value

    "`AddressYears` is set to `OverAYear`" must {

      val result: UserAnswers = answersWithPreviousAddress.set(PartnerAddressYearsId(0))(AddressYears.OverAYear).asOpt.value

      "remove the data for `PreviousPostCodeLookup`" in {
        result.get(PartnerPreviousAddressPostCodeLookupId(0)) mustNot be(defined)
      }

      "remove the data for `PreviousAddress`" in {
        result.get(PartnerPreviousAddressId(0)) mustNot be(defined)
      }
    }

    "`AddressYears` is set to `UnderAYear`" must {

      val result: UserAnswers = answersWithPreviousAddress.set(PartnerAddressYearsId(0))(AddressYears.UnderAYear).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(PartnerPreviousAddressPostCodeLookupId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(PartnerPreviousAddressId(0)) mustBe defined
      }
    }

    "`AddressYears` is removed" must {

      val result: UserAnswers = answersWithPreviousAddress.remove(PartnerAddressYearsId(0)).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(PartnerPreviousAddressPostCodeLookupId(0)) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(PartnerPreviousAddressId(0)) mustBe defined
      }
    }
  }

}
