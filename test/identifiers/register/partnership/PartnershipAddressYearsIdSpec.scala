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

package identifiers.register.partnership

import models.AddressYears.{OverAYear, UnderAYear}
import models.{Address, TolerantAddress}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import utils.UserAnswers

class PartnershipAddressYearsIdSpec extends WordSpec with OptionValues with MustMatchers {

  "Cleanup" when {

    val address: Address = Address("foo", "bar", None, None, None, "uk")
    val tolerantAddress: TolerantAddress = TolerantAddress(None, None, None, None, None, None)
    val partnershipUserAnswers = UserAnswers()
      .set(PartnershipAddressYearsId)(UnderAYear)
      .flatMap(_.set(PartnershipPreviousAddressId)(address))
      .flatMap(_.set(PartnershipPreviousAddressPostCodeLookupId)(Seq(tolerantAddress)))
      .asOpt.value

    "`AddressYears` is set to `OverAYear`" must {

      val result = partnershipUserAnswers.set(PartnershipAddressYearsId)(OverAYear).asOpt.value


      "remove the data from postcode lookup" in {

        result.get(PartnershipPreviousAddressPostCodeLookupId) must not be defined

      }

      "remove the data from address" in {

        result.get(PartnershipPreviousAddressId) must not be defined

      }
    }

    "`AddressYears` is set to `UnderAYear`" must {

      val result = partnershipUserAnswers.set(PartnershipAddressYearsId)(UnderAYear).asOpt.value

      "not remove the data from postcode lookup" in {

        result.get(PartnershipPreviousAddressPostCodeLookupId) must be(defined)

      }

      "not remove the data from address" in {

        result.get(PartnershipPreviousAddressId) must be(defined)

      }
    }

    "'AddressYears' is removed" must {

      val result = partnershipUserAnswers.remove(PartnershipAddressYearsId).asOpt.value

      "not remove the data from postcode lookup" in {

        result.get(PartnershipPreviousAddressPostCodeLookupId) must be(defined)

      }

      "not remove the data from address" in {

        result.get(PartnershipPreviousAddressId) must be(defined)

      }
    }
  }
}
