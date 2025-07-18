/*
 * Copyright 2024 HM Revenue & Customs
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

import models.UserType.UserType
import models.requests.OptionalDataRequest

import scala.concurrent.ExecutionContext

case class PSAStartEvent(externalId: String, userType: UserType, existingUser: Boolean) extends AuditEvent {

  override def auditType: String = "PSAStart"

  override def details: Map[String, String] =
    Map(
      "externalId" -> externalId,
      "userType" -> userType.toString,
      "existingUser" -> existingUser.toString
    )

}

object PSAStartEvent {

  def sendEvent(auditService: AuditService)(implicit request: OptionalDataRequest[?], executionContext: ExecutionContext): Unit = {

    val event = PSAStartEvent(request.externalId, request.user.userType, request.user.isExistingPSA)

    auditService.sendEvent(event)

  }

}
