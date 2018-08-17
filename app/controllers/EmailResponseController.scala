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

import audit.{AuditService, EmailAuditEvent}
import com.google.inject.Inject
import controllers.model.{EmailEvents, Opened}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, BodyParsers}
import uk.gov.hmrc.crypto.{ApplicationCrypto, Crypted}
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

class EmailResponseController @Inject()(
                                         auditService: AuditService,
                                         crypto: ApplicationCrypto) extends FrontendController {


  def retrieveStatus(id: String): Action[JsValue] = Action(BodyParsers.parse.tolerantJson) {
    implicit request =>
      val decryptedPsaId = crypto.QueryParameterCrypto.decrypt(Crypted(id)).value
      request.body.validate[EmailEvents].fold(
        _ => BadRequest("Bad Request received from Email Call back event"),
        emailEvents => {
          emailEvents.events.filterNot(_.event == Opened).foreach { emailEvent =>
            auditService.sendEvent(EmailAuditEvent(decryptedPsaId, emailEvent.event))
          }
          Ok
        }
      )
  }
}
