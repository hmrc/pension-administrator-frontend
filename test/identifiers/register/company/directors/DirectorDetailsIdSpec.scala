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

import java.time.LocalDate

import identifiers.register.company.MoreThanTenDirectorsId
import models.PersonDetails
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.Json
import utils.UserAnswers

class DirectorDetailsIdSpec extends WordSpec with MustMatchers with OptionValues {

  val personToDelete = PersonDetails("John", None, "One", LocalDate.now())

  val userAnswersWithTenDirectors = UserAnswers(Json.obj(
    "directors" -> Json.arr(
      Json.obj(DirectorDetailsId.toString -> personToDelete),
      Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Two", LocalDate.now())),
      Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Three", LocalDate.now())),
      Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Four", LocalDate.now())),
      Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Five", LocalDate.now())),
      Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Six", LocalDate.now())),
      Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Seven", LocalDate.now())),
      Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Eight", LocalDate.now())),
      Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Nine", LocalDate.now())),
      Json.obj(DirectorDetailsId.toString -> PersonDetails("Tim", None, "Ten", LocalDate.now(), isDeleted = true)),
      Json.obj(DirectorDetailsId.toString -> PersonDetails("Tim", None, "Eleven", LocalDate.now(), isDeleted = true)),
      Json.obj(DirectorDetailsId.toString -> PersonDetails("Tim", None, "Twelve", LocalDate.now(), isDeleted = true)),
      Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "Thirteen", LocalDate.now()))
    )
  ))

  val userAnswersWithOneDirector = UserAnswers(Json.obj(
    "directors" -> Json.arr(
      Json.obj(DirectorDetailsId.toString -> PersonDetails("John", None, "One", LocalDate.now()))
    )
  )) .set(MoreThanTenDirectorsId)(true).asOpt.value

  "Cleanup" must {

    "remove MoreThanTenDirectorsId" when {

      "there are fewer than 10 directors" in {

        val result: UserAnswers = userAnswersWithTenDirectors
          .set(DirectorDetailsId(1))(personToDelete.copy(isDeleted = true)).asOpt.value

        result.get(MoreThanTenDirectorsId) must not be defined

      }

    }

  }

}
