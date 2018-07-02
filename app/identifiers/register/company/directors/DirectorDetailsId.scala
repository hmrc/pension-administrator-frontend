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

package identifiers.register.company.directors

import identifiers._
import identifiers.register.company.MoreThanTenDirectorsId
import models.PersonDetails
import play.api.libs.json.{JsPath, JsResult, JsSuccess}
import utils.UserAnswers

case class DirectorDetailsId(index: Int) extends TypedIdentifier[PersonDetails] {
  override def path: JsPath = JsPath \ "directors" \ index \ DirectorDetailsId.toString

  override def cleanup(value: Option[PersonDetails], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.get(MoreThanTenDirectorsId) match {
      case Some(_) =>
        userAnswers.allDirectorsAfterDelete match {
          case directors if directors.length < 10 =>
            userAnswers.remove(MoreThanTenDirectorsId)
          case _ => JsSuccess(userAnswers)
        }
      case _ => JsSuccess(userAnswers)
    }
  }
}

object DirectorDetailsId {
  def collectionPath: JsPath = JsPath \ "directors" \\ DirectorDetailsId.toString
  override def toString: String = "directorDetails"
}
