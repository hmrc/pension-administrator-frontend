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
import identifiers.register.adviser.{AdviserAddressId, AdviserEmailId, AdviserNameId, AdviserPhoneId}
import identifiers.register.company._
import identifiers.register.company.directors._
import identifiers.register.individual._
import identifiers.register.partnership._
import identifiers.register.partnership.partners._
import models.RegistrationLegalStatus.{Individual, LimitedCompany, Partnership}
import models._
import models.register.DeclarationWorkingKnowledge
import play.api.libs.json.Reads
import utils.UserAnswers

class DataCompletion {

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

  def isAddressComplete(userAnswers: UserAnswers,
                        currentAddressId: TypedIdentifier[Address],
                        previousAddressId: TypedIdentifier[Address],
                        timeAtAddress: TypedIdentifier[AddressYears],
                        tradingTime: Option[TypedIdentifier[Boolean]],
                        confirmPreviousAddress: TypedIdentifier[Boolean]
                       ): Option[Boolean] = {
    (userAnswers.get(currentAddressId), userAnswers.get(timeAtAddress), userAnswers.get(confirmPreviousAddress)) match {
      case (None, _, _) => None
      case (Some(_), _, Some(_)) =>
        userAnswers.get(previousAddressId) match {
          case Some(_) => Some(true)
          case _ => Some(false)
        }
      case (Some(_), Some(AddressYears.OverAYear), _) =>
        Some(true)
      case (Some(_), Some(AddressYears.UnderAYear), _) =>
        isAddressCompleteUnderAYear(userAnswers, previousAddressId, tradingTime)
      case _ =>
        Some(false)
    }
  }

  private def isAddressCompleteUnderAYear(userAnswers: UserAnswers,
                                          previousAddressId: TypedIdentifier[Address],
                                          tradingTime: Option[TypedIdentifier[Boolean]]): Option[Boolean] = {
    (userAnswers.get(previousAddressId), tradingTime) match {
      case (Some(_), Some(tradingTimeId)) if userAnswers.get(tradingTimeId).contains(true) =>
        Some(true)
      case (Some(_), None) =>
        Some(true)
      case (_, Some(tradingTimeId)) if userAnswers.get(tradingTimeId).contains(false) =>
        Some(true)
      case _ =>
        Some(false)
    }
  }

  def isAnswerComplete[A](userAnswers: UserAnswers,
                          yesNoQuestionId: TypedIdentifier[Boolean],
                          yesValueId: TypedIdentifier[A],
                          noReasonIdOpt: Option[TypedIdentifier[String]])(implicit reads: Reads[A]): Option[Boolean] =
    (userAnswers.get(yesNoQuestionId), userAnswers.get(yesValueId), noReasonIdOpt) match {
      case (None, None, _) => None
      case (_, Some(_), _) => Some(true)
      case (_, _, Some(noReasonId)) if userAnswers.get(noReasonId).isDefined => Some(true)
      case (Some(false), _, None) => Some(true)
      case (Some(false), _, Some(noReasonId)) if userAnswers.get(noReasonId).isEmpty => Some(false)
      case _ => Some(false)
    }

  def isAnswerComplete[A](userAnswers: UserAnswers, id: TypedIdentifier[A])(implicit rds: Reads[A]): Option[Boolean] = {
    userAnswers.get(id) match {
      case None => None
      case Some(_) => Some(true)
    }
  }

  def isContactDetailsComplete(userAnswers: UserAnswers, emailId: TypedIdentifier[String],
                               phoneId: TypedIdentifier[String]): Option[Boolean] =
    (userAnswers.get(emailId), userAnswers.get(phoneId)) match {
      case (Some(_), Some(_)) => Some(true)
      case (None, None) => None
      case _ => Some(false)
    }

  def isCompanyComplete(ua: UserAnswers, mode: Mode): Boolean = {
    val allDirectorsCompleted = ua.allDirectorsAfterDelete(mode).nonEmpty &
      ua.allDirectorsAfterDelete(mode).forall(_.isComplete)
    isCompanyDetailsComplete(ua) && allDirectorsCompleted
  }

  def isPartnershipComplete(ua: UserAnswers, mode: Mode): Boolean = {
    val allPartnersCompleted = ua.allPartnersAfterDelete(mode).nonEmpty &
      ua.allPartnersAfterDelete(mode).forall(_.isComplete)
    isPartnershipDetailsComplete(ua) && allPartnersCompleted
  }

  def isCompanyDetailsComplete(ua: UserAnswers): Boolean = {
    val allAnswers = Seq(
      isAnswerComplete(ua, RegistrationInfoId),
      isAnswerComplete(ua, BusinessNameId),
      isAddressComplete(ua, CompanyContactAddressId, CompanyPreviousAddressId, CompanyAddressYearsId,
        Some(CompanyTradingOverAYearId), CompanyConfirmPreviousAddressId),
      isContactDetailsComplete(ua, CompanyEmailId, CompanyPhoneId)
    ) ++ (
      if (ua.get(AreYouInUKId).contains(true)) {
        Seq(
          isAnswerComplete(ua, BusinessUTRId),
          isAnswerComplete(ua, HasCompanyCRNId, CompanyRegistrationNumberId, None),
          isAnswerComplete(ua, HasPAYEId, EnterPAYEId, None),
          isAnswerComplete(ua, HasVATId, EnterVATId, None)
        )
      } else {
        Seq(
          isAnswerComplete(ua, CompanyAddressId)
        )
      }
      )
    isComplete(allAnswers).getOrElse(false)
  }

  def isPartnershipDetailsComplete(ua: UserAnswers): Boolean = {
    val allAnswers = Seq(
      isAnswerComplete(ua, RegistrationInfoId),
      isAnswerComplete(ua, BusinessNameId),
      isAddressComplete(ua, PartnershipContactAddressId, PartnershipPreviousAddressId, PartnershipAddressYearsId,
        Some(PartnershipTradingOverAYearId), PartnershipConfirmPreviousAddressId),
      isContactDetailsComplete(ua, PartnershipEmailId, PartnershipPhoneId)
    ) ++ (
      if (ua.get(AreYouInUKId).contains(true)) {
        Seq(
          isAnswerComplete(ua, BusinessUTRId),
          isAnswerComplete(ua, HasPAYEId, EnterPAYEId, None),
          isAnswerComplete(ua, HasVATId, EnterVATId, None)
        )
      } else {
        Seq(
          isAnswerComplete(ua, PartnershipRegisteredAddressId)
        )
      }
      )
    isComplete(allAnswers).getOrElse(false)
  }

  def isDirectorComplete(ua: UserAnswers, index: Int): Boolean = {
    isComplete(
      Seq(
        isAnswerComplete(ua, DirectorNameId(index)),
        isAnswerComplete(ua, DirectorDOBId(index)),
        isAnswerComplete(ua, HasDirectorNINOId(index), DirectorEnterNINOId(index), Some(DirectorNoNINOReasonId(index))),
        isAnswerComplete(ua, HasDirectorUTRId(index), DirectorEnterUTRId(index), Some(DirectorNoUTRReasonId(index))),
        isAddressComplete(ua, DirectorAddressId(index), DirectorPreviousAddressId(index),
          DirectorAddressYearsId(index), None, DirectorConfirmPreviousAddressId(index)),
        isAnswerComplete(ua, DirectorEmailId(index)),
        isAnswerComplete(ua, DirectorPhoneId(index))
      )
    ).getOrElse(false)
  }

  def isPartnerComplete(ua: UserAnswers, index: Int): Boolean = {
    isComplete(
      Seq(
        isAnswerComplete(ua, PartnerNameId(index)),
        isAnswerComplete(ua, PartnerDOBId(index)),
        isAnswerComplete(ua, HasPartnerNINOId(index), PartnerEnterNINOId(index), Some(PartnerNoNINOReasonId(index))),
        isAnswerComplete(ua, HasPartnerUTRId(index), PartnerEnterUTRId(index), Some(PartnerNoUTRReasonId(index))),
        isAddressComplete(ua, PartnerAddressId(index), PartnerPreviousAddressId(index),
          PartnerAddressYearsId(index), None, PartnerConfirmPreviousAddressId(index)),
        isAnswerComplete(ua, PartnerEmailId(index)),
        isAnswerComplete(ua, PartnerPhoneId(index))
      )
    ).getOrElse(false)
  }

  def isIndividualComplete(ua: UserAnswers, mode: Mode): Boolean = {
    isComplete(
      Seq(
        isAnswerComplete(ua, RegistrationInfoId),
        isAnswerComplete(ua, IndividualDetailsId),
        isAnswerComplete(ua, IndividualDateOfBirthId),
        isAddressComplete(ua, IndividualContactAddressId, IndividualPreviousAddressId, IndividualAddressYearsId, None, IndividualConfirmPreviousAddressId),
        isAnswerComplete(ua, IndividualEmailId),
        isAnswerComplete(ua, IndividualPhoneId)
      ) ++ (
        if (mode == NormalMode) {
          Seq(isAnswerComplete(ua, IndividualAddressId),
            isAnswerComplete(ua, IndividualSameContactAddressId))
        } else {
          Nil
        })
    ).getOrElse(false)
  }

  def isAdviserComplete(ua: UserAnswers, mode: Mode): Boolean = {
    val isWkYes = if (mode == NormalMode) {
      ua.get(DeclarationWorkingKnowledgeId).contains(DeclarationWorkingKnowledge.WorkingKnowledge)
    } else {
      ua.get(VariationWorkingKnowledgeId).contains(true)
    }
    if (isWkYes) {
      true
    } else {
      ua.get(AdviserEmailId).nonEmpty && ua.get(AdviserPhoneId).nonEmpty &&
        ua.get(AdviserNameId).nonEmpty && ua.get(AdviserAddressId).nonEmpty
    }
  }

  def isPsaUpdateDetailsInComplete(ua: UserAnswers): Boolean = {
    def incompleteDetails: Boolean =
      ua.get(RegistrationInfoId).map(_.legalStatus) match {
        case Some(Individual) =>
          !isIndividualComplete(ua, UpdateMode)
        case Some(LimitedCompany) =>
          ua.allDirectorsAfterDelete(UpdateMode).exists(!_.isComplete) |
            !isCompanyDetailsComplete(ua)
        case Some(Partnership) =>
          !isPartnershipDetailsComplete(ua) |
            ua.allPartnersAfterDelete(UpdateMode).exists(!_.isComplete)
        case _ =>
          true
      }
    !isAdviserComplete(ua, UpdateMode) | incompleteDetails
  }
}

object DataCompletion extends DataCompletion
