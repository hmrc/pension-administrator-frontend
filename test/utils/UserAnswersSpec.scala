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

package utils

import org.scalatest.{FlatSpec, Matchers, OptionValues}
import play.api.libs.json.{JsPath, JsResultException, Json}

class UserAnswersSpec extends FlatSpec with Matchers with OptionValues {

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

  "UserAnswers" should "get all matching recursive results" in {
    val userAnswers = UserAnswers(establishers)
    userAnswers.getAll[String](JsPath \ "establishers" \\ "name").value should contain allOf("foo", "bar")
  }

  it should "throw JsResultException if Js Value is not of correct type" in {
    val userAnswers = UserAnswers(establishers)
    intercept[JsResultException]{
      userAnswers.getAll[Boolean](JsPath \ "establishers" \\ "name")
    }
  }

  it should "return an empty list when no matches" in {
    val userAnswers = UserAnswers(establishers)
    userAnswers.getAll[String](JsPath \ "establishers" \\ "address").value.size shouldBe 0
  }
}
