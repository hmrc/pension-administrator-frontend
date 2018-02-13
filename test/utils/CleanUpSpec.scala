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

import identifiers.TypedIdentifier
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json._

class CleanupSpec extends WordSpec with MustMatchers with PropertyChecks with OptionValues {

  object TestIdentifier extends TypedIdentifier[Boolean]

  "#default" must {
    "return a `Cleanup` instance which doesn't modify anything" in {

      val instance = implicitly[Cleanup[TestIdentifier.type]]

      val jsLeafGen: Gen[JsValue] = {
        Gen.frequency(
          10 -> Gen.alphaNumStr.map(JsString),
          10 -> Gen.chooseNum(1, 9999).map(JsNumber(_)),
          3  -> Gen.oneOf(true, false).map(JsBoolean)
        )
      }

      val newValueGen: Gen[Option[Boolean]] = {
        Gen.oneOf(None, Some(true), Some(false))
      }

      forAll(jsLeafGen, newValueGen) {
        (value, newValue) =>
          instance(TestIdentifier)(newValue, UserAnswers(value)).asOpt.value mustEqual UserAnswers(value)
      }
    }
  }
}
