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

package identifiers.register

import identifiers.TypedIdentifier
import identifiers.register.company._
import identifiers.register.company.directors.DirectorId
import identifiers.register.individual._
import identifiers.register.partnership._
import identifiers.register.partnership.partners.PartnerId
import models.PersonName
import play.api.libs.json.{JsResult, JsSuccess}
import utils.UserAnswers

case object RegisterAsBusinessId extends TypedIdentifier[Boolean] {
  override def toString: String = "registerAsBusiness"

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): JsResult[UserAnswers] = {
    value match {
      case Some(false) =>
        removeAllCompany(userAnswers)
          .flatMap(removeAllPartnership)
      case Some(true) =>
        removeAllIndividual(userAnswers)
      case _ => super.cleanup(value, userAnswers)
    }
  }

  private def removeAllIndividual(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.removeAllOf(List(IndividualDetailsId, IndividualAddressId, IndividualDateOfBirthId, IndividualSameContactAddressId,
      IndividualContactAddressListId, IndividualContactAddressId, IndividualAddressYearsId, IndividualPreviousAddressId,
      IndividualPreviousAddressListId, IndividualPreviousAddressPostCodeLookupId,
      IndividualEmailId, IndividualPhoneId
    ))
  }

  private def removeDirectorsOrPartners(personNameSeq: Seq[PersonName],
                                        userAnswers: UserAnswers, id: TypedIdentifier[Nothing]): JsResult[UserAnswers] = {
    if (personNameSeq.nonEmpty) {
      userAnswers.remove(id)
    } else {
      JsSuccess(userAnswers)
    }
  }

  private def removeAllCompany(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.removeAllOf(List(NonUKBusinessTypeId, BusinessNameId, BusinessUTRId, CompanyAddressId, CompanySameContactAddressId,
      CompanyAddressListId, CompanyContactAddressId, CompanyContactAddressListId, CompanyAddressYearsId, CompanyPreviousAddressId,
      CompanyPreviousAddressPostCodeLookupId, CompanyEmailId, CompanyPhoneId, MoreThanTenDirectorsId))
      .flatMap(answers => removeDirectorsOrPartners(answers.allDirectors, answers, DirectorId))
  }

  private def removeAllPartnership(userAnswers: UserAnswers): JsResult[UserAnswers] = {
    userAnswers.removeAllOf(List(NonUKBusinessTypeId, BusinessNameId, PartnershipRegisteredAddressId, PartnershipSameContactAddressId,
      PartnershipContactAddressPostCodeLookupId, PartnershipContactAddressListId, PartnershipContactAddressId,
      PartnershipAddressYearsId, PartnershipPreviousAddressId, PartnershipPreviousAddressPostCodeLookupId,
      PartnershipPreviousAddressListId, PartnershipEmailId, PartnershipPhoneId, MoreThanTenPartnersId))
      .flatMap(answers => removeDirectorsOrPartners(answers.allPartners, answers, PartnerId))
  }

}
