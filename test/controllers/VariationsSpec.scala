/*
 * Copyright 2020 HM Revenue & Customs
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

import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.address.ManualAddressControllerSpec.externalId
import identifiers.TypedIdentifier
import identifiers.register.company.directors.{DirectorNameId, DirectorPreviousAddressId, IsDirectorCompleteId}
import models._
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import uk.gov.hmrc.play.bootstrap.tools.Stubs.stubMessagesControllerComponents
import utils.UserAnswers

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}

class VariationsSpec extends ControllerSpecBase {
  private val psaUser = PSAUser(UserType.Individual, None, isExistingPSA = false, None)

  val testId: TypedIdentifier[Address] = new TypedIdentifier[Address] {
    override def toString = "testId"
  }

  val testChangeId: TypedIdentifier[Boolean] = new TypedIdentifier[Boolean] {
    override def toString = "testChangeId"
  }

  val testPersonId: TypedIdentifier[PersonName] = new TypedIdentifier[PersonName] {
    override def toString = "personDetailsId"
  }

  val person = PersonName("John", "Doe")
  val validData = UserAnswers(Json.obj(testPersonId.toString -> person))

  private val testVariationsNonIndexed: Variations = new Variations {

    override def findChangeIdNonIndexed[A](id:TypedIdentifier[A]): Option[TypedIdentifier[Boolean]] = Some(testChangeId)

    override protected def cacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector

    implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.Implicits.global

    override protected def controllerComponents: MessagesControllerComponents = stubMessagesControllerComponents()
  }

  private val testVariationsIndexed: Variations = new Variations {

    override def findChangeIdNonIndexed[A](id:TypedIdentifier[A]): Option[TypedIdentifier[Boolean]] = None
    override def findChangeIdIndexed[A](id:TypedIdentifier[A]): Option[TypedIdentifier[Boolean]] = Some(testChangeId)

    override protected def cacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector

    implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.Implicits.global

    override protected def controllerComponents: MessagesControllerComponents = stubMessagesControllerComponents()
  }

  def dataRequest(userAnswers: UserAnswers = UserAnswers()): DataRequest[AnyContent] = DataRequest(FakeRequest(), externalId, psaUser, userAnswers)

  def userAnswersWithDirector(isNew: Boolean = false) = UserAnswers(Json.obj(
    "directors" -> Json.arr(
      Json.obj(DirectorNameId.toString ->
        PersonName("", "", isNew = isNew))
    )))

  "Variations" must {

    "set the complete flag for existing director or partner" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsNonIndexed.setCompleteFlagForExistingDirOrPartners(
        UpdateMode, DirectorPreviousAddressId(0), userAnswersWithDirector())(dataRequest()), Duration.Inf)
      FakeUserAnswersCacheConnector.verify(IsDirectorCompleteId(0), true)
    }

    "not set the complete flag for new director or partner" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsNonIndexed.setCompleteFlagForExistingDirOrPartners(
        UpdateMode, DirectorPreviousAddressId(0), userAnswersWithDirector(true))(dataRequest()), Duration.Inf)
      FakeUserAnswersCacheConnector.verifyNot(IsDirectorCompleteId(0))
    }

    "not set the complete flag for NormalMode" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsNonIndexed.setCompleteFlagForExistingDirOrPartners(
        NormalMode, DirectorPreviousAddressId(0), userAnswersWithDirector())(dataRequest()), Duration.Inf)
      FakeUserAnswersCacheConnector.verifyNot(IsDirectorCompleteId(0))
    }

    "update the changed flag when save is called in Update Mode" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsNonIndexed.saveChangeFlag(UpdateMode, testId)(dataRequest()), Duration.Inf)
      FakeUserAnswersCacheConnector.verify(testChangeId, true)
    }

    "not update the changed flag when save is called in NormalMode" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsNonIndexed.saveChangeFlag(NormalMode, testId)(dataRequest()), Duration.Inf)
      FakeUserAnswersCacheConnector.verifyNot(testChangeId)
    }

    "not update the changed flag when save is called in CheckMode" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsNonIndexed.saveChangeFlag(CheckMode, testId)(dataRequest()), Duration.Inf)
      FakeUserAnswersCacheConnector.verifyNot(testChangeId)
    }

    "update the changed flag when save is called in Update Mode with indexed identifier" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsIndexed.saveChangeFlag(UpdateMode, testId)(dataRequest()), Duration.Inf)
      FakeUserAnswersCacheConnector.verify(testChangeId, true)
    }

    "not update the changed flag when save is called in NormalMode  with indexed identifier" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsIndexed.saveChangeFlag(NormalMode, testId)(dataRequest()), Duration.Inf)
      FakeUserAnswersCacheConnector.verifyNot(testChangeId)
    }

    "not update the changed flag when save is called in CheckMode  with indexed identifier" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsIndexed.saveChangeFlag(CheckMode, testId)(dataRequest()), Duration.Inf)
      FakeUserAnswersCacheConnector.verifyNot(testChangeId)
    }

    "set the new flag when setNewFalg is called in Update Mode with indexed identifier" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsIndexed.setNewFlag(testPersonId, UpdateMode, validData)(dataRequest(validData)), Duration.Inf)
      FakeUserAnswersCacheConnector.verify(testPersonId, person.copy(isNew=true))
    }
  }
}
