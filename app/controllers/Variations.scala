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
import identifiers.register._
import identifiers.register.adviser.{AdviserAddressId, AdviserDetailsId, ConfirmDeleteAdviserId}
import identifiers.register.company._
import identifiers.register.company.directors.{CheckYourAnswersId => DirectorsCheckYourAnswersId, _}
import identifiers.register.individual._
import identifiers.register.partnership._
import identifiers.register.partnership.partners.{CheckYourAnswersId => PartnersCheckYourAnswersId, _}
import models._
import models.requests.DataRequest
import play.api.libs.json._
import play.api.mvc.AnyContent
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import utils.UserAnswers

import scala.concurrent.{ExecutionContext, Future}

trait Variations extends FrontendController {

  protected def cacheConnector: UserAnswersCacheConnector

  implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext

  private val changeIds: Map[TypedIdentifier[_], TypedIdentifier[Boolean]] = Map(
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
    AdviserDetailsId -> DeclarationChangedId,
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
      case PartnerAddressId(_) | PartnerAddressYearsId(_) | PartnerContactDetailsId(_) |
           PartnerNinoId(_) | PartnerPreviousAddressId(_) | PartnerUniqueTaxReferenceId(_) | PartnerDetailsId(_)
      => Some(DirectorsOrPartnersChangedId)
      case _ => None
    }
  }

  def saveChangeFlag[A](mode: Mode, id: TypedIdentifier[A])(implicit request: DataRequest[AnyContent]): Future[JsValue] = {
    val applicableMode = if (mode == UpdateMode) Some(mode) else None

    val result = applicableMode.flatMap { _ =>
      findChangeIdNonIndexed(id).fold(findChangeIdIndexed(id))(Some(_))
        .map(cacheConnector.save(request.externalId, _, value = true))
    }
    result.fold(doNothing)(identity)
  }

  def setCompleteFlagForExistingDirOrPartners(mode: Mode, inputId: TypedIdentifier[Address],
                                              userAnswers: UserAnswers)(implicit request: DataRequest[AnyContent]): Future[JsValue] = {
    (mode, inputId) match {
      case (UpdateMode, DirectorPreviousAddressId(index)) =>
        setCompleteFlag(userAnswers, DirectorNameId(index), index, IsDirectorCompleteId(index))
      case (UpdateMode, PartnerPreviousAddressId(index)) =>
        setCompleteFlagPerson(userAnswers, PartnerDetailsId(index), index, IsPartnerCompleteId(index))
      case _ =>
        doNothing
    }
  }

  private def setCompleteFlagPerson(userAnswers: UserAnswers, id: TypedIdentifier[PersonDetails],
                              index: Int, completeId: TypedIdentifier[Boolean])
                             (implicit request: DataRequest[AnyContent]): Future[JsValue] = {
    userAnswers.get(id) match {
      case Some(details) if !details.isNew =>
        cacheConnector.save(request.externalId, completeId, true)
      case _ =>
        doNothing
    }
  }

  private def setCompleteFlag(userAnswers: UserAnswers, id: TypedIdentifier[PersonName],
                                    index: Int, completeId: TypedIdentifier[Boolean])
                                   (implicit request: DataRequest[AnyContent]): Future[JsValue] = {
    userAnswers.get(id) match {
      case Some(details) if !details.isNew =>
        cacheConnector.save(request.externalId, completeId, true)
      case _ =>
        doNothing
    }
  }

  def setNewFlagPerson(id: TypedIdentifier[PersonDetails], mode: Mode, userAnswers: UserAnswers)
                      (implicit request: DataRequest[_]): Future[JsValue] = {
    if (mode == UpdateMode | mode == CheckUpdateMode) {
      userAnswers.get(id).fold(doNothing) { details =>
        cacheConnector.save(request.externalId, id, details.copy(isNew = true))
      }
    } else {
      doNothing
    }
  }

  def setNewFlag(id: TypedIdentifier[PersonName], mode: Mode, userAnswers: UserAnswers)
                (implicit request: DataRequest[_]): Future[JsValue] = {
    if (mode == UpdateMode | mode == CheckUpdateMode) {
      userAnswers.get(id).fold(doNothing) { details =>
        cacheConnector.save(request.externalId, id, details.copy(isNew = true))
      }
    } else {
      doNothing
    }
  }

  private def doNothing(implicit request: DataRequest[_]): Future[JsValue] = Future.successful(request.userAnswers.json)
}
