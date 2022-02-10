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

import models.TolerantAddress
import org.scalatest._
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class CompanyAddressIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "Cleanup" when {

    val answersWithCompanyAddress = UserAnswers(Json.obj())
      .set(CompanyAddressId)(TolerantAddress(Some("100"),
        Some("SuttonStreet"),
        Some("Wokingham"),
        Some("Surrey"),
        Some("NE39 1HX"),
        Some("GB")))
      .asOpt.value

    "`CompanyAddress` is not defined" must {

      val result: UserAnswers = UserAnswers(Json.obj())

      "remove the data for `CompanyAddress`" in {
        result.get(CompanyAddressId) mustNot be(defined)
      }
    }

    "`CompanyAddress` is set to an address" must {

      val result: UserAnswers = UserAnswers(Json.obj())
        .set(CompanyAddressId)(TolerantAddress(Some("100"),
          Some("SuttonStreet"),
          Some("Wokingham"),
          Some("Surrey"),
          Some("NE39 1HX"),
          Some("GB")))
        .asOpt.value

      "not remove the data for `CompanyAddressYears`" in {
        result.get(CompanyAddressId) mustBe defined
      }
    }

    "`CompanyAddress` is removed" must {

      val result: UserAnswers = answersWithCompanyAddress.remove(CompanyAddressYearsId).asOpt.value

      "not remove the data for `PreviousPostCodeLookup`" in {
        result.get(CompanyAddressId) mustBe defined
      }

      "not remove the data for `PreviousAddress`" in {
        result.get(CompanyAddressId) mustBe defined
      }
    }
  }
}
