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
import identifiers.register.company._
import identifiers.register.individual._
import identifiers.register.partnership.partners.{PartnerContactDetailsId, PartnerPreviousAddressId}
import identifiers.register.partnership._
import models._
import models.requests.DataRequest
import play.api.libs.json._
import play.api.mvc.AnyContent
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.Future

trait Variations extends FrontendController {

  protected def cacheConnector: UserAnswersCacheConnector

  implicit val ec = play.api.libs.concurrent.Execution.defaultContext

  private def doSave(id: TypedIdentifier[Boolean])(implicit request: DataRequest[AnyContent]): Future[JsValue] =
    cacheConnector.save(request.externalId, id, true)

  protected val changeIds: Map[TypedIdentifier[_ >: ContactDetails with Address <: Product with Serializable], TypedIdentifier[Boolean]] = Map(
    IndividualContactAddressId -> IndividualAddressChangedId,
    IndividualPreviousAddressId -> IndividualPreviousAddressChangedId,
    IndividualContactDetailsId -> IndividualContactDetailsChangedId,
    CompanyContactAddressId -> CompanyContactAddressChangedId,
    CompanyPreviousAddressId -> CompanyPreviousAddressChangedId,
    ContactDetailsId -> CompanyContactDetailsChangedId,
    PartnershipContactAddressId -> PartnershipContactAddressChangedId,
    PartnershipPreviousAddressId -> PartnershipPreviousAddressChangedId,
    PartnershipContactDetailsId -> PartnershipContactDetailsChangedId
  )

  def saveChangeFlag[A](mode: Mode, id: TypedIdentifier[A])(implicit request: DataRequest[AnyContent]): Future[JsValue] = {
    if (mode == UpdateMode) {
      changeIds.find(_._1 == id) match {
        case Some(item) => doSave(item._2)
        case None => Future.successful(request.userAnswers.json)
      }
    } else {
      Future.successful(request.userAnswers.json)
    }
  }

}
