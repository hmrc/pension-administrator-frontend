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

import audit.AuditService
import audit.testdoubles.StubSuccessfulAuditService
import controllers.model.{Delivered, EmailEvent, EmailEvents, Opened}
import org.joda.time.DateTime
import play.api.test.Helpers._
import play.api.inject.bind
import uk.gov.hmrc.crypto.{ApplicationCrypto, PlainText}
import play.api.libs.json._

class EmailResponseControllerSpec extends ControllerSpecBase {

  val fakeAuditService = new StubSuccessfulAuditService()
  val testPsaId = "A1234567"
  val emailEvents = EmailEvents(Seq(EmailEvent(Delivered, DateTime.now()), EmailEvent(Opened, DateTime.now())))

  "EmailResponseController" when {
    "called retrieveStatus" must {

      "return OK and send the email audit event" in {

        running(
          _.overrides(
            bind[AuditService].toInstance(fakeAuditService)
          )
        ){ app =>

          val id = app.injector.instanceOf[ApplicationCrypto].QueryParameterCrypto.encrypt(PlainText(testPsaId)).value
          val controller = app.injector.instanceOf[EmailResponseController]

          val result = controller.retrieveStatus(id)(fakeRequest.withBody(Json.toJson(emailEvents)))

          status(result) mustBe OK
          fakeAuditService.verifySent(audit.EmailAuditEvent(testPsaId, Delivered)) mustBe true
        }
      }

      "return BAD_REQUEST when request does not contain EmailEvents" in {
        val invalidRequest = Json.obj("name" -> "test")
        running(_.overrides(
          bind[AuditService].to(fakeAuditService)
        )) { app =>

          fakeAuditService.reset()

          val id = app.injector.instanceOf[ApplicationCrypto].QueryParameterCrypto.encrypt(PlainText(testPsaId)).value

          val controller = app.injector.instanceOf[EmailResponseController]

          val result = controller.retrieveStatus(id)(fakeRequest.withBody(invalidRequest))

          status(result) mustBe BAD_REQUEST
          fakeAuditService.verifyNothingSent mustBe true
        }
      }
    }
  }
}
