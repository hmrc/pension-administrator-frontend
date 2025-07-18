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

import org.apache.pekko.stream.Materializer
import org.scalatest.Inside
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{ApplicationLifecycle, bind}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector._
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends AsyncFlatSpec with Matchers with Inside {

  import AuditServiceSpec._

  "AuditServiceImpl" should "construct and send the correct event" in {

    implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest()

    val event = TestAuditEvent("test-audit-payload")

    auditService().sendEvent(event)

    val sentEvent = FakeAuditConnector.lastSentEvent

    inside(sentEvent) {
      case DataEvent(auditSource, auditType, _, _, detail, _, _, _, _) =>
        auditSource shouldBe appName
        auditType shouldBe "TestAuditEvent"
        detail should contain("payload" -> "test-audit-payload")
    }

  }

  "AuditServiceImpl" should "construct and send the correct DeregisterEvent event" in {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest()

    val event = DeregisterEvent("user-id", "psa-id")

    auditService().sendEvent(event)

    val sentEvent = FakeAuditConnector.lastSentEvent

    inside(sentEvent) {
      case DataEvent(auditSource, auditType, _, _, detail, _, _, _, _) =>
        auditSource shouldBe appName
        auditType shouldBe "PSADeEnrolment"
        detail shouldBe Map("userId" -> "user-id", "psaId" -> "psa-id")
    }

  }

}

object AuditServiceSpec {

  private val app = new GuiceApplicationBuilder()
    .overrides(
      bind[AuditConnector].toInstance(FakeAuditConnector)
    )
    .build()

  def fakeRequest(): FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  def auditService(): AuditService = app.injector.instanceOf[AuditService]

  def appName: String = app.configuration.underlying.getString("appName")

}

//noinspection ScalaDeprecation
object FakeAuditConnector extends AuditConnector {

  private var sentEvent: DataEvent = _

  override def auditingConfig: AuditingConfig = AuditingConfig(None, false, "test audit source", false)

  override def sendEvent(event: DataEvent)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[AuditResult] = {
    sentEvent = event
    super.sendEvent(event)
  }

  def lastSentEvent: DataEvent = sentEvent

  def materializer: Materializer = ???

  def lifecycle: ApplicationLifecycle = ???

  def auditChannel: AuditChannel = ???

  def datastreamMetrics: DatastreamMetrics = ???
}

case class TestAuditEvent(payload: String) extends AuditEvent {

  override def auditType: String = "TestAuditEvent"

  override def details: Map[String, String] =
    Map(
      "payload" -> payload
    )

}
