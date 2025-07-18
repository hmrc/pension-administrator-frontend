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

package audit

import audit.testdoubles.StubSuccessfulAuditService
import models.UserType.UserType
import models.requests.OptionalDataRequest
import models.{PSAUser, UserType}
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.test.FakeRequest

import scala.concurrent.Future

class PSAStartEventSpec extends AsyncFlatSpec with Matchers {

  private val externalId = "test-external-id"

  private def testRequest(userType: UserType, isExistingPsa: Boolean): OptionalDataRequest[?] = {
    OptionalDataRequest(
      request = FakeRequest("", ""),
      externalId = externalId,
      user = PSAUser(userType, None, isExistingPsa, None, None, ""),
      userAnswers = None
    )
  }

  "PSAStartEvent" should "send a PSAStart event for invdividuals" in {

    val auditService = new StubSuccessfulAuditService()
    implicit val request: OptionalDataRequest[?] = testRequest(UserType.Individual, false)

    PSAStartEvent.sendEvent(auditService)

    Future {
      auditService.verifySent(
        PSAStartEvent(
          externalId,
          UserType.Individual,
          false
        )
      ) shouldBe true
    }

  }

  it should "send a PSAStart event for organisations" in {

    val auditService = new StubSuccessfulAuditService()
    implicit val request: OptionalDataRequest[?] = testRequest(UserType.Organisation, false)

    PSAStartEvent.sendEvent(auditService)

    val psaStartEvent = PSAStartEvent(
      externalId,
      UserType.Organisation,
      existingUser = false
    )

    psaStartEvent.auditType shouldBe "PSAStart"
    psaStartEvent.details shouldBe
      Map(
        "externalId" -> externalId,
        "userType" -> UserType.Organisation.toString,
        "existingUser" -> "false"
      )

    Future {
      auditService.verifySent(
        psaStartEvent
      ) shouldBe true
    }

  }

  it should "send a PSAStart event for an existing user" in {

    val auditService = new StubSuccessfulAuditService()
    implicit val request: OptionalDataRequest[?] = testRequest(UserType.Individual, true)

    PSAStartEvent.sendEvent(auditService)

    Future {
      auditService.verifySent(
        PSAStartEvent(
          externalId,
          UserType.Individual,
          true
        )
      ) shouldBe true
    }

  }

}
