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

  def isCompleteNew(list: Seq[Either[Message, Boolean]]): Seq[Message] = {
    list.flatMap {
      case Right(_) => List.empty
      case Left(x) => List(x)
    }
  }

  def isListComplete(list: Seq[Boolean]): Boolean =
    list.nonEmpty & list.foldLeft(true)({
      case (acc, true) => acc
      case (_, false) => false
    })

  def isAddressComplete(currentAddressId: TypedIdentifier[Address],
                        previousAddressId: TypedIdentifier[Address],
                        timeAtAddress: TypedIdentifier[AddressYears],
                        tradingTime: Option[TypedIdentifier[Boolean]]
                       ): Either[Message, Boolean] = {
    (get(currentAddressId), get(timeAtAddress)) match {
      case (Some(_), Some(AddressYears.OverAYear)) => Right(true)
      case (None, _) => Left(Message(s"incomplete.${lastPath(currentAddressId)}"))
      case (Some(_), Some(AddressYears.UnderAYear)) =>
        (get(previousAddressId), tradingTime) match {
          case (Some(_), Some(tradingTimeId)) if get(tradingTimeId).isEmpty | get(tradingTimeId).contains(true) =>
            Right(true)
          case (Some(_), None) =>
            Right(true)
          case (_, Some(tradingTimeId)) if get(tradingTimeId).contains(false) =>
            Right(true)
          case _ =>
            Left(Message(s"incomplete.${lastPath(previousAddressId)}"))
        }
      case _ => Left(Message(s"incomplete.${lastPath(timeAtAddress)}"))
    }
  }

  def isAnswerComplete[A](yesNoQuestionId: TypedIdentifier[Boolean],
                          yesValueId: TypedIdentifier[A],
                          noReasonIdOpt: Option[TypedIdentifier[String]])(implicit reads: Reads[A]): Either[Message, Boolean] =
    (get(yesNoQuestionId), get(yesValueId), noReasonIdOpt) match {
      case (None, None, _) => Left(Message(s"incomplete.${lastPath(yesNoQuestionId)}"))
      case (_, Some(_), _) => Right(true)
      case (_, _, Some(noReasonId)) if get(noReasonId).isDefined => Right(true)
      case (Some(false), _, None) => Right(true)
      case (Some(false), _, Some(noReasonId)) if get(noReasonId).isEmpty => Left(Message(s"incomplete.${lastPath(noReasonId)}"))
      case _ => Left(Message(s"incomplete.${lastPath(yesValueId)}"))
    }

  def isAnswerComplete[A](id: TypedIdentifier[A])(implicit rds: Reads[A]): Either[Message, Boolean] = {
    get(id) match {
      case None => Left(Message(s"incomplete.${lastPath(id)}"))
      case Some(_) => Right(true)
    }
  }

  def isCompanyComplete: Boolean = {
    getIncompleteCompanyDetails.isEmpty
  }

  def getIncompleteCompanyDetails: Seq[Message] = {
    isCompleteNew(
      Seq(
        isAnswerComplete(BusinessNameId),
        isAnswerComplete(BusinessUTRId),
        isAnswerComplete(CompanyRegistrationNumberId),
        isAnswerComplete(HasPAYEId, EnterPAYEId, None),
        isAnswerComplete(HasVATId, EnterVATId, None),
        isAddressComplete(CompanyContactAddressId, CompanyPreviousAddressId, CompanyAddressYearsId, Some(CompanyTradingOverAYearId)),
        isAnswerComplete(CompanyEmailId),
        isAnswerComplete(CompanyPhoneId)
      )
    )
  }

  def getIncompletePartnershipDetails: Seq[Message] = {
    isCompleteNew(
      Seq(
        isAnswerComplete(BusinessNameId),
        isAnswerComplete(BusinessUTRId),
        isAnswerComplete(HasPAYEId, EnterPAYEId, None),
        isAnswerComplete(HasVATId, EnterVATId, None),
        isAddressComplete(PartnershipContactAddressId, PartnershipPreviousAddressId, PartnershipAddressYearsId, Some(PartnershipTradingOverAYearId)),
        isAnswerComplete(PartnershipEmailId),
        isAnswerComplete(PartnershipPhoneId)
      )
    )
  }

  def getIncompleteDirectorDetails(index: Int): Seq[Message] = {
    isCompleteNew(
      Seq(
        isAnswerComplete(DirectorNameId(index)),
        isAnswerComplete(DirectorDOBId(index)),
        isAnswerComplete(HasDirectorNINOId(index), DirectorEnterNINOId(index), Some(DirectorNoNINOReasonId(index))),
        isAnswerComplete(HasDirectorUTRId(index), DirectorEnterUTRId(index), Some(DirectorNoUTRReasonId(index))),
        isAddressComplete(DirectorAddressId(index), DirectorPreviousAddressId(index), DirectorAddressYearsId(index), None),
        isAnswerComplete(DirectorEmailId(index)),
        isAnswerComplete(DirectorPhoneId(index))
      )
    )
  }

  def getIncompletePartnerDetails(index: Int): Seq[Message] = {
    isCompleteNew(
      Seq(
        isAnswerComplete(PartnerNameId(index)),
        isAnswerComplete(PartnerDOBId(index)),
        isAnswerComplete(HasPartnerNINOId(index), PartnerEnterNINOId(index), Some(PartnerNoNINOReasonId(index))),
        isAnswerComplete(HasPartnerUTRId(index), PartnerEnterUTRId(index), Some(PartnerNoUTRReasonId(index))),
        isAddressComplete(PartnerAddressId(index), PartnerPreviousAddressId(index), PartnerAddressYearsId(index), None),
        isAnswerComplete(PartnerEmailId(index)),
        isAnswerComplete(PartnerPhoneId(index))
      )
    )
  }

  def isPartnerComplete(index: Int): Boolean = {
    getIncompletePartnerDetails(index).isEmpty
  }

  def isDirectorComplete(index: Int): Boolean = {
    getIncompleteDirectorDetails(index).isEmpty
  }

  def getIncompleteIndividualDetails: Seq[Message] = {
    isCompleteNew(
      Seq(
        isAnswerComplete(IndividualDetailsId),
        isAnswerComplete(IndividualDateOfBirthId),
        isAnswerComplete(IndividualAddressId),
        isAnswerComplete(IndividualSameContactAddressId),
        isAddressComplete(IndividualContactAddressId, IndividualPreviousAddressId, IndividualAddressYearsId, None),
        isAnswerComplete(IndividualEmailId),
        isAnswerComplete(IndividualPhoneId)
      )
    )
  }
}
