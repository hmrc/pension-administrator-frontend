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

import identifiers.TypedIdentifier
import identifiers.register.company.MoreThanTenDirectorsId
import models.register.company.directors.DirectorDetails
import play.api.libs.json.{JsPath, JsResult, JsSuccess}
import utils.UserAnswers

case class DirectorId(index: Int) extends TypedIdentifier[Nothing] {

  override def path: JsPath = JsPath \ DirectorId.toString \ index

  override def cleanup(value: Option[Nothing], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.get(MoreThanTenDirectorsId) match {
      case Some(_) =>
        userAnswers.getAll(DirectorDetailsId.collectionPath)(DirectorDetails.format) match {
          case Some(directors) if directors.filterNot(_.isDeleted).length < 10 =>
            userAnswers.remove(MoreThanTenDirectorsId)
          case _ => JsSuccess(userAnswers)
        }
      case _ => JsSuccess(userAnswers)
    }
  }


}

object DirectorId {
  override def toString: String = "directors"
}
