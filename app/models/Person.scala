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

package models

import controllers.register.company.directors.routes
import models.register.company.directors.DirectorDetails

import scala.language.implicitConversions

case class Person(index: Int, name: String, deleteLink: String, editLink: String) {
  def id = s"person-$index"
  def deleteLinkId = s"$id-delete"
  def editLinkId = s"$id-edit"
}

object Person {

  implicit def indexedCompanyDirectors(directors: Seq[DirectorDetails]): Seq[Person] = {
    directors.zipWithIndex.map { case (director, index) =>
      Person(
        index,
        director.fullName,
        routes.ConfirmDeleteDirectorController.onPageLoad(index).url,
        routes.DirectorDetailsController.onPageLoad(NormalMode, Index(index)).url
      )
    }
  }

}
