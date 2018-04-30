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

package controllers

import java.time.LocalDate

import base.SpecBase
import controllers.actions.FakeDataRetrievalAction
import identifiers.register.company.BusinessDetailsId
import identifiers.register.company.directors.DirectorDetailsId
import identifiers.register.individual.IndividualDetailsId
import models.TolerantIndividual
import models.register.company.BusinessDetails
import models.register.company.directors.DirectorDetails
import play.api.libs.json.Json

trait ControllerSpecBase extends SpecBase {

  val cacheMapId = "id"

  def getEmptyData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))

  def dontGetAnyData: FakeDataRetrievalAction = new FakeDataRetrievalAction(None)

  def getDirector: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      BusinessDetailsId.toString ->
        BusinessDetails("Test Company Name", "Test UTR"),
      "directors" -> Json.arr(
        Json.obj(
          DirectorDetailsId.toString -> DirectorDetails("test first name", Some("test middle name"), "test last name", LocalDate.now())
        )
      )
    )))

  def getIndividual: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      IndividualDetailsId.toString ->
        TolerantIndividual(Some("TestFirstName"), None, Some("TestLastName"))
    )))

}
