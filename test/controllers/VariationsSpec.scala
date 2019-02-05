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

import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.address.ManualAddressControllerSpec.externalId
import identifiers.TypedIdentifier
import models._
import models.requests.DataRequest
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class VariationsSpec extends ControllerSpecBase {
  private val psaUser = PSAUser(UserType.Individual, None, isExistingPSA = false, None)

  val testId: TypedIdentifier[Address] = new TypedIdentifier[Address] {
    override def toString = "testId"
  }

  val testChangeId: TypedIdentifier[Boolean] = new TypedIdentifier[Boolean] {
    override def toString = "testChangeId"
  }

  private val testVariations = new Variations {
    override val changeIds: Map[TypedIdentifier[_ >: ContactDetails with Address <: Product with Serializable], TypedIdentifier[Boolean]] =
      Map(testId -> testChangeId)
    override protected def cacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  }

  def dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), externalId, psaUser, UserAnswers())

  "Variations" must {
    "update the changed flag when save is called in Update Mode" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariations.saveChangeFlag(UpdateMode, testId)(dataRequest), Duration.Inf)
      FakeUserAnswersCacheConnector.verify(testChangeId, true)
    }

    "not update the changed flag when save is called in NormalMode" in {
      FakeUserAnswersCacheConnector.reset()
      Await.result(testVariations.saveChangeFlag(NormalMode, testId)(dataRequest), Duration.Inf)
      FakeUserAnswersCacheConnector.verifyNot(testChangeId)
    }

  }
}
