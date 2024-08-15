/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.cache.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.address.ManualAddressControllerSpec.externalId
import identifiers.TypedIdentifier
import identifiers.register.company.directors.DirectorNameId
import models._
import models.requests.DataRequest
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, MessagesControllerComponents}
import play.api.test.FakeRequest
import utils.UserAnswers

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext}

class VariationsSpec extends ControllerSpecBase {
  private val psaUser = PSAUser(UserType.Individual, None, isExistingPSA = false, None, None, "")

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

    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    override protected def controllerComponents: MessagesControllerComponents = SpecBase.controllerComponents
  }

  private val testVariationsIndexed: Variations = new Variations {

    override def findChangeIdNonIndexed[A](id:TypedIdentifier[A]): Option[TypedIdentifier[Boolean]] = None
    override def findChangeIdIndexed[A](id:TypedIdentifier[A]): Option[TypedIdentifier[Boolean]] = Some(testChangeId)

    override protected def cacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector

    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

    override protected def controllerComponents: MessagesControllerComponents = SpecBase.controllerComponents
  }

  def dataRequest(userAnswers: UserAnswers = UserAnswers()): DataRequest[AnyContent] = DataRequest(FakeRequest(), externalId, psaUser, userAnswers)

  def userAnswersWithDirector(isNew: Boolean = false) = UserAnswers(Json.obj(
    "directors" -> Json.arr(
      Json.obj(DirectorNameId.toString ->
        PersonName("", "", isNew = isNew))
    )))

  "Variations" must {

    "update the changed flag when save is called in Update Mode" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsNonIndexed.saveChangeFlag(UpdateMode, testId)(dataRequest()), 2.seconds)
      FakeUserAnswersCacheConnector.verify(testChangeId, true)
    }

    "not update the changed flag when save is called in NormalMode" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsNonIndexed.saveChangeFlag(NormalMode, testId)(dataRequest()), 2.seconds)
      FakeUserAnswersCacheConnector.verifyNot(testChangeId)
    }

    "not update the changed flag when save is called in CheckMode" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsNonIndexed.saveChangeFlag(CheckMode, testId)(dataRequest()), 2.second)
      FakeUserAnswersCacheConnector.verifyNot(testChangeId)
    }

    "update the changed flag when save is called in Update Mode with indexed identifier" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsIndexed.saveChangeFlag(UpdateMode, testId)(dataRequest()), 2.seconds)
      FakeUserAnswersCacheConnector.verify(testChangeId, true)
    }

    "not update the changed flag when save is called in NormalMode  with indexed identifier" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsIndexed.saveChangeFlag(NormalMode, testId)(dataRequest()), 2.second)
      FakeUserAnswersCacheConnector.verifyNot(testChangeId)
    }

    "not update the changed flag when save is called in CheckMode  with indexed identifier" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsIndexed.saveChangeFlag(CheckMode, testId)(dataRequest()), 2.second)
      FakeUserAnswersCacheConnector.verifyNot(testChangeId)
    }

    "set the new flag when setNewFalg is called in Update Mode with indexed identifier" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariationsIndexed.setNewFlag(testPersonId, UpdateMode, validData)(dataRequest(validData)), 2.second)
      FakeUserAnswersCacheConnector.verify(testPersonId, person.copy(isNew=true))
    }
  }
}
