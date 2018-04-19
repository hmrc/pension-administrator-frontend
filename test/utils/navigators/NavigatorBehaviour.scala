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

package utils.navigators

import identifiers.Identifier
import models.{CheckMode, NormalMode}
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import org.scalatest.prop.{PropertyChecks, TableFor4}
import play.api.mvc.Call
import utils.{Navigator, UserAnswers}

trait NavigatorBehaviour extends PropertyChecks with OptionValues {
  this: WordSpec with MustMatchers =>

  def navigatorWithRoutes[A <: Identifier](navigator: Navigator, routes: TableFor4[A, UserAnswers, Call, Option[Call]]): Unit = {

    "behave like a navigator" when {

      "navigating in NormalMode" must {
        forAll(routes) { (id: Identifier, userAnswers: UserAnswers, call: Call, _: Option[Call]) =>
          s"move from $id to $call when data is $userAnswers" in {
            val result = navigator.nextPage(id, NormalMode)(userAnswers)
            result mustBe call
          }
        }
      }

      "navigating in CheckMode" must {
        forAll(routes) { (id: Identifier, userAnswers: UserAnswers, _: Call, editCall: Option[Call]) =>
          if (editCall.isDefined) {
            s"move from $id to ${editCall.value} when data is $userAnswers" in {
              val result = navigator.nextPage(id, CheckMode)(userAnswers)
              result mustBe editCall.value
            }
          }
        }
      }
    }
  }
}