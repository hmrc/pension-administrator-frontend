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

package controllers

import connectors.cache.UserAnswersCacheConnector
import identifiers.TypedIdentifier
import identifiers.register._
import identifiers.register.adviser.{AdviserAddressId, AdviserEmailId, AdviserPhoneId, ConfirmDeleteAdviserId}
import identifiers.register.company._
import identifiers.register.company.directors.{CheckYourAnswersId => DirectorsCheckYourAnswersId, _}
import identifiers.register.individual._
import identifiers.register.partnership._
import identifiers.register.partnership.partners.{CheckYourAnswersId => PartnersCheckYourAnswersId, _}
import models._
import models.requests.DataRequest
import play.api.libs.json._
import play.api.mvc.AnyContent
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

trait Variations extends FrontendBaseController {

  protected def cacheConnector: UserAnswersCacheConnector

  implicit val executionContext: ExecutionContext

  private val changeIds: Map[TypedIdentifier[?], TypedIdentifier[Boolean]] = Map(
    IndividualContactAddressId -> IndividualAddressChangedId,
    IndividualPreviousAddressId -> IndividualPreviousAddressChangedId,
    CompanyContactAddressId -> CompanyContactAddressChangedId,
    CompanyPreviousAddressId -> CompanyPreviousAddressChangedId,
    CompanyEmailId -> CompanyContactDetailsChangedId,
    CompanyPhoneId -> CompanyContactDetailsChangedId,
    IndividualEmailId -> IndividualContactDetailsChangedId,
    IndividualPhoneId -> IndividualContactDetailsChangedId,
    PartnershipContactAddressId -> PartnershipContactAddressChangedId,
    PartnershipPreviousAddressId -> PartnershipPreviousAddressChangedId,
    PartnershipEmailId -> PartnershipContactDetailsChangedId,
    PartnershipPhoneId -> PartnershipContactDetailsChangedId,
    VariationWorkingKnowledgeId -> DeclarationChangedId,
    AdviserAddressId -> DeclarationChangedId,
    AdviserEmailId -> DeclarationChangedId,
    AdviserPhoneId -> DeclarationChangedId,
    ConfirmDeleteAdviserId -> DeclarationChangedId,
    MoreThanTenDirectorsId -> MoreThanTenDirectorsOrPartnersChangedId,
    MoreThanTenPartnersId -> MoreThanTenDirectorsOrPartnersChangedId,
    DirectorsCheckYourAnswersId -> DirectorsOrPartnersChangedId,
    PartnersCheckYourAnswersId -> DirectorsOrPartnersChangedId
  )

  protected def findChangeIdNonIndexed[A](id: TypedIdentifier[A]): Option[TypedIdentifier[Boolean]] = {
    changeIds.find(_._1 == id) match {
      case Some(item) => Some(item._2)
      case None => None
    }
  }

  protected def findChangeIdIndexed[A](id: TypedIdentifier[A]): Option[TypedIdentifier[Boolean]] = {
    id match {
      case DirectorAddressId(_) | DirectorAddressYearsId(_) | DirectorEmailId(_) | DirectorPhoneId(_) |
           DirectorEnterNINOId(_) | DirectorPreviousAddressId(_) | DirectorEnterUTRId(_) | DirectorNameId(_)
      => Some(DirectorsOrPartnersChangedId)
      case PartnerAddressId(_) | PartnerAddressYearsId(_) | PartnerEmailId(_) | PartnerPhoneId(_) |
           PartnerEnterNINOId(_) | PartnerPreviousAddressId(_) | PartnerEnterUTRId(_) | PartnerNameId(_)
      => Some(DirectorsOrPartnersChangedId)
      case _ => None
    }
  }

  def saveChangeFlag[A](mode: Mode, id: TypedIdentifier[A])(implicit request: DataRequest[AnyContent]): Future[JsValue] = {
    val applicableMode = if (mode == UpdateMode) Some(mode) else None

    val result = applicableMode.flatMap { _ =>
      findChangeIdNonIndexed(id).fold(findChangeIdIndexed(id))(Some(_))
        .map(cacheConnector.save(_, value = true))
    }
    result.fold(doNothing)(identity)
  }

  def setNewFlag(id: TypedIdentifier[PersonName], mode: Mode, userAnswers: UserAnswers)
                (implicit request: DataRequest[?]): Future[JsValue] = {
    if (mode == UpdateMode | mode == CheckUpdateMode) {
      userAnswers.get(id).fold(doNothing) { details =>
        cacheConnector.save(id, details.copy(isNew = true))
      }
    } else {
      doNothing
    }
  }

  private def doNothing(implicit request: DataRequest[?]): Future[JsValue] = Future.successful(request.userAnswers.json)
}
