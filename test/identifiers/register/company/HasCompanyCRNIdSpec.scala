/*
 * Copyright 2019 HM Revenue & Customs
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

import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.{Enumerable, UserAnswers}

class HasCompanyCRNIdSpec extends WordSpec with MustMatchers with OptionValues with Enumerable.Implicits {

  "Cleanup" when {
    "`HasCompanyCRNId` is set to `false`" must {
      "remove the CompanyRegistrationNumberId" in {
        val ua = UserAnswers(Json.obj(CompanyRegistrationNumberId.toString -> ""))
        val result = HasCompanyCRNId.cleanup(Some(false), ua).asOpt.value
        result.get(CompanyRegistrationNumberId) mustBe None
      }
    }

    "`HasCompanyCRNId` is set to `true`" must {
      "NOT remove the CompanyRegistrationNumberId" in {
        val ua = UserAnswers(Json.obj(CompanyRegistrationNumberId.toString -> ""))
        val result = HasCompanyCRNId.cleanup(Some(true), ua).asOpt.value
        result.get(CompanyRegistrationNumberId) mustBe Some("")
      }
    }

  }
}
