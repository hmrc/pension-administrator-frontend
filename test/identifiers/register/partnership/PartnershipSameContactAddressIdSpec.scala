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

import models.{Address, TolerantAddress}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import utils.UserAnswers

class PartnershipSameContactAddressIdSpec extends WordSpec with OptionValues with MustMatchers {

  "Cleanup" when {

    val tolerantAddress: TolerantAddress = TolerantAddress(None, None, None, None, None, None)

    val partnershipUserAnswers = UserAnswers()
      .set(PartnershipContactAddressId)(Address("address1", "address2", None, None, None, "UK"))
      .flatMap(_.set(PartnershipContactAddressPostCodeLookupId)(Seq(tolerantAddress)))
      .asOpt.value

    "false" must {

      val result = partnershipUserAnswers
        .set(PartnershipSameContactAddressId)(false)
        .asOpt.value

      "delete 'PartnershipContactAddress'" in {

        result.get(PartnershipContactAddressId) must not be defined

      }

      "delete 'PartnershipPostcodeLookup'" in {

        result.get(PartnershipContactAddressPostCodeLookupId) must not be defined
      }
    }

    "true" must {

      val result = partnershipUserAnswers
        .set(PartnershipSameContactAddressId)(true)
        .asOpt.value

      "delete 'PartnershipContactAddress'" in {

        result.get(PartnershipContactAddressId) must not be defined

      }

      "delete 'PartnershipPostcodeLookup'" in {

        result.get(PartnershipContactAddressPostCodeLookupId) must not be defined

      }
    }
  }
}
