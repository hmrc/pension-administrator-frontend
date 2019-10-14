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

import identifiers.TypedIdentifier
import org.scalatest.OptionValues
import org.scalatest.matchers.{MatchResult, Matcher}
import play.api.libs.json.{JsPath, JsValue, Reads}

trait MockValidationHelper extends OptionValues {

  def containJson(path: JsPath): Matcher[JsValue] = new Matcher[JsValue] {
    override def apply(left: JsValue): MatchResult =
      MatchResult(
        path.json.pick.reads(left).asOpt.isDefined,
        s"Json object did not contain any value at $path",
        s"Json object contained a value at $path"
      )
  }

  def containJson(identifier: TypedIdentifier[_]): Matcher[JsValue] =
    containJson(identifier.path)

  def containJson[A : Reads](path: JsPath, value: A): Matcher[JsValue] = new Matcher[JsValue] {
    override def apply(left: JsValue): MatchResult =
      MatchResult(
        path.json.pick.reads(left).flatMap(_.validate[A]).asOpt.value == value,
        s"Json object did not contain $value at $path",
        s"Json object contained $value at $path"
      )
  }

  def containJson[A : Reads](identifier: TypedIdentifier[A], value: A): Matcher[JsValue] =
    containJson(identifier.path, value)

}
