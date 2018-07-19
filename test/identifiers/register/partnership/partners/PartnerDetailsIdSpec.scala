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

import java.time.LocalDate

import identifiers.register.partnership.MoreThanTenPartnersId
import models.PersonDetails
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.UserAnswers

class PartnerDetailsIdSpec extends WordSpec with OptionValues with MustMatchers {

  "Cleanup" when {

    val personDetails = PersonDetails("foo", None, "bar", LocalDate.now)

    val partnershipUserAnswers = UserAnswers(Json.obj("partners" -> Json.arr(Json.obj(PartnerDetailsId.toString -> personDetails))))
      .set(MoreThanTenPartnersId)(true)
      .asOpt.value

    "'PersonDetails' isDeleted is changed to true" must {

      "the 'MoreThanTenPartners' flag should be removed" in {

        partnershipUserAnswers
          .set(PartnerDetailsId(1))(personDetails.copy(isDeleted = true))
          .asOpt.value
          .get(MoreThanTenPartnersId) must not be defined

      }
    }

    "'PersonDetails' isDeleted remains false" must {

      "the 'MoreThanTenPartners' flag remains the same" in {

        partnershipUserAnswers
          .set(PartnerDetailsId(1))(personDetails.copy(firstName = "changedName"))
          .asOpt.value
          .get(MoreThanTenPartnersId).value mustBe true
      }
    }
  }
}
