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
import models.register.company.directors.DirectorDetails
import play.api.libs.json.JsPath

case class DirectorDetailsId(index: Int) extends TypedIdentifier[DirectorDetails] {
  override def path: JsPath = JsPath \ "directors" \ index \ DirectorDetailsId.toString
}

object DirectorDetailsId {
  def collectionPath: JsPath = JsPath \ "directors" \\ DirectorDetailsId.toString
  override def toString: String = "directorDetails"
}
