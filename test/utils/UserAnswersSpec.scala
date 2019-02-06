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

package utils

import java.time.LocalDate

import controllers.register.company.directors.routes
import identifiers.register.company.directors.{DirectorDetailsId, IsDirectorCompleteId}
import identifiers.register.individual.IndividualAddressChangedId
import models.{Index, NormalMode, PersonDetails}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.{JsPath, JsResultException, Json}
import viewmodels.Person

class UserAnswersSpec extends WordSpec with MustMatchers with OptionValues {

  private val establishers = Json.obj(
    "establishers" -> Json.arr(
      Json.obj(
        "name" -> "foo"
      ),
      Json.obj(
        "name" -> "bar"
      )
    )
  )

  "getAll" should {
    "get all matching recursive results" in {
      val userAnswers = UserAnswers(establishers)
      userAnswers.getAll[String](JsPath \ "establishers" \\ "name").value must contain allOf("foo", "bar")
    }

    "throw JsResultException if Js Value is not of correct type" in {
      val userAnswers = UserAnswers(establishers)
      intercept[JsResultException] {
        userAnswers.getAll[Boolean](JsPath \ "establishers" \\ "name")
      }
    }

    "return an empty list when no matches" in {
      val userAnswers = UserAnswers(establishers)
      userAnswers.getAll[String](JsPath \ "establishers" \\ "address").value.size mustBe 0
    }
  }

  ".allDirectorsAfterDelete" must {

    "return a map of director names, edit links, delete links and isComplete flag" in {
      val userAnswers = UserAnswers()
        .set(DirectorDetailsId(0))(PersonDetails("First", None, "Last", LocalDate.now()))
        .flatMap(_.set(IsDirectorCompleteId(0))(true))
        .flatMap(_.set(IsDirectorCompleteId(1))(false))
        .flatMap(_.set(DirectorDetailsId(1))(PersonDetails("First1", None, "Last1", LocalDate.now))).get

      val directorEntities = Seq(
        Person(0, "First Last", routes.ConfirmDeleteDirectorController.onPageLoad(0).url,
          routes.CheckYourAnswersController.onPageLoad(Index(0)).url,
          isDeleted = false, isComplete = true),
        Person(1, "First1 Last1", routes.ConfirmDeleteDirectorController.onPageLoad(1).url,
          routes.DirectorDetailsController.onPageLoad(NormalMode, Index(1)).url,
          isDeleted = false, isComplete = false))

      val result = userAnswers.allDirectorsAfterDelete

      result.size mustEqual 2
      result mustBe directorEntities
    }
  }
}
