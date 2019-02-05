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

import connectors.UserAnswersCacheConnector
import identifiers.TypedIdentifier
import identifiers.register.individual.{IndividualAddressChangedId, IndividualContactAddressId}
import models.{Mode, UpdateMode}
import models.requests.DataRequest
import play.api.libs.json._
import play.api.mvc.AnyContent
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

trait Variations extends FrontendController {

  protected def dataCacheConnector: UserAnswersCacheConnector

  implicit val ec = play.api.libs.concurrent.Execution.defaultContext

  private def doSave(id: TypedIdentifier[Boolean])(implicit request: DataRequest[AnyContent]): Future[JsValue] =
    dataCacheConnector.save(request.externalId, id, true)

  def saveChangeFlag[A](mode:Mode, id: TypedIdentifier[A])(implicit request: DataRequest[AnyContent]): Future[JsValue] = {
    if (mode == UpdateMode) {
      id match {
        case IndividualContactAddressId => doSave(IndividualAddressChangedId)
        case _ => Future.successful(request.userAnswers.json)
      }
    } else {
      Future.successful(request.userAnswers.json)
    }
  }

}
