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

import models.register.company.CompanyDirector
import scala.language.implicitConversions

case class Person(name: String, deleteLink: String, editLink: String)

object Person {

  def id(index: Int) = s"person-$index"
  def deleteLinkId(index: Int) = s"${id(index)}-delete"
  def editLinkId(index: Int) = s"${id(index)}-edit"

  implicit def indexedCompanyDirectors(directors: Seq[CompanyDirector]): Seq[(Int, Person)] = {
    directors.indices.zip(directors.map { director =>
      Person(
        director.fullName,
        controllers.register.company.routes.ConfirmDeleteDirectorController.onPageLoad().url,
        controllers.routes.IndexController.onPageLoad().url
      )
    })
  }

}
