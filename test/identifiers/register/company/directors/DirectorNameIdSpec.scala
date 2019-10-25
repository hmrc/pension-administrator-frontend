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

package identifiers.register.company.directors

import identifiers.register.company.MoreThanTenDirectorsId
import models.PersonName
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.UserAnswers

class DirectorNameIdSpec extends WordSpec with MustMatchers with OptionValues {

  val personToDelete = PersonName("John", "One")

  val userAnswersWithTenDirectors = UserAnswers(Json.obj(
    "directors" -> Json.arr(
      Json.obj(DirectorNameId.toString -> personToDelete),
      Json.obj(DirectorNameId.toString -> PersonName("John", "Two")),
      Json.obj(DirectorNameId.toString -> PersonName("John", "Three")),
      Json.obj(DirectorNameId.toString -> PersonName("John", "Four")),
      Json.obj(DirectorNameId.toString -> PersonName("John", "Five")),
      Json.obj(DirectorNameId.toString -> PersonName("John", "Six")),
      Json.obj(DirectorNameId.toString -> PersonName("John", "Seven")),
      Json.obj(DirectorNameId.toString -> PersonName("John", "Eight")),
      Json.obj(DirectorNameId.toString -> PersonName("John", "Nine")),
      Json.obj(DirectorNameId.toString -> PersonName("Tim", "Ten", isDeleted = true)),
      Json.obj(DirectorNameId.toString -> PersonName("Tim", "Eleven", isDeleted = true)),
      Json.obj(DirectorNameId.toString -> PersonName("Tim", "Twelve", isDeleted = true)),
      Json.obj(DirectorNameId.toString -> PersonName("John", "Thirteen"))
    )
  ))

  val userAnswersWithOneDirector = UserAnswers(Json.obj(
    "directors" -> Json.arr(
      Json.obj(DirectorNameId.toString -> PersonName("John", "One"))
    )
  )) .set(MoreThanTenDirectorsId)(true).asOpt.value

  "Cleanup" must {

    "remove MoreThanTenDirectorsId" when {

      "there are fewer than 10 directors" in {

        val result: UserAnswers = userAnswersWithTenDirectors
          .set(DirectorNameId(1))(personToDelete.copy(isDeleted = true)).asOpt.value

        result.get(MoreThanTenDirectorsId) must not be defined

      }

    }

  }

}