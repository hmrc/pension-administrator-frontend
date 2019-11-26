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

package controllers

import base.SpecBase
import controllers.actions._
import identifiers.register.adviser.AdviserNameId
import identifiers.register.company.directors.DirectorNameId
import identifiers.register.individual.IndividualDetailsId
import identifiers.register.partnership.partners.PartnerNameId
import identifiers.register.{BusinessNameId, RegistrationInfoId}
import models._
import play.api.inject.bind
import play.api.inject.guice.GuiceableModule
import play.api.libs.json.Json
import utils.UserAnswers

import scala.concurrent.ExecutionContext

trait ControllerSpecBase extends SpecBase {

  implicit val executionContext: ExecutionContext = inject[ExecutionContext]

  val cacheMapId = "id"

  val firstIndex = Index(0)

  def getEmptyData: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(Json.obj()))

  def dontGetAnyData: FakeDataRetrievalAction = new FakeDataRetrievalAction(None)

  def getCompany: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      RegistrationInfoId.toString -> RegistrationInfo(
        RegistrationLegalStatus.LimitedCompany, "", noIdentifier = false, RegistrationCustomerType.UK, None, None),
      BusinessNameId.toString -> "Test Company Name")
    ))

  def getDirector: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      BusinessNameId.toString -> "Test Company Name",
      "directors" -> Json.arr(
        Json.obj(
          DirectorNameId.toString -> PersonName("test first name", "test last name")
        )
      )
    )))

  def getIndividual: FakeDataRetrievalAction = new FakeDataRetrievalAction(Some(
    Json.obj(
      RegistrationInfoId.toString -> RegistrationInfo(
        RegistrationLegalStatus.Individual, "", noIdentifier = false, RegistrationCustomerType.UK, None, None),
      IndividualDetailsId.toString ->
        TolerantIndividual(Some("TestFirstName"), None, Some("TestLastName"))
    )))

  def getPartnership: DataRetrievalAction =
    UserAnswers().businessName("Test Partnership Name").dataRetrievalAction

  def getPartner: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(
      Json.obj(
        BusinessNameId.toString -> "Test Partnership Name",
        "partners" -> Json.arr(
          Json.obj(
            PartnerNameId.toString ->
              PersonName("test first name", "test last name")
          )
        )
      )
    )
  )

  def getAdviser: FakeDataRetrievalAction = new FakeDataRetrievalAction(
    Some(Json.obj(AdviserNameId.toString -> "Test Adviser Name")))

  def modules(dataRetrievalAction: DataRetrievalAction): Seq[GuiceableModule] = Seq(
    bind[AuthAction].toInstance(FakeAuthAction),
    bind[AllowAccessActionProvider].toInstance(FakeAllowAccessProvider()),
    bind[DataRetrievalAction].toInstance(dataRetrievalAction)
  )
}
