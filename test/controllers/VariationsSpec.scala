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
import connectors.{FakeUserAnswersCacheConnector, UserAnswersCacheConnector}
import controllers.actions.FakeDataRetrievalAction
import controllers.address.ManualAddressControllerSpec.externalId
import identifiers.register.individual.{IndividualAddressChangedId, IndividualContactAddressId, IndividualDetailsCorrectId}
import models.requests.DataRequest
import models.{NormalMode, PSAUser, UpdateMode, UserType}
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import utils.UserAnswers

class VariationsSpec extends ControllerSpecBase {
  private val psaUser = PSAUser(UserType.Individual, None, isExistingPSA = false, None)

  private val testVariations = new Variations {
    override protected def cacheConnector: UserAnswersCacheConnector = FakeUserAnswersCacheConnector
  }

  def dataRequest: DataRequest[AnyContent] = DataRequest(FakeRequest(), externalId, psaUser, UserAnswers())

  "Variations" must {
    "update the changed flag when save is called in Update Mode" in {
      testVariations.saveChangeFlag(UpdateMode, IndividualContactAddressId)(dataRequest)
      .map(_ => FakeUserAnswersCacheConnector.verify(IndividualAddressChangedId, true))
    }

    "update the changed flag when save is called in NormalMode" in {
      testVariations.saveChangeFlag(NormalMode, IndividualContactAddressId)(dataRequest)
        .map(_ => FakeUserAnswersCacheConnector.verifyNot(IndividualAddressChangedId))
    }

  }
}
