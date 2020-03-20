/*
 * Copyright 2020 HM Revenue & Customs
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

package utils.dataCompletion

import identifiers._
import identifiers.register._
import identifiers.register.company._
import identifiers.register.company.directors._
import identifiers.register.individual._
import identifiers.register.partnership._
import identifiers.register.partnership.partners._
import models._
import models.register.BusinessType
import play.api.libs.json.Reads
import utils.UserAnswers
import viewmodels.Message

trait DataCompletion {

  self: UserAnswers =>

  def lastPath[A](id: TypedIdentifier[A]): String = id.path.path.last.toString.replace("/", "")

  //GENERIC METHODS
  def isComplete(list: Seq[Option[Boolean]]): Option[Boolean] =
    if (list.flatten.isEmpty) {
      None
    }
    else {
      Some(list.foldLeft(true)({
        case (acc, Some(true)) => acc
        case (_, Some(false)) => false
        case (_, None) => false
      }))
    }

  def isListComplete(list: Seq[Boolean]): Boolean =
    list.nonEmpty & list.foldLeft(true)({
      case (acc, true) => acc
      case (_, false) => false
    })

  def isAddressComplete(currentAddressId: TypedIdentifier[Address],
                        previousAddressId: TypedIdentifier[Address],
                        timeAtAddress: TypedIdentifier[AddressYears],
                        tradingTime: Option[TypedIdentifier[Boolean]],
                        confirmPreviousAddress: TypedIdentifier[Boolean]
                       ): Option[Boolean] = {
    (get(currentAddressId), get(timeAtAddress), get(confirmPreviousAddress)) match {
      case (Some(_), _, Some(true)) =>
        Some(true)
      case (Some(_), _, Some(false)) =>
        get(previousAddressId) match {
          case Some(_) => Some(true)
          case _ => Some(false)
        }
      case (Some(_), Some(AddressYears.OverAYear), _) =>
        Some(true)
      case (None, _,_) =>
        Some(false)
      case (Some(_), Some(AddressYears.UnderAYear), _) =>
        isAddressCompleteUnderAYear(previousAddressId, tradingTime)
      case _ =>
        Some(false)
    }
  }

  private def isAddressCompleteUnderAYear(previousAddressId: TypedIdentifier[Address],
                                          tradingTime: Option[TypedIdentifier[Boolean]]): Option[Boolean] = {
    (get(previousAddressId), tradingTime) match {
      case (Some(_), Some(tradingTimeId)) if get(tradingTimeId).isEmpty | get(tradingTimeId).contains(true) =>
        Some(true)
      case (Some(_), None) =>
        Some(true)
      case (_, Some(tradingTimeId)) if get(tradingTimeId).contains(false) =>
        Some(true)
      case _ =>
        Some(false)
    }
  }

  def isAddressComplete(currentAddressId: TypedIdentifier[Address],
                        previousAddressId: TypedIdentifier[Address],
                        confirmPreviousAddressId: TypedIdentifier[Boolean]): Option[Boolean] = {
    (get(currentAddressId), get(confirmPreviousAddressId)) match {
      case (Some(_), Some(true)) => Some(true)
      case (Some(_), Some(false)) =>
        get(previousAddressId) match {
          case Some(_) => Some(true)
          case _ => Some(false)
        }
      case _ => Some(false)
    }
  }

  def isAnswerComplete[A](yesNoQuestionId: TypedIdentifier[Boolean],
                          yesValueId: TypedIdentifier[A],
                          noReasonIdOpt: Option[TypedIdentifier[String]])(implicit reads: Reads[A]): Option[Boolean] =
    (get(yesNoQuestionId), get(yesValueId), noReasonIdOpt) match {
      case (None, None, _) => Some(false)
      case (_, Some(_), _) => Some(true)
      case (_, _, Some(noReasonId)) if get(noReasonId).isDefined => Some(true)
      case (Some(false), _, None) => Some(true)
      case (Some(false), _, Some(noReasonId)) if get(noReasonId).isEmpty => Some(false)
      case _ => Some(false)
    }

  def isAnswerComplete[A](id: TypedIdentifier[A])(implicit rds: Reads[A]): Option[Boolean] = {
    get(id) match {
      case None => Some(false)
      case Some(_) => Some(true)
    }
  }

  def isCompanyComplete(mode: Mode): Boolean = {
    val allDirectorsCompleted = allDirectorsAfterDelete(mode).nonEmpty & allDirectorsAfterDelete(mode).forall(_.isComplete)
    isCompanyDetailsComplete(mode) && allDirectorsCompleted
  }

  def isPartnershipComplete(mode: Mode): Boolean = {
    val allPartnersCompleted = allPartnersAfterDelete(mode).nonEmpty & allPartnersAfterDelete(mode).forall(_.isComplete)
    isPartnershipDetailsComplete && allPartnersCompleted
  }

  def isCompanyDetailsComplete(mode: Mode): Boolean = {
    get(AreYouInUKId) match {
      case Some(true) =>
        isComplete(
          Seq(
            isAnswerComplete(BusinessNameId),
            isAnswerComplete(BusinessUTRId),
            if (get(BusinessTypeId).contains(BusinessType.UnlimitedCompany)) {
              isAnswerComplete(HasCompanyCRNId, CompanyRegistrationNumberId, None)
            } else {
              isAnswerComplete(CompanyRegistrationNumberId)
            },
            isAnswerComplete(HasPAYEId, EnterPAYEId, None),
            isAnswerComplete(HasVATId, EnterVATId, None),
              isAddressComplete(CompanyContactAddressId, CompanyPreviousAddressId, CompanyAddressYearsId,
                Some(CompanyTradingOverAYearId), CompanyConfirmPreviousAddressId),
            isAnswerComplete(CompanyEmailId),
            isAnswerComplete(CompanyPhoneId)
          )
        ).getOrElse(false)
      case _ =>
        isComplete(
          Seq(
            isAnswerComplete(BusinessNameId),
            isAnswerComplete(CompanyAddressId),
            isAddressComplete(CompanyContactAddressId, CompanyPreviousAddressId, CompanyAddressYearsId,
              Some(CompanyTradingOverAYearId), CompanyConfirmPreviousAddressId),
            isAnswerComplete(CompanyEmailId),
            isAnswerComplete(CompanyPhoneId)
          )
        ).getOrElse(false)
    }
  }

  def isPartnershipDetailsComplete: Boolean = {
    get(AreYouInUKId) match {
      case Some(true) =>
        isComplete(
          Seq(
            isAnswerComplete(BusinessNameId),
            isAnswerComplete(BusinessUTRId),
            isAnswerComplete(HasPAYEId, EnterPAYEId, None),
            isAnswerComplete(HasVATId, EnterVATId, None),
            isAddressComplete(PartnershipContactAddressId, PartnershipPreviousAddressId, PartnershipAddressYearsId,
              Some(PartnershipTradingOverAYearId), PartnershipConfirmPreviousAddressId),
            isAnswerComplete(PartnershipEmailId),
            isAnswerComplete(PartnershipPhoneId)
          )
        ).getOrElse(false)
      case _ =>
        isComplete(
          Seq(
            isAnswerComplete(BusinessNameId),
            isAnswerComplete(PartnershipRegisteredAddressId),
            isAddressComplete(PartnershipContactAddressId, PartnershipPreviousAddressId,
              PartnershipAddressYearsId, Some(PartnershipTradingOverAYearId), PartnershipConfirmPreviousAddressId),
            isAnswerComplete(PartnershipEmailId),
            isAnswerComplete(PartnershipPhoneId)
          )
        ).getOrElse(false)
    }
  }

  def isDirectorComplete(index: Int): Boolean = {
    isComplete(
      Seq(
        isAnswerComplete(DirectorNameId(index)),
        isAnswerComplete(DirectorDOBId(index)),
        isAnswerComplete(HasDirectorNINOId(index), DirectorEnterNINOId(index), Some(DirectorNoNINOReasonId(index))),
        isAnswerComplete(HasDirectorUTRId(index), DirectorEnterUTRId(index), Some(DirectorNoUTRReasonId(index))),
        isAddressComplete(DirectorAddressId(index), DirectorPreviousAddressId(index),
          DirectorAddressYearsId(index), None, DirectorConfirmPreviousAddressId(index)),
        isAnswerComplete(DirectorEmailId(index)),
        isAnswerComplete(DirectorPhoneId(index))
      )
    ).getOrElse(false)
  }

  def isPartnerComplete(index: Int): Boolean = {
    isComplete(
      Seq(
        isAnswerComplete(PartnerNameId(index)),
        isAnswerComplete(PartnerDOBId(index)),
        isAnswerComplete(HasPartnerNINOId(index), PartnerEnterNINOId(index), Some(PartnerNoNINOReasonId(index))),
        isAnswerComplete(HasPartnerUTRId(index), PartnerEnterUTRId(index), Some(PartnerNoUTRReasonId(index))),
        isAddressComplete(PartnerAddressId(index), PartnerPreviousAddressId(index), PartnerAddressYearsId(index), None, PartnerConfirmPreviousAddressId(index)),
        isAnswerComplete(PartnerEmailId(index)),
        isAnswerComplete(PartnerPhoneId(index))
      )
    ).getOrElse(false)
  }

  def isIndividualComplete(mode: Mode): Boolean = {
    isComplete(
      Seq(
        isAnswerComplete(IndividualDetailsId),
        isAnswerComplete(IndividualDateOfBirthId),
        isAddressComplete(IndividualContactAddressId, IndividualPreviousAddressId, IndividualAddressYearsId, None, IndividualConfirmPreviousAddressId),
        isAnswerComplete(IndividualEmailId),
        isAnswerComplete(IndividualPhoneId)
      ) ++ (if(mode == NormalMode) Seq(isAnswerComplete(IndividualAddressId), isAnswerComplete(IndividualSameContactAddressId)) else Nil)
    ).getOrElse(false)
  }
}
