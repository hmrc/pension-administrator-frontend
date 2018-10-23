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

package identifiers.register

import identifiers.register.company.{BusinessDetailsId, CompanyAddressId}
import models.{BusinessDetails, TolerantAddress}
import models.register.{BusinessType, NonUKBusinessType}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class AreYouInUKIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {
  "Cleanup" when {

    val answersForYes = UserAnswers(Json.obj())
      .set(AreYouInUKId)(true)
      .flatMap(_.set(BusinessDetailsId)(BusinessDetails("test company", Some("utr")))
        .flatMap(_.set(BusinessTypeId)(BusinessType.LimitedCompany)))
      .asOpt.value

    val answersForNo = UserAnswers(Json.obj())
      .set(AreYouInUKId)(false)
      .flatMap(_.set(BusinessDetailsId)(BusinessDetails("test company", None))
        .flatMap(_.set(CompanyAddressId)(TolerantAddress(Some("line 1"),Some("line 2"), Some("line 3"), Some("line 4"), None, Some("DE"))))
        .flatMap(_.set(NonUKBusinessTypeId)(NonUKBusinessType.Company)))
      .asOpt.value


    "where are you in uk has already answered as Yes and we change to No " must {
      val result: UserAnswers =
        answersForYes.set(AreYouInUKId)(false)
          .asOpt.value

      "remove the data for Business details " in {
        result.get(BusinessDetailsId) mustNot be(defined)
      }

      "remove the data for Business type " in {
        result.get(BusinessTypeId) mustNot be(defined)
      }
    }


    "where are you in uk has already answered as No and we change to Yes " must {
      val result: UserAnswers =
        answersForNo.set(AreYouInUKId)(true)
          .asOpt.value

      "remove the data for Business details " in {
        result.get(BusinessDetailsId) mustNot be(defined)
      }

      "remove the data for non uk Business type " in {
        result.get(NonUKBusinessTypeId) mustNot be(defined)
      }

      "remove the data for non uk company address " in {
        result.get(CompanyAddressId) mustNot be(defined)
      }
    }
  }
}
