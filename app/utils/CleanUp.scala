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
import play.api.libs.json._

trait Cleanup[I <: TypedIdentifier.PathDependent] {

  def apply(id: I)(value: Option[id.Data], answers: UserAnswers): JsResult[UserAnswers]
}

object Cleanup {

  def apply[A, I <: TypedIdentifier[A]](f: PartialFunction[(I, Option[A], UserAnswers), JsResult[UserAnswers]]): Cleanup[I] =
    new Cleanup[I] {
      override def apply(id: I)(value: Option[id.Data], answers: UserAnswers): JsResult[UserAnswers] = {
        f.lift((id, value, answers)).getOrElse(JsSuccess(answers))
      }
    }

  implicit def default[I <: TypedIdentifier.PathDependent]: Cleanup[I] =
    new Cleanup[I] {
      override def apply(id: I)(value: Option[id.Data], answers: UserAnswers): JsResult[UserAnswers] =
        JsSuccess(answers)
    }
}
